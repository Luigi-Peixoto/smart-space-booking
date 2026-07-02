package imd.ufrn.com.br.smart_space_booking.framework.service;

import imd.ufrn.com.br.smart_space_booking.framework.dto.AvaliacaoCriterioDTO;
import imd.ufrn.com.br.smart_space_booking.framework.dto.TrustScoreHistoricoResponseDTO;
import imd.ufrn.com.br.smart_space_booking.framework.enums.UsuarioStatus;
import imd.ufrn.com.br.smart_space_booking.framework.model.RegraAvaliacao;
import imd.ufrn.com.br.smart_space_booking.framework.model.RegraTrustScoreEvento;
import imd.ufrn.com.br.smart_space_booking.framework.model.Reserva;
import imd.ufrn.com.br.smart_space_booking.framework.model.TrustScoreHistorico;
import imd.ufrn.com.br.smart_space_booking.framework.model.Usuario;
import imd.ufrn.com.br.smart_space_booking.framework.repository.RegraAvaliacaoRepository;
import imd.ufrn.com.br.smart_space_booking.framework.repository.TrustScoreHistoricoRepository;
import imd.ufrn.com.br.smart_space_booking.framework.repository.UsuarioRepository;
import imd.ufrn.com.br.smart_space_booking.framework.strategy.TrustScoreDecisao;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TrustScoreService {

    private static final int SCORE_MINIMO = 0;
    private static final int SCORE_MAXIMO = 100;

    private final RegraAvaliacaoRepository regraRepository;
    private final UsuarioRepository usuarioRepository;
    private final TrustScoreHistoricoRepository historicoRepository;

    public TrustScoreService(RegraAvaliacaoRepository regraRepository,
                             UsuarioRepository usuarioRepository,
                             TrustScoreHistoricoRepository historicoRepository) {
        this.regraRepository = regraRepository;
        this.usuarioRepository = usuarioRepository;
        this.historicoRepository = historicoRepository;
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
     * @param regraEvento RegraTrustScoreEvento que originou a alteração — null se não cadastrada (usa fallback da strategy)
     * @param reserva     Reserva relacionada — null se não houver
     * @param descricao   Contexto adicional — null se não houver
     */
    @Transactional
    public void registrarAlteracaoPorEvento(Usuario usuario, int delta, RegraTrustScoreEvento regraEvento,
                                            Reserva reserva, String descricao) {
        aplicarEHistoriar(usuario, delta, null, regraEvento, reserva, descricao);
    }

    private void aplicarEHistoriar(Usuario usuario, int delta, RegraAvaliacao regra, RegraTrustScoreEvento regraEvento,
                                   Reserva reserva, String descricao) {
        int scoreAnterior = usuario.getTrustScore() != null ? usuario.getTrustScore() : SCORE_MAXIMO;
        int scorePosterior = clamp(scoreAnterior + delta, SCORE_MINIMO, SCORE_MAXIMO);

        usuario.setTrustScore(scorePosterior);
        if (scorePosterior == SCORE_MINIMO) {
            usuario.setStatus(UsuarioStatus.SUSPENSO);
        }
        usuarioRepository.save(usuario);

        TrustScoreHistorico historico = new TrustScoreHistorico();
        historico.setUsuario(usuario);
        historico.setReserva(reserva);
        historico.setRegra(regra);
        historico.setRegraEvento(regraEvento);
        historico.setDelta(delta);
        historico.setScoreAnterior(scoreAnterior);
        historico.setScorePosterior(scorePosterior);
        historico.setDescricao(descricao);
        historicoRepository.save(historico);
    }

    /**
     * Aplica deltas a partir de uma lista de critérios avaliados com notas.
     * Chama registrarAlteracao para cada critério que gerou alteração.
     * Uso principal: checkout via IA.
     *
     * @param usuarioId          ID do usuário afetado
     * @param criteriosAvaliados Lista de critérios com notas
     * @param reserva            Reserva relacionada — null se não houver
     * @return delta total aplicado
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

            TrustScoreDecisao decisao = avaliarCriterio(regra, avaliado.getNota());
            if (decisao.aplicavel()) {
                registrarAlteracao(usuario, decisao.delta(), regra, reserva, decisao.descricao());
                deltaTotal += decisao.delta();
            }
        }

        return deltaTotal;
    }

    /**
     * Decide se a nota dada a um critério de avaliação gera bônus, penalidade ou nada —
     * mesma fórmula genérica para qualquer RegraAvaliacao, independente do hotspot.
     */
    private TrustScoreDecisao avaliarCriterio(RegraAvaliacao regra, int notaBruta) {
        int nota = clamp(notaBruta, 0, 10);

        if (nota >= regra.getLimiBonus() && regra.getDeltaBonus() != 0) {
            return TrustScoreDecisao.aplicavel(regra.getDeltaBonus(),
                    "Bônus: " + regra.getNome() + " (nota " + nota + ")");
        }
        if (nota < regra.getLimiPenalidade() && regra.getDeltaPenalidade() != 0) {
            return TrustScoreDecisao.aplicavel(regra.getDeltaPenalidade(),
                    "Penalidade: " + regra.getNome() + " (nota " + nota + ")");
        }
        return TrustScoreDecisao.naoAplicavel();
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