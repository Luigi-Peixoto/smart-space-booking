package imd.ufrn.com.br.smart_space_booking.instancia_veiculo.strategy;

import java.time.ZoneId;
import java.util.List;

import imd.ufrn.com.br.smart_space_booking.framework.enums.MomentoEvento;
import imd.ufrn.com.br.smart_space_booking.framework.evento.EventoTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.model.RegraTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.model.Reserva;

/**
 * Evento do Veículo: penaliza cancelar reservas cujo início
 * cai na madrugada (0h–6h) — critério baseado no HORÁRIO DO DIA da reserva,
 * diferente de antecedência, contagem ou duração.
 */
public class CancelamentoMadrugadaEvento implements EventoTrustScore {

    private static final int HORA_FIM_MADRUGADA = 6;
    private static final ZoneId ZONA_LOCAL = ZoneId.of("America/Fortaleza");

    private final String chave;
    private final int deltaPadrao;
    private final MomentoEvento momento;

    public CancelamentoMadrugadaEvento(String chave, int deltaPadrao, MomentoEvento momento) {
        this.chave = chave;
        this.deltaPadrao = deltaPadrao;
        this.momento = momento;
    }

    @Override
    public String chave() {
        return chave;
    }

    @Override
    public String descricao() {
        return "Penalidade por cancelar uma reserva de veículo com início na madrugada (0h-6h).";
    }

    @Override
    public MomentoEvento momento() {
        return momento;
    }

    @Override
    public int avaliar(Reserva reserva, List<Reserva> reservasRelacionadas, RegraTrustScore regraConfigurada,
                       StringBuilder descricaoSaida) {
        int hora = reserva.getInicioDateTime().withZoneSameInstant(ZONA_LOCAL).getHour();
        if (hora >= HORA_FIM_MADRUGADA) {
            return 0;
        }

        int deltaBase = regraConfigurada != null ? regraConfigurada.getValorPrincipal() : deltaPadrao;
        descricaoSaida.append("Cancelamento de reserva com início às ").append(hora).append("h (madrugada).");
        return deltaBase;
    }

    @Override
    public int deltaPadrao() {
        return deltaPadrao;
    }

    @Override
    public Integer parametroPadrao() {
        return null;
    }
}
