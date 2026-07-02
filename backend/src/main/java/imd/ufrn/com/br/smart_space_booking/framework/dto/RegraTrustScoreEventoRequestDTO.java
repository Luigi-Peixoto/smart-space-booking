package imd.ufrn.com.br.smart_space_booking.framework.dto;

import imd.ufrn.com.br.smart_space_booking.framework.enums.TrustScoreEvento;

public record RegraTrustScoreEventoRequestDTO(
        TrustScoreEvento evento,
        Integer delta,
        Integer parametro,
        String descricao
) {}
