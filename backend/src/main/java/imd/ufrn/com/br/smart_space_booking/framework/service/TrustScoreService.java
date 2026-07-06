package imd.ufrn.com.br.smart_space_booking.framework.service;

import imd.ufrn.com.br.smart_space_booking.framework.dto.AvaliacaoCriterioDTO;
import imd.ufrn.com.br.smart_space_booking.framework.dto.TrustScoreHistoricoResponseDTO;
import imd.ufrn.com.br.smart_space_booking.framework.enums.CategoriaRegraTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.enums.NivelExigencia;
import imd.ufrn.com.br.smart_space_booking.framework.enums.TrustScoreHistoricoTipo;
import imd.ufrn.com.br.smart_space_booking.framework.enums.UsuarioStatus;
import imd.ufrn.com.br.smart_space_booking.framework.exception.TrustScoreInsuficienteException;
import imd.ufrn.com.br.smart_space_booking.framework.model.Recurso;
import imd.ufrn.com.br.smart_space_booking.framework.model.RegraAvaliacao;
import imd.ufrn.com.br.smart_space_booking.framework.model.RegraTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.model.Reserva;
import imd.ufrn.com.br.smart_space_booking.framework.model.TrustScoreHistorico;
import imd.ufrn.com.br.smart_space_booking.framework.model.Usuario;
import imd.ufrn.com.br.smart_space_booking.framework.repository.RegraAvaliacaoRepository;
import imd.ufrn.com.br.smart_space_booking.framework.repository.RegraTrustScoreRepository;
import imd.ufrn.com.br.smart_space_booking.framework.repository.ReservaRepository;
import imd.ufrn.com.br.smart_space_booking.framework.repository.TrustScoreHistoricoRepository;
import imd.ufrn.com.br.smart_space_booking.framework.repository.UsuarioRepository;
import imd.ufrn.com.br.smart_space_booking.framework.restricao.RestricaoTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.strategy.TrustScoreStrategy;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Dono de tudo que envolve TrustScore: aplicar deltas (penalidades/bônus) E
 * decidir se uma tentativa de reserva deve ser barrada (exigência de acesso +
 * restrições do hotspot) — antes essa segunda parte vivia espalhada no
 * ReservaService; juntar aqui faz mais sentido porque as duas pontas mexem
 * na mesma coisa (o TrustScore do usuário) e agora compartilham o mesmo
 * histórico (penalidade muda o score; bloqueio só documenta a tentativa).
 */
@Service
public class TrustScoreService {

    private static final int SCORE_MINIMO = 0;
    private static final int SCORE_MAXIMO = 100;

    private final RegraAvaliacaoRepository regraRepository;
    private final RegraTrustScoreRepository regraTrustScoreRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReservaRepository reservaRepository;
    private final TrustScoreHistoricoRepository historicoRepository;
    private final TransactionTemplate transacaoNova;

