package imd.ufrn.com.br.smart_space_booking.framework.strategy;

import java.util.List;

import imd.ufrn.com.br.smart_space_booking.framework.enums.MomentoEvento;
import imd.ufrn.com.br.smart_space_booking.framework.enums.NivelExigencia;
import imd.ufrn.com.br.smart_space_booking.framework.evento.CancelamentoTardioEvento;
import imd.ufrn.com.br.smart_space_booking.framework.evento.EventoTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.evento.ExcessoCancelamentosEvento;
import imd.ufrn.com.br.smart_space_booking.framework.evento.NoShowEvento;
import imd.ufrn.com.br.smart_space_booking.framework.model.Recurso;
import imd.ufrn.com.br.smart_space_booking.framework.model.Reserva;
import imd.ufrn.com.br.smart_space_booking.framework.restricao.RestricaoTrustScore;

/**
 * Interface que define a estratégia de cálculo do TrustScore para diferentes tipos de recursos.
 * Cada hotspot (instância de recurso) deve implementar essa interface para fornecer
 * eventos e restrições específicas, bem como a lógica de severidade e exigência mínima.
 * O framework fornece implementações padrão para eventos comuns, mas cada hotspot
 * pode personalizar a lógica de acordo com suas necessidades.
 */
public interface TrustScoreStrategy {

    boolean suporta(Recurso recurso);

    String tipoRecurso();

    /**
     * Lista de eventos de TrustScore que este hotspot quer usar — cada evento
     * representa uma penalidade que pode ser aplicada a uma reserva. O delta
     * de cada evento é ajustado pelo fatorSeveridade do recurso, que é o ponto de extensão do hotspot.
     * 
     */
    default List<EventoTrustScore> eventos() {
        return List.of(
                new CancelamentoTardioEvento("CANCELAMENTO_TARDIO", janelaCancelamentoPadrao(), -10,
                        this::fatorSeveridade, MomentoEvento.CANCELAMENTO),
                new NoShowEvento("NO_SHOW", -15,
                        this::fatorSeveridade, MomentoEvento.NO_SHOW),
                new ExcessoCancelamentosEvento("EXCESSO_CANCELAMENTOS", limiteCancelamentosPadrao(), -20,
                        this::fatorSeveridade, MomentoEvento.CANCELAMENTO)
        );
    }

    
    default List<RestricaoTrustScore> restricoes() {
        return List.of();
    }

    /**
     * Escala o delta base de cada evento pela severidade do recurso — 1.0 significa "sem ajuste". 
     * Cada hotspot decide como quer medir a severidade do seu recurso, e o framework aplica isso a todos os eventos.
     * 
     */
    default double fatorSeveridade(Reserva reserva) {
        return 1.0;
    }

    /**
     * Decide o nível de exigência do recurso, que determina o TrustScore mínimo pra reservar.
     * O framework fornece um fallback genérico baseado no fatorSeveridade, mas cada hotspot pode sobrescrever isso com a sua própria lógica de severidade.
     */
    default NivelExigencia nivelExigencia(Reserva reserva) {
        double fator = fatorSeveridade(reserva);
        if (fator >= 2.0) return NivelExigencia.ALTA;
        if (fator >= 1.5) return NivelExigencia.MEDIA;
        return NivelExigencia.BAIXA;
    }

    // ─── Fallbacks — usados só quando o admin não configurou uma RegraTrustScore.
    // Cada hotspot pode sobrescrever o seu próprio, em vez de herdar um único
    // valor global do framework — mais um item que o hotspot pode variar.

    /** Janela de antecedência (horas) do CANCELAMENTO_TARDIO quando não configurada. */
    default long janelaCancelamentoPadrao() {
        return 2L;
    }

    /** Limite semanal de cancelamentos do EXCESSO_CANCELAMENTOS quando não configurado. */
    default long limiteCancelamentosPadrao() {
        return 3L;
    }

    /** TrustScore mínimo pra reservar um recurso desse nível quando não configurado. */
    default int scoreMinimoPadrao(NivelExigencia nivel) {
        return nivel.scoreMinimoPadrao();
    }

    /**
     * Descreve, com os mesmos limiares usados em {@link #nivelExigencia}, 
     * o critério que classifica um recurso em cada nível — 
     * mantém a UI admin sempre alinhada com o código, sem duplicar os números à mão.
     */
    default String descricaoNivelExigencia(NivelExigencia nivel) {
        return "Classificação automática baseada na sensibilidade do recurso.";
    }
}
