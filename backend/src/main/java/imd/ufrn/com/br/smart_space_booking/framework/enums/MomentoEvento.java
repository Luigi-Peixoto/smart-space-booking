package imd.ufrn.com.br.smart_space_booking.framework.enums;

/**
 * Momento do evento, usado pra diferenciar o mesmo tipo de evento (ex: CHECKOUT)
 * em diferentes pontos do fluxo de vida da reserva (ex: CHECKOUT de devolução
 * vs CHECKOUT de cancelamento). Cada evento do framework ou hotspot deve declarar
 * o momento que ele representa, pra que o TrustScoreStrategy possa agrupar e
 * avaliar corretamente.
 */
public enum MomentoEvento {
    CANCELAMENTO,
    NO_SHOW,
    CHECKIN,
    CHECKOUT
}