    public TrustScoreService(RegraAvaliacaoRepository regraRepository,
                             RegraTrustScoreRepository regraTrustScoreRepository,
                             UsuarioRepository usuarioRepository,
                             ReservaRepository reservaRepository,
                             TrustScoreHistoricoRepository historicoRepository,
                             PlatformTransactionManager transactionManager) {
        this.regraRepository = regraRepository;
        this.regraTrustScoreRepository = regraTrustScoreRepository;
        this.usuarioRepository = usuarioRepository;
        this.reservaRepository = reservaRepository;
        this.historicoRepository = historicoRepository;
        this.transacaoNova = new TransactionTemplate(transactionManager);
        this.transacaoNova.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /**
     * Aplica uma alteração no TrustScore originada por um critério de avaliação
     * (checkout via IA) ou por um ajuste manual, e registra no histórico.
     *
     * @param usuario   Usuário afetado — obrigatório
     * @param delta     Variação positiva (bônus) ou negativa (penalidade) — obrigatório
     * @param regra     RegraAvaliacao que originou a alteração — null se não houver (ex: ajuste manual)
     * @param reserva   Reserva relacionada — null se não houver
     * @param descricao Contexto adicional — null se não houver
     */
    @Transactional
    public void registrarAlteracao(Usuario usuario, int delta, RegraAvaliacao regra,
                                   Reserva reserva, String descricao) {
        aplicarEHistoriar(usuario, delta, regra, null, reserva, descricao);
    }

    /**
     * Aplica uma alteração no TrustScore originada por um evento estrutural do
     * ciclo de vida da reserva (cancelamento tardio, no-show, excesso de
     * cancelamentos), e registra no histórico.
     *
     * @param usuario     Usuário afetado — obrigatório
     * @param delta       Variação (sempre negativa hoje, mas não é uma regra fixa) — obrigatório
     * @param regraEvento RegraTrustScore (categoria EVENTO) que originou a alteração — null se não cadastrada (usa fallback da strategy)
     * @param reserva     Reserva relacionada — null se não houver
     * @param descricao   Contexto adicional — null se não houver
     */
    @Transactional
    public void registrarAlteracaoPorEvento(Usuario usuario, int delta, RegraTrustScore regraEvento,
                                            Reserva reserva, String descricao) {
        aplicarEHistoriar(usuario, delta, null, regraEvento, reserva, descricao);
    }

    private void aplicarEHistoriar(Usuario usuario, int delta, RegraAvaliacao regra, RegraTrustScore regraTrustScore,
                                   Reserva reserva, String descricao) {
        int scoreAnterior = usuario.getTrustScore() != null ? usuario.getTrustScore() : SCORE_MAXIMO;
        int scorePosterior = clamp(scoreAnterior + delta, SCORE_MINIMO, SCORE_MAXIMO);

        usuario.setTrustScore(scorePosterior);
        usuario.setNivelRestricao(calcularNivelRestricao(scorePosterior));
        if (scorePosterior == SCORE_MINIMO) {
            usuario.setStatus(UsuarioStatus.SUSPENSO);
        }
        usuarioRepository.save(usuario);

        TrustScoreHistorico historico = new TrustScoreHistorico();
        historico.setTipo(TrustScoreHistoricoTipo.PENALIDADE);
        historico.setUsuario(usuario);
        historico.setReserva(reserva);
        historico.setRegra(regra);
        historico.setRegraTrustScore(regraTrustScore);
        historico.setDelta(delta);
        historico.setScoreAnterior(scoreAnterior);
        historico.setScorePosterior(scorePosterior);
        historico.setDescricao(descricao);
        historicoRepository.save(historico);
    }

    // ─── Impacto no uso: exigência de acesso + restrições do hotspot ──────────

    /**
     * Barra a tentativa de reserva se o usuário não tiver o TrustScore
     * necessário — primeiro checa a exigência de acesso do recurso (fixa,
     * baseada na sensibilidade dele), depois as restrições específicas que o
     * hotspot declarou ({@link TrustScoreStrategy#restricoes()}). Qualquer
     * violação grava um registro de BLOQUEIO no histórico e lança
     * {@link TrustScoreInsuficienteException}.
     */
    @Transactional
    public void validarRestricoes(Usuario usuario, Reserva reservaTentativa, TrustScoreStrategy strategy) {
        validarExigenciaDeAcesso(usuario, reservaTentativa, strategy);

        List<Reserva> reservasAtivasMesmoTipo = reservaRepository.findReservasAtivasPorUsuario(usuario.getId())
                .stream()
                .filter(r -> strategy.suporta(r.getRecurso()))
                .toList();

        for (RestricaoTrustScore restricao : strategy.restricoes()) {
            RegraTrustScore regraConfigurada = buscarRegra(CategoriaRegraTrustScore.RESTRICAO, restricao.chave());
            String mensagem = restricao.avaliar(usuario, reservaTentativa, reservasAtivasMesmoTipo, regraConfigurada);
            if (mensagem != null) {
                registrarBloqueio(usuario, reservaTentativa.getRecurso(), regraConfigurada, mensagem);
                throw new TrustScoreInsuficienteException(mensagem);
            }
        }
    }

    private void validarExigenciaDeAcesso(Usuario usuario, Reserva reservaTentativa, TrustScoreStrategy strategy) {
        NivelExigencia nivel = strategy.nivelExigencia(reservaTentativa);
        RegraTrustScore regraConfigurada = buscarRegra(CategoriaRegraTrustScore.EXIGENCIA, nivel.name());
        int scoreMinimo = regraConfigurada != null ? regraConfigurada.getValorPrincipal() : strategy.scoreMinimoPadrao(nivel);
        int scoreAtual = scoreAtual(usuario);

        if (scoreAtual < scoreMinimo) {
            String mensagem = "TrustScore insuficiente para reservar este recurso (exigência " + nivel + "): seu score é "
                    + scoreAtual + ", o mínimo exigido é " + scoreMinimo + ".";
            registrarBloqueio(usuario, reservaTentativa.getRecurso(), regraConfigurada, mensagem);
            throw new TrustScoreInsuficienteException(mensagem);
        }
    }

    private RegraTrustScore buscarRegra(CategoriaRegraTrustScore categoria, String chave) {
        return regraTrustScoreRepository.findByCategoriaAndChave(categoria, chave).orElse(null);
    }

    /** Grava uma tentativa de reserva barrada — não muda o score, só documenta o que aconteceu e por quê. */
    private void registrarBloqueio(Usuario usuario, Recurso recurso, RegraTrustScore regraConfigurada, String mensagem) {
        int score = scoreAtual(usuario);

        transacaoNova.executeWithoutResult(status -> {
            TrustScoreHistorico historico = new TrustScoreHistorico();
            historico.setTipo(TrustScoreHistoricoTipo.BLOQUEIO);
            historico.setUsuario(usuario);
            historico.setRecurso(recurso);
            historico.setRegraTrustScore(regraConfigurada);
            historico.setDelta(0);
            historico.setScoreAnterior(score);
            historico.setScorePosterior(score);
            historico.setDescricao(mensagem);
            historicoRepository.save(historico);
        });
    }

    private int scoreAtual(Usuario usuario) {
        return usuario.getTrustScore() != null ? usuario.getTrustScore() : SCORE_MAXIMO;
    }

    /**
     * Calcula o nível de exigência do usuário, que determina o TrustScore mínimo pra reservar.
     * O framework fornece um fallback genérico baseado no score atual, mas cada hotspot pode sobrescrever isso com a sua própria lógica de severidade.
     * 
     */
    private NivelExigencia calcularNivelRestricao(int score) {
        if (score >= scoreMinimoExigenciaGlobal(NivelExigencia.ALTA)) return NivelExigencia.ALTA;
        if (score >= scoreMinimoExigenciaGlobal(NivelExigencia.MEDIA)) return NivelExigencia.MEDIA;
        return NivelExigencia.BAIXA;
    }

    private int scoreMinimoExigenciaGlobal(NivelExigencia nivel) {
        RegraTrustScore regra = buscarRegra(CategoriaRegraTrustScore.EXIGENCIA, nivel.name());
        return regra != null ? regra.getValorPrincipal() : nivel.scoreMinimoPadrao();
    }

    /**
     * Aplica uma lista de alterações de TrustScore originadas por critérios de avaliação (checkout via IA) e registra no histórico.
     * Retorna o delta total aplicado (positivo ou negativo).
     */
    @Transactional
    public int aplicarDelta(Long usuarioId, List<AvaliacaoCriterioDTO> criteriosAvaliados, Reserva reserva) {
        if (criteriosAvaliados == null || criteriosAvaliados.isEmpty()) return 0;

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + usuarioId));

