package imd.ufrn.com.br.smart_space_booking.strategy;

import org.springframework.stereotype.Component;

@Component("trustScoreSala")
public class TrustScoreSalaStrategy implements TrustScoreStrategy {

    @Override
    public long getJanelaCancelamentoEmHoras() { return 2L; }

    @Override
    public int getDeltaPadraoNoShow() { return -15; }

    @Override
    public int getDeltaPadraoCancelamentoTardio() { return -10; }
}