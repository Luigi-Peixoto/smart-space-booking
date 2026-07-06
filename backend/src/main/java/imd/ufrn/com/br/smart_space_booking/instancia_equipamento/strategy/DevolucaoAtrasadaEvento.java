package imd.ufrn.com.br.smart_space_booking.instancia_equipamento.strategy;

import java.time.Duration;
import java.util.List;

import org.hibernate.Hibernate;

import imd.ufrn.com.br.smart_space_booking.framework.enums.MomentoEvento;
import imd.ufrn.com.br.smart_space_booking.framework.evento.EventoTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.model.RegraTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.model.Reserva;
import imd.ufrn.com.br.smart_space_booking.instancia_equipamento.model.Equipamento;

/**
 * Evento do Equipamento: penaliza devolver (checkout) depois
 * do fim da reserva, escalando pelo tamanho do kit — o mesmo dado que
 * usa pra BLOQUEAR reservas grandes, aqui
 * usado pra PENALIZAR depois do fato. Mesma característica do recurso, dois
 * pontos de extensão diferentes (restrição vs evento). Demonstra o momento
 * CHECKOUT.
 */
public class DevolucaoAtrasadaEvento implements EventoTrustScore {

    private static final long TOLERANCIA_MINUTOS = 10L;
    private static final int TAMANHO_KIT_GRANDE = 3;

    private final String chave;
    private final int deltaPadrao;
    private final MomentoEvento momento;

    public DevolucaoAtrasadaEvento(String chave, int deltaPadrao, MomentoEvento momento) {
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
        return "Penalidade por devolver o equipamento após o fim da reserva, escalada pelo tamanho do kit.";
    }

    @Override
    public MomentoEvento momento() {
        return momento;
    }

    @Override
    public int avaliar(Reserva reserva, List<Reserva> reservasRelacionadas, RegraTrustScore regraConfigurada,
                       StringBuilder descricaoSaida) {
        long minutosAtraso = Duration.between(reserva.getFimDateTime(), reserva.getDataHoraCheckout()).toMinutes();
        if (minutosAtraso <= TOLERANCIA_MINUTOS) {
            return 0;
        }

        Equipamento equipamento = (Equipamento) Hibernate.unproxy(reserva.getRecurso());
        double fator = equipamento.getSubItens().size() > TAMANHO_KIT_GRANDE ? 1.5 : 1.0;

        int deltaBase = regraConfigurada != null ? regraConfigurada.getValorPrincipal() : deltaPadrao;
        int delta = EventoTrustScore.escalar(deltaBase, fator);
        descricaoSaida.append("Devolução com ").append(minutosAtraso).append(" minutos de atraso")
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
