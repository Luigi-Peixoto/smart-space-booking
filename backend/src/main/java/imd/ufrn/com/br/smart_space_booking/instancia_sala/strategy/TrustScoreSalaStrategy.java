package imd.ufrn.com.br.smart_space_booking.instancia_sala.strategy;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

import imd.ufrn.com.br.smart_space_booking.framework.enums.MomentoEvento;
import imd.ufrn.com.br.smart_space_booking.framework.enums.NivelExigencia;
import imd.ufrn.com.br.smart_space_booking.framework.evento.EventoTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.model.Recurso;
import imd.ufrn.com.br.smart_space_booking.framework.model.Reserva;
import imd.ufrn.com.br.smart_space_booking.framework.restricao.LimiteConcorrenciaRestricao;
import imd.ufrn.com.br.smart_space_booking.framework.restricao.RestricaoTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.strategy.TrustScoreStrategy;
import imd.ufrn.com.br.smart_space_booking.instancia_sala.model.Sala;

/**
 * Regra de negócio própria da Sala: cancelar/faltar numa sala grande prejudica
 * mais gente do que numa pequena (uma reunião de 20 pessoas desmarcada de
 * última hora custa mais caro pra comunidade do que uma sala de estudo pra 2).
 * A penalidade escala com a capacidade, em vez de ser um número fixo.
 */
@Component
public class TrustScoreSalaStrategy implements TrustScoreStrategy {

    private static final int CAPACIDADE_AUDITORIO = 20;
    private static final int CAPACIDADE_SALA_REUNIAO = 8;

    @Override
    public boolean suporta(Recurso recurso) {
        return Hibernate.unproxy(recurso) instanceof Sala;
    }

    @Override
    public String tipoRecurso() {
        return "SALA";
    }

    @Override
    public double fatorSeveridade(Reserva reserva) {
        Sala sala = (Sala) Hibernate.unproxy(reserva.getRecurso());
        Integer capacidade = sala.getCapacidade();
        if (capacidade == null) return 1.0;

        if (capacidade > CAPACIDADE_AUDITORIO) return 2.0; // auditório/sala grande: afeta muita gente
        if (capacidade > CAPACIDADE_SALA_REUNIAO) return 1.5; // sala de reunião média
        return 1.0; // sala pequena/individual
    }

    @Override
    public List<RestricaoTrustScore> restricoes() {
        return List.of(
                new LimiteConcorrenciaRestricao(tipoRecurso(), 2, 60),
                new RestricaoSemArCondicionado(tipoRecurso() + ":SEM_AR_CONDICIONADO", 40)
        );
    }

    @Override
    public List<EventoTrustScore> eventos() {
        List<EventoTrustScore> eventos = new ArrayList<>(TrustScoreStrategy.super.eventos());
        eventos.add(new CheckinAtrasadoEvento(tipoRecurso() + ":CHECKIN_ATRASADO", -5, MomentoEvento.CHECKIN));
        eventos.add(new CancelamentoMesmoDiaEvento(tipoRecurso() + ":CANCELAMENTO_MESMO_DIA", -8, MomentoEvento.CANCELAMENTO));
        return eventos;
    }

    /**
     * Descreve, com os mesmos limiares usados em {@link #fatorSeveridade}, o
     * critério de capacidade que classifica uma sala em cada nível — mantém a
     * UI admin sempre alinhada com o código, sem duplicar os números à mão.
     */
    @Override
    public String descricaoNivelExigencia(NivelExigencia nivel) {
        return switch (nivel) {
            case ALTA -> "Salas com capacidade acima de " + CAPACIDADE_AUDITORIO + " pessoas (auditórios/salas grandes).";
            case MEDIA -> "Salas com capacidade entre " + (CAPACIDADE_SALA_REUNIAO + 1) + " e " + CAPACIDADE_AUDITORIO + " pessoas (salas de reunião).";
            case BAIXA -> "Salas com capacidade até " + CAPACIDADE_SALA_REUNIAO + " pessoas (salas pequenas/individuais).";
        };
    }
}
