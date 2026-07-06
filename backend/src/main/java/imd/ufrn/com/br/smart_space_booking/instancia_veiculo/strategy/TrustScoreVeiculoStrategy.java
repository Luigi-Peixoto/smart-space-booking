package imd.ufrn.com.br.smart_space_booking.instancia_veiculo.strategy;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import imd.ufrn.com.br.smart_space_booking.framework.enums.MomentoEvento;
import imd.ufrn.com.br.smart_space_booking.framework.evento.EventoTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.model.Recurso;
import imd.ufrn.com.br.smart_space_booking.framework.model.Reserva;
import imd.ufrn.com.br.smart_space_booking.framework.restricao.LimiteConcorrenciaRestricao;
import imd.ufrn.com.br.smart_space_booking.framework.restricao.LimiteDuracaoReservaRestricao;
import imd.ufrn.com.br.smart_space_booking.framework.restricao.RestricaoTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.strategy.TrustScoreStrategy;
import imd.ufrn.com.br.smart_space_booking.instancia_veiculo.model.Veiculo;

/**
 * Regra de negócio própria do Veículo: cancelar ou faltar numa reserva de
 * poucas horas é bem menos disruptivo do que numa reserva de vários dias —
 * quem reservou o carro pra uma viagem longa provavelmente já organizou outras
 * coisas em torno dela. A penalidade escala com a duração da própria reserva,
 * não com um número fixo por evento.
 */
@Component
public class TrustScoreVeiculoStrategy implements TrustScoreStrategy {

    private static final long HORAS_VIAGEM_LONGA = 48L;
    private static final long HORAS_DIA_INTEIRO = 12L;

    @Override
    public boolean suporta(Recurso recurso) {
        return recurso instanceof Veiculo;
    }

    @Override
    public String tipoRecurso() {
        return "VEICULO";
    }

    @Override
    public double fatorSeveridade(Reserva reserva) {
        long horasReservadas = ChronoUnit.HOURS.between(reserva.getInicioDateTime(), reserva.getFimDateTime());

        if (horasReservadas >= HORAS_VIAGEM_LONGA) return 2.0; // viagem de dois dias ou mais
        if (horasReservadas >= HORAS_DIA_INTEIRO) return 1.5; // reserva de um dia inteiro
        return 1.0; // poucas horas
    }

    /** Cancelar um carro exige reorganizar logística/combustível/chaves — pede mais antecedência que o padrão do framework. */
    @Override
    public long janelaCancelamentoPadrao() {
        return 6L;
    }

    @Override
    public List<RestricaoTrustScore> restricoes() {
        return List.of(
                new LimiteConcorrenciaRestricao(tipoRecurso(), 1, 60),
                new LimiteDuracaoReservaRestricao(tipoRecurso() + ":DURACAO_MAXIMA", 24L, 50),
                new RestricaoHorarioNoturno(tipoRecurso() + ":HORARIO_NOTURNO", 22, 55)
        );
    }

    @Override
    public List<EventoTrustScore> eventos() {
        List<EventoTrustScore> eventos = new ArrayList<>(TrustScoreStrategy.super.eventos());
        eventos.add(new DevolucaoAtrasadaVeiculoEvento(tipoRecurso() + ":DEVOLUCAO_ATRASADA", -5, MomentoEvento.CHECKOUT));
        eventos.add(new CancelamentoMadrugadaEvento(tipoRecurso() + ":CANCELAMENTO_MADRUGADA", -8, MomentoEvento.CANCELAMENTO));
        return eventos;
    }
}
