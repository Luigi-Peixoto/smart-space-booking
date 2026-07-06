package imd.ufrn.com.br.smart_space_booking.instancia_sala.strategy;

import java.time.ZonedDateTime;
import java.util.List;

import imd.ufrn.com.br.smart_space_booking.framework.enums.MomentoEvento;
import imd.ufrn.com.br.smart_space_booking.framework.evento.EventoTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.model.RegraTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.model.Reserva;

/**
 * Evento da Sala: penaliza cancelar mais de uma reserva da
 * MESMA sala no MESMO DIA — janela (dia, não semana) e escopo (um recurso
 * específico, não qualquer um) diferentes do EXCESSO_CANCELAMENTOS pronto do
 * framework. Reaproveita o mesmo momento (CANCELAMENTO) e a mesma lista de
 * cancelamentos recentes que o framework já busca, só filtra diferente.
 */
public class CancelamentoMesmoDiaEvento implements EventoTrustScore {

    private final String chave;
    private final int deltaPadrao;
    private final MomentoEvento momento;

    public CancelamentoMesmoDiaEvento(String chave, int deltaPadrao, MomentoEvento momento) {
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
        return "Penalidade por cancelar mais de uma reserva da mesma sala no mesmo dia.";
    }

    @Override
    public MomentoEvento momento() {
        return momento;
    }

    @Override
    public int avaliar(Reserva reserva, List<Reserva> reservasRelacionadas, RegraTrustScore regraConfigurada,
                       StringBuilder descricaoSaida) {
        ZonedDateTime hoje = ZonedDateTime.now();
        long cancelamentosHojeMesmaSala = reservasRelacionadas.stream()
                .filter(r -> r.getRecurso().getId().equals(reserva.getRecurso().getId()))
                .filter(r -> r.getDataHoraCancelamento() != null
                        && r.getDataHoraCancelamento().toLocalDate().equals(hoje.toLocalDate()))
                .count();
        if (cancelamentosHojeMesmaSala < 2) {
            return 0;
        }

        int deltaBase = regraConfigurada != null ? regraConfigurada.getValorPrincipal() : deltaPadrao;
        descricaoSaida.append("Mais de uma reserva desta sala cancelada hoje (")
                .append(cancelamentosHojeMesmaSala).append(").");
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
