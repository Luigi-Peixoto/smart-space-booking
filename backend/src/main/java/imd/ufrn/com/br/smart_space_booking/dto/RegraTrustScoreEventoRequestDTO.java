package imd.ufrn.com.br.smart_space_booking.dto;

import imd.ufrn.com.br.smart_space_booking.enums.TrustScoreEvento;

public record RegraTrustScoreEventoRequestDTO(
        TrustScoreEvento evento,
        Integer delta,
        Integer parametro,
        String descricao
) {}
