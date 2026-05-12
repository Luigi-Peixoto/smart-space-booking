package imd.ufrn.com.br.smart_space_booking.service;

import imd.ufrn.com.br.smart_space_booking.dto.AvaliacaoCriterioDTO;
import imd.ufrn.com.br.smart_space_booking.model.RegraAvaliacao;
import imd.ufrn.com.br.smart_space_booking.model.Usuario;
import imd.ufrn.com.br.smart_space_booking.repository.RegraAvaliacaoRepository;
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

    public TrustScoreService(RegraAvaliacaoRepository regraRepository,
                             UsuarioRepository usuarioRepository) {
        this.regraRepository = regraRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public int aplicarDelta(Long usuarioId, List<AvaliacaoCriterioDTO> criteriosAvaliados) {
        if (criteriosAvaliados == null || criteriosAvaliados.isEmpty()) {
            return 0;
        }

        Map<Long, RegraAvaliacao> regrasById = regraRepository.findAll()
                .stream()
                .collect(Collectors.toMap(RegraAvaliacao::getId, r -> r));

        int deltaTotal = calcularDeltaTotal(criteriosAvaliados, regrasById);

        if (deltaTotal == 0) {
            return 0;
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + usuarioId));

        int scoreAtual = usuario.getTrustScore() != null ? usuario.getTrustScore() : SCORE_MAXIMO;
        int novoScore = clamp(scoreAtual + deltaTotal, SCORE_MINIMO, SCORE_MAXIMO);

        usuario.setTrustScore(novoScore);
        usuarioRepository.save(usuario);

        return deltaTotal;
    }

    private int calcularDeltaTotal(List<AvaliacaoCriterioDTO> criterios,
                                   Map<Long, RegraAvaliacao> regrasById) {
        int total = 0;

        for (AvaliacaoCriterioDTO avaliado : criterios) {
            if (avaliado.getId() == null || avaliado.getNota() == null) continue;

            RegraAvaliacao regra = regrasById.get(avaliado.getId());
            if (regra == null) continue; // regra foi desativada/removida após a auditoria

            int nota = clamp(avaliado.getNota(), 0, 10);

            if (nota >= regra.getLimiBonus()) {
                total += regra.getDeltaBonus();
            } else if (nota < regra.getLimiPenalidade()) {
                total += regra.getDeltaPenalidade(); // já é negativo
            }
            // zona neutra: não acumula nada
        }

        return total;
    }

    private int clamp(int valor, int min, int max) {
        return Math.max(min, Math.min(max, valor));
    }
}