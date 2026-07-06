package imd.ufrn.com.br.smart_space_booking.framework.enums;

/**
 * O que um registro de TrustScoreHistorico representa: uma mudança de
 * pontuação de fato, ou uma tentativa de reserva barrada (que não muda o
 * score, mas precisa ficar registrada).
 */
public enum TrustScoreHistoricoTipo {
    PENALIDADE,
    BLOQUEIO
}
