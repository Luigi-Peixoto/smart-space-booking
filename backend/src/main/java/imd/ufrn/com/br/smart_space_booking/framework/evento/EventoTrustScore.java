package imd.ufrn.com.br.smart_space_booking.framework.evento;

import java.util.List;

import imd.ufrn.com.br.smart_space_booking.framework.enums.MomentoEvento;
import imd.ufrn.com.br.smart_space_booking.framework.model.RegraTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.model.Reserva;

/**
 * Interface que todo evento de TrustScore deve implementar — cada evento
 * representa uma penalidade que pode ser aplicada a uma reserva, e cada
 * implementação deve declarar a sua própria chave, descrição, momento do
 * ciclo de vida da reserva em que é avaliado e função de severidade do recurso.
 * 
 */
public interface EventoTrustScore {

    String chave();

    String descricao();

    /**
     * Ponto do ciclo de vida em que este evento deve ser avaliado — toda
     * implementação recebe o seu no construtor (não há padrão implícito),
     * pra ficar visível no ponto de instanciação qual momento cada evento usa.
     */
    MomentoEvento momento();

    /**
     * Avalia a penalidade deste evento para a reserva, considerando as reservas relacionadas
     * e a regra de TrustScore configurada pelo admin. Retorna o delta de TrustScore a ser aplicado.
     * 
     */
    int avaliar(Reserva reserva, List<Reserva> reservasRelacionadas, RegraTrustScore regraConfigurada,
                StringBuilder descricaoSaida);

    /** Delta padrão (usado quando o admin não configurou uma RegraTrustScore) — exibido na UI admin como sugestão. */
    int deltaPadrao();

    /**
     * Parâmetro adicional padrão (janela em horas, limite de contagem, etc.) —
     * {@code null} se este evento não usa um segundo número. A UI admin usa
     * isso pra desabilitar o campo em eventos que não têm parâmetro.
     */
    Integer parametroPadrao();

    /** Escala o delta base pela severidade do recurso — 1.0 significa "sem ajuste". */
    static int escalar(int deltaBase, double fatorSeveridade) {
        return (int) Math.round(deltaBase * fatorSeveridade);
    }

    /** Sufixo explicando o ajuste de severidade na descrição, quando o fator alterou o delta base. */
    static String sufixoAjuste(int deltaBase, int deltaEscalado) {
        return deltaEscalado != deltaBase ? " [severidade do recurso: " + deltaBase + " → " + deltaEscalado + "]" : "";
    }
}
