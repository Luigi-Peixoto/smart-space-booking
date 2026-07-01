package imd.ufrn.com.br.smart_space_booking.strategy;

public interface TrustScoreStrategy {

    /**
     * Janela de antecedência mínima para cancelamento sem penalidade.
     * 2h para salas, 6h para equipamentos, 24h para veículos.
     */
    long getJanelaCancelamentoEmHoras();

    /**
     * Delta aplicado quando RegraAvaliacao "No-Show" não existe no banco.
     */
    int getDeltaPadraoNoShow();

    /**
     * Delta aplicado quando RegraAvaliacao "Cancelamento Tardio" não existe no banco.
     */
    int getDeltaPadraoCancelamentoTardio();
}