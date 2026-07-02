package imd.ufrn.com.br.smart_space_booking.dto;

import imd.ufrn.com.br.smart_space_booking.enums.TrustScoreEvento;
import imd.ufrn.com.br.smart_space_booking.model.RegraTrustScoreEvento;

import java.time.LocalDateTime;

public record RegraTrustScoreEventoResponseDTO(
        Long id,
        TrustScoreEvento evento,
        Integer delta,
        Integer parametro,
        String descricao,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static RegraTrustScoreEventoResponseDTO fromEntity(RegraTrustScoreEvento r) {
        return new RegraTrustScoreEventoResponseDTO(
                r.getId(),
                r.getEvento(),
                r.getDelta(),
                r.getParametro(),
                r.getDescricao(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}
