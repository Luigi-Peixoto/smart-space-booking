package imd.ufrn.com.br.smart_space_booking.instancia_veiculo.strategy;

import java.time.Duration;
import java.util.List;

import imd.ufrn.com.br.smart_space_booking.framework.enums.MomentoEvento;
import imd.ufrn.com.br.smart_space_booking.framework.evento.EventoTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.model.RegraTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.model.Reserva;

/**
 * Evento do Veículo: penaliza devolver (checkout) depois do
 * fim da reserva, com severidade que escala pelo PRÓPRIO ATRASO (cada hora a
 * mais soma mais penalidade) — diferente do Equipamento, que escala por uma
 * característica FIXA do recurso (tamanho do kit). Demonstra que a
 * severidade pode vir de qualquer lugar: constante, característica do
 * recurso, ou dado calculado na hora do evento.
 */
public class DevolucaoAtrasadaVeiculoEvento implements EventoTrustScore {

    private final String chave;
    private final int deltaPorHoraAtraso;
    private final MomentoEvento momento;

    public DevolucaoAtrasadaVeiculoEvento(String chave, int deltaPorHoraAtraso, MomentoEvento momento) {
        this.chave = chave;
        this.deltaPorHoraAtraso = deltaPorHoraAtraso;
        this.momento = momento;
    }

    @Override
    public String chave() {
        return chave;
    }

    @Override
    public String descricao() {
        return "Penalidade por devolver o veículo atrasado — escala por hora de atraso, não por característica fixa do recurso.";
    }

    @Override
    public MomentoEvento momento() {
        return momento;
    }

    @Override
    public int avaliar(Reserva reserva, List<Reserva> reservasRelacionadas, RegraTrustScore regraConfigurada,
                       StringBuilder descricaoSaida) {
        long minutosAtraso = Duration.between(reserva.getFimDateTime(), reserva.getDataHoraCheckout()).toMinutes();
        if (minutosAtraso <= 0) {
            return 0;
        }

        long horasAtrasoArredondadas = Math.max(1, minutosAtraso / 60);
        int deltaPorHora = regraConfigurada != null ? regraConfigurada.getValorPrincipal() : deltaPorHoraAtraso;
        int delta = (int) (deltaPorHora * horasAtrasoArredondadas);

        descricaoSaida.append("Devolução do veículo com ").append(minutosAtraso)
                .append(" minutos de atraso (").append(horasAtrasoArredondadas).append("h x ")
                .append(deltaPorHora).append(" por hora).");
        return delta;
    }

    @Override
    public int deltaPadrao() {
        return deltaPorHoraAtraso;
    }

    @Override
    public Integer parametroPadrao() {
        return null;
    }
}
