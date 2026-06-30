package imd.ufrn.com.br.smart_space_booking.strategy;

import imd.ufrn.com.br.smart_space_booking.model.RegraAvaliacao;
import imd.ufrn.com.br.smart_space_booking.model.Reserva;
import imd.ufrn.com.br.smart_space_booking.model.Usuario;
import imd.ufrn.com.br.smart_space_booking.repository.RegraAvaliacaoRepository;
import imd.ufrn.com.br.smart_space_booking.service.TrustScoreService;
import org.springframework.stereotype.Component;

@Component("trustScoreSala")
public class TrustScoreSalaStrategy implements TrustScoreStrategy {

    @Override
    public long getJanelaCancelamentoEmHoras() {
        return 2L;
    }

    @Override
    public void processarCancelamentoTardio(Usuario usuario, Reserva reserva,
                                            long horasDeAntecedencia,
                                            RegraAvaliacaoRepository regraRepository,
                                            TrustScoreService trustScoreService) {
        RegraAvaliacao regra = regraRepository
                .findByNomeIgnoreCase("Cancelamento Tardio")
                .orElse(null);

        int delta = regra != null ? regra.getDeltaPenalidade() : -10;
        String descricao = "Cancelamento de sala com " + horasDeAntecedencia + "h de antecedência.";

        trustScoreService.registrarAlteracao(usuario, delta, regra, reserva, descricao);

    }

    @Override
    public void processarNoShow(Usuario usuario, Reserva reserva,
                                RegraAvaliacaoRepository regraRepository,
                                TrustScoreService trustScoreService) {
        RegraAvaliacao regra = regraRepository
                .findByNomeIgnoreCase("No-Show")
                .orElse(null);

        int delta = regra != null ? regra.getDeltaPenalidade() : -15;

        trustScoreService.registrarAlteracao(usuario, delta, regra, reserva,
                "Reserva de sala cancelada automaticamente por no-show.");
    }

    @Override
    public void processarExcessoCancelamentos(Usuario usuario, Reserva reserva,
                                              long totalCancelamentos,
                                              RegraAvaliacaoRepository regraRepository,
                                              TrustScoreService trustScoreService) {
        regraRepository.findByNomeIgnoreCase("Excesso de Cancelamentos").ifPresent(regra -> {
            if (totalCancelamentos > regra.getLimiPenalidade()) {
                trustScoreService.registrarAlteracao(
                        usuario,
                        regra.getDeltaPenalidade(),
                        regra,
                        reserva,
                        "Excesso de cancelamentos de sala na semana (" + totalCancelamentos + " cancelamentos)."
                );
            }
        });
    }
}