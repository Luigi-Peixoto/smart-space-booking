package imd.ufrn.com.br.smart_space_booking.framework.dto;

import imd.ufrn.com.br.smart_space_booking.framework.model.RegraAvaliacao;

import java.time.LocalDateTime;

public record RegraAvaliacaoResponseDTO(
        Long id,
        String nome,
        String descricao,
        Integer limiBonus,
        Integer deltaBonus,
        Integer limiPenalidade,
        Integer deltaPenalidade,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static RegraAvaliacaoResponseDTO fromEntity(RegraAvaliacao r) {
        return new RegraAvaliacaoResponseDTO(
                r.getId(),
                r.getNome(),
                r.getDescricao(),
                r.getLimiBonus(),
                r.getDeltaBonus(),
                r.getLimiPenalidade(),
                r.getDeltaPenalidade(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}
