package imd.ufrn.com.br.smart_space_booking.framework.dto;

import imd.ufrn.com.br.smart_space_booking.framework.enums.CategoriaRegraTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.model.RegraTrustScore;

import java.time.LocalDateTime;

public record RegraTrustScoreResponseDTO(
        Long id,
        CategoriaRegraTrustScore categoria,
        String chave,
        Integer valorPrincipal,
        Integer valorSecundario,
        String descricao,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static RegraTrustScoreResponseDTO fromEntity(RegraTrustScore r) {
        return new RegraTrustScoreResponseDTO(
                r.getId(),
                r.getCategoria(),
                r.getChave(),
                r.getValorPrincipal(),
                r.getValorSecundario(),
                r.getDescricao(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}
