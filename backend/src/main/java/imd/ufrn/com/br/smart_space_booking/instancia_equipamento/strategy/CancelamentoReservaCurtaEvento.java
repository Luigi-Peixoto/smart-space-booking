package imd.ufrn.com.br.smart_space_booking.instancia_equipamento.strategy;

import java.time.temporal.ChronoUnit;
import java.util.List;

import imd.ufrn.com.br.smart_space_booking.framework.enums.MomentoEvento;
import imd.ufrn.com.br.smart_space_booking.framework.evento.EventoTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.model.RegraTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.model.Reserva;

/**
 * Evento do Equipamento: penaliza cancelar reservas CURTAS
 * (poucas horas) — o oposto do que a severidade do Veículo faz (lá, reservas
 * LONGAS são as mais sensíveis). O critério de disparo aqui é a duração da
 * PRÓPRIA reserva, diferente de antecedência (CANCELAMENTO_TARDIO) ou
 * contagem (EXCESSO_CANCELAMENTOS).
 */
public class CancelamentoReservaCurtaEvento implements EventoTrustScore {

    private static final long HORAS_RESERVA_CURTA = 2L;

    private final String chave;
    private final int deltaPadrao;
    private final MomentoEvento momento;

    public CancelamentoReservaCurtaEvento(String chave, int deltaPadrao, MomentoEvento momento) {
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
        return "Penalidade por cancelar uma reserva curta (2h ou menos) de equipamento.";
    }

    @Override
    public MomentoEvento momento() {
        return momento;
    }

    @Override
    public int avaliar(Reserva reserva, List<Reserva> reservasRelacionadas, RegraTrustScore regraConfigurada,
                       StringBuilder descricaoSaida) {
        long horasReservadas = ChronoUnit.HOURS.between(reserva.getInicioDateTime(), reserva.getFimDateTime());
        if (horasReservadas > HORAS_RESERVA_CURTA) {
            return 0;
        }

        int deltaBase = regraConfigurada != null ? regraConfigurada.getValorPrincipal() : deltaPadrao;
        descricaoSaida.append("Cancelamento de reserva curta (").append(horasReservadas).append("h).");
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
