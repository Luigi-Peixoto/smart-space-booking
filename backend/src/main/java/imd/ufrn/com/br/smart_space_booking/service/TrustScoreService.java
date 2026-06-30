package imd.ufrn.com.br.smart_space_booking.service;

import imd.ufrn.com.br.smart_space_booking.dto.AvaliacaoCriterioDTO;
import imd.ufrn.com.br.smart_space_booking.dto.TrustScoreHistoricoResponseDTO;
import imd.ufrn.com.br.smart_space_booking.enums.UsuarioStatus;
import imd.ufrn.com.br.smart_space_booking.model.RegraAvaliacao;
import imd.ufrn.com.br.smart_space_booking.model.Reserva;
import imd.ufrn.com.br.smart_space_booking.model.TrustScoreHistorico;
import imd.ufrn.com.br.smart_space_booking.model.Usuario;
import imd.ufrn.com.br.smart_space_booking.repository.RegraAvaliacaoRepository;
import imd.ufrn.com.br.smart_space_booking.repository.TrustScoreHistoricoRepository;
import imd.ufrn.com.br.smart_space_booking.repository.UsuarioRepository;
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

    @Transactional
    public void registrarAlteracao(Usuario usuario, int delta, RegraAvaliacao regra,
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
        historico.setDelta(delta);
        historico.setScoreAnterior(scoreAnterior);
        historico.setScorePosterior(scorePosterior);
        historico.setDescricao(descricao);
        historicoRepository.save(historico);
    }

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

            int nota = clamp(avaliado.getNota(), 0, 10);
            int delta = 0;
            String descricao = null;

            if (nota >= regra.getLimiBonus()) {
                delta = regra.getDeltaBonus();
                descricao = "Bônus: " + regra.getNome() + " (nota " + nota + ")";
            } else if (nota < regra.getLimiPenalidade()) {
                delta = regra.getDeltaPenalidade();
                descricao = "Penalidade: " + regra.getNome() + " (nota " + nota + ")";
            }

            if (delta != 0) {
                registrarAlteracao(usuario, delta, regra, reserva, descricao);
                deltaTotal += delta;
            }
        }

        return deltaTotal;
    }

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