package imd.ufrn.com.br.smart_space_booking.instancia_sala.strategy;

import java.time.Duration;
import java.util.List;

import org.hibernate.Hibernate;

import imd.ufrn.com.br.smart_space_booking.framework.enums.MomentoEvento;
import imd.ufrn.com.br.smart_space_booking.framework.evento.EventoTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.model.RegraTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.model.Reserva;
import imd.ufrn.com.br.smart_space_booking.instancia_sala.model.Sala;

/**
 * Evento da Sala (não vem pronto do framework): penaliza
 * fazer check-in atrasado — mas antes do prazo de no-show — escalando pela
 * capacidade da sala, já que uma sala grande tem mais gente esperando por
 * quem chegou atrasado. Demonstra um MOMENTO novo (CHECKIN), que os 3
 * eventos prontos do framework nunca usam.
 */
public class CheckinAtrasadoEvento implements EventoTrustScore {

    private static final long TOLERANCIA_MINUTOS = 5L;

    private final String chave;
    private final int deltaPadrao;
    private final MomentoEvento momento;

    public CheckinAtrasadoEvento(String chave, int deltaPadrao, MomentoEvento momento) {
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
        return "Penalidade por check-in com mais de 5 minutos de atraso, escalada pela capacidade da sala.";
    }

    @Override
    public MomentoEvento momento() {
        return momento;
    }

    @Override
    public int avaliar(Reserva reserva, List<Reserva> reservasRelacionadas, RegraTrustScore regraConfigurada,
                       StringBuilder descricaoSaida) {
        long minutosAtraso = Duration.between(reserva.getInicioDateTime(), reserva.getDataHoraCheckin()).toMinutes();
        if (minutosAtraso <= TOLERANCIA_MINUTOS) {
            return 0;
        }

        Sala sala = (Sala) Hibernate.unproxy(reserva.getRecurso());
        double fator = sala.getCapacidade() != null && sala.getCapacidade() > 20 ? 1.5 : 1.0;

        int deltaBase = regraConfigurada != null ? regraConfigurada.getValorPrincipal() : deltaPadrao;
        int delta = EventoTrustScore.escalar(deltaBase, fator);
        descricaoSaida.append("Check-in com ").append(minutosAtraso).append(" minutos de atraso")
                .append(EventoTrustScore.sufixoAjuste(deltaBase, delta)).append(".");
        return delta;
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
