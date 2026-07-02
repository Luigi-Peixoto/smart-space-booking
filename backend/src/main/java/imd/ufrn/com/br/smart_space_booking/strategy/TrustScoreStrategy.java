package imd.ufrn.com.br.smart_space_booking.strategy;

import imd.ufrn.com.br.smart_space_booking.enums.TrustScoreEvento;
import imd.ufrn.com.br.smart_space_booking.model.Recurso;

/**
 * Ponto de extensão do framework para as regras de TrustScore de um hotspot
 * (Sala, e futuramente Equipamento/Veículo). Mantida pura — sem acesso a
 * repository/service — para permanecer testável e não acoplar decisão de
 * negócio a infraestrutura de persistência.
 */
public interface TrustScoreStrategy {

    /** O resolver usa isso para escolher a estratégia certa pelo tipo do recurso. */
    boolean suporta(Recurso recurso);

    /** Decide se/quanto o TrustScore deve variar para o evento e contexto informados. */
    TrustScoreDecisao avaliar(TrustScoreEvento evento, TrustScoreContexto contexto);
}