        Map<Long, RegraAvaliacao> regrasById = regraRepository.findAll()
                .stream()
                .collect(Collectors.toMap(RegraAvaliacao::getId, r -> r));

        int deltaTotal = 0;

        for (AvaliacaoCriterioDTO avaliado : criteriosAvaliados) {
            if (avaliado.getId() == null || avaliado.getNota() == null) continue;

            RegraAvaliacao regra = regrasById.get(avaliado.getId());
            if (regra == null) continue;

            StringBuilder descricao = new StringBuilder();
            int delta = avaliarCriterio(regra, avaliado.getNota(), descricao);
            if (delta != 0) {
                registrarAlteracao(usuario, delta, regra, reserva, descricao.toString());
                deltaTotal += delta;
            }
        }

        return deltaTotal;
    }

    /**
     * Avalia um critério de avaliação (nota de 0 a 10) e retorna o delta de TrustScore a ser aplicado.
     * Se a nota estiver acima do limite de bônus, aplica o delta de bônus; se estiver abaixo do limite de penalidade, aplica o delta de penalidade; 
     * caso contrário, não aplica nenhuma alteração. A descrição da avaliação é escrita no StringBuilder fornecido.
     * 
     */
    private int avaliarCriterio(RegraAvaliacao regra, int notaBruta, StringBuilder descricaoSaida) {
        int nota = clamp(notaBruta, 0, 10);

        if (nota >= regra.getLimiBonus() && regra.getDeltaBonus() != 0) {
            descricaoSaida.append("Bônus: ").append(regra.getNome()).append(" (nota ").append(nota).append(")");
            return regra.getDeltaBonus();
        }
        if (nota < regra.getLimiPenalidade() && regra.getDeltaPenalidade() != 0) {
            descricaoSaida.append("Penalidade: ").append(regra.getNome()).append(" (nota ").append(nota).append(")");
            return regra.getDeltaPenalidade();
        }
        return 0;
    }

    /**
     * Retorna o histórico completo de um usuário, do mais recente ao mais antigo.
     */
    public List<TrustScoreHistoricoResponseDTO> buscarHistorico(Long usuarioId) {
        return historicoRepository.findByUsuarioIdOrderByCriadoEmDesc(usuarioId)
                .stream()
                .map(TrustScoreHistoricoResponseDTO::fromEntity)
                .toList();
    }

    private int clamp(int valor, int min, int max) {
        return Math.max(min, Math.min(max, valor));
    }
}
