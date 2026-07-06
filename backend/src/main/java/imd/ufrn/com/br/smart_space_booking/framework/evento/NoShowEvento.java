package imd.ufrn.com.br.smart_space_booking.framework.evento;

import java.util.List;
import java.util.function.ToDoubleFunction;

import imd.ufrn.com.br.smart_space_booking.framework.enums.MomentoEvento;
import imd.ufrn.com.br.smart_space_booking.framework.model.RegraTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.model.Reserva;

/**
 * Penaliza faltar a uma reserva sem cancelar (no-show) — pronto pro
 * framework, cada hotspot instancia com seu próprio delta padrão e função de
 * severidade.
 */
public class NoShowEvento implements EventoTrustScore {

    private final String chave;
    private final int deltaPadrao;
    private final ToDoubleFunction<Reserva> fatorSeveridade;
    private final MomentoEvento momento;

    public NoShowEvento(String chave, int deltaPadrao, ToDoubleFunction<Reserva> fatorSeveridade, MomentoEvento momento) {
        this.chave = chave;
        this.deltaPadrao = deltaPadrao;
        this.fatorSeveridade = fatorSeveridade;
        this.momento = momento;
    }

    @Override
    public String chave() {
        return chave;
    }

    @Override
    public String descricao() {
        return "Penalidade por faltar a uma reserva sem cancelar (no-show).";
    }

    @Override
    public MomentoEvento momento() {
        return momento;
    }

    @Override
    public int avaliar(Reserva reserva, List<Reserva> reservasRelacionadas, RegraTrustScore regraConfigurada,
                       StringBuilder descricaoSaida) {
        int deltaBase = regraConfigurada != null ? regraConfigurada.getValorPrincipal() : deltaPadrao;
        int delta = EventoTrustScore.escalar(deltaBase, fatorSeveridade.applyAsDouble(reserva));
        descricaoSaida.append("Reserva cancelada automaticamente por no-show")
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
