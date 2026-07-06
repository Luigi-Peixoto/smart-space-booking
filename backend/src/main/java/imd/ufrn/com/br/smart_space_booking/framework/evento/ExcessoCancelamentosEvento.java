package imd.ufrn.com.br.smart_space_booking.framework.evento;

import java.util.List;
import java.util.function.ToDoubleFunction;

import imd.ufrn.com.br.smart_space_booking.framework.enums.MomentoEvento;
import imd.ufrn.com.br.smart_space_booking.framework.model.RegraTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.model.Reserva;

/**
 * Penaliza exceder um limite semanal de cancelamentos — pronto pro
 * framework, cada hotspot instancia com seu próprio limite/delta padrão e
 * função de severidade. {reservasRelacionadas} deve trazer os
 * cancelamentos recentes do usuário — o próprio evento conta quantos são.
 */
public class ExcessoCancelamentosEvento implements EventoTrustScore {

    private final String chave;
    private final long limitePadrao;
    private final int deltaPadrao;
    private final ToDoubleFunction<Reserva> fatorSeveridade;
    private final MomentoEvento momento;

    public ExcessoCancelamentosEvento(String chave, long limitePadrao, int deltaPadrao,
                                      ToDoubleFunction<Reserva> fatorSeveridade, MomentoEvento momento) {
        this.chave = chave;
        this.limitePadrao = limitePadrao;
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
        return "Penalidade por exceder um limite semanal de cancelamentos.";
    }

    @Override
    public MomentoEvento momento() {
        return momento;
    }

    @Override
    public int avaliar(Reserva reserva, List<Reserva> reservasRelacionadas, RegraTrustScore regraConfigurada,
                       StringBuilder descricaoSaida) {
        long limite = regraConfigurada != null && regraConfigurada.getValorSecundario() != null
                ? regraConfigurada.getValorSecundario() : limitePadrao;
        long cancelamentosNaSemana = reservasRelacionadas != null ? reservasRelacionadas.size() : 0;
        if (cancelamentosNaSemana <= limite) {
            return 0;
        }

        int deltaBase = regraConfigurada != null ? regraConfigurada.getValorPrincipal() : deltaPadrao;
        int delta = EventoTrustScore.escalar(deltaBase, fatorSeveridade.applyAsDouble(reserva));
        descricaoSaida.append("Excesso de cancelamentos na semana (").append(cancelamentosNaSemana)
                .append(" cancelamentos, limite ").append(limite).append(")")
                .append(EventoTrustScore.sufixoAjuste(deltaBase, delta)).append(".");
        return delta;
    }

    @Override
    public int deltaPadrao() {
        return deltaPadrao;
    }

    @Override
    public Integer parametroPadrao() {
        return (int) limitePadrao;
    }
}
