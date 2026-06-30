package imd.ufrn.com.br.smart_space_booking.strategy;

import imd.ufrn.com.br.smart_space_booking.model.Reserva;
import imd.ufrn.com.br.smart_space_booking.model.Usuario;
import imd.ufrn.com.br.smart_space_booking.repository.RegraAvaliacaoRepository;
import imd.ufrn.com.br.smart_space_booking.service.TrustScoreService;

public interface TrustScoreStrategy {

    /**
     * Janela de antecedência mínima para cancelamento sem penalidade.
     * Ex: 2h para salas, 6h para equipamentos, 24h para veículos.
     */
    long getJanelaCancelamentoEmHoras();

    /**
     * Processa a penalidade de cancelamento tardio.
     * Cada hotspot decide qual regra busca, o delta de fallback,
     * a descrição e quaisquer ações adicionais.
     */
    void processarCancelamentoTardio(Usuario usuario, Reserva reserva,
                                     long horasDeAntecedencia,
                                     RegraAvaliacaoRepository regraRepository,
                                     TrustScoreService trustScoreService);

    /**
     * Processa a penalidade de no-show automático.
     */
    void processarNoShow(Usuario usuario, Reserva reserva,
                         RegraAvaliacaoRepository regraRepository,
                         TrustScoreService trustScoreService);

    /**
     * Processa a penalidade por excesso de cancelamentos na semana.
     * Cada hotspot pode ter limiar diferente ou ignorar o evento.
     */
    void processarExcessoCancelamentos(Usuario usuario, Reserva reserva,
                                       long totalCancelamentos,
                                       RegraAvaliacaoRepository regraRepository,
                                       TrustScoreService trustScoreService);
}