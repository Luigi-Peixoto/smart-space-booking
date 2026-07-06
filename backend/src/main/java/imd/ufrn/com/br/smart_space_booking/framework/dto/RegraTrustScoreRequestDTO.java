package imd.ufrn.com.br.smart_space_booking.framework.dto;

import imd.ufrn.com.br.smart_space_booking.framework.enums.CategoriaRegraTrustScore;

public record RegraTrustScoreRequestDTO(
        CategoriaRegraTrustScore categoria,
        String chave,
        Integer valorPrincipal,
        Integer valorSecundario,
        String descricao
) {}
