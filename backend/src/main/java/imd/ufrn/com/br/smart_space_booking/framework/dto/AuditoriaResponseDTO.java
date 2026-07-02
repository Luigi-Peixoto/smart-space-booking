package imd.ufrn.com.br.smart_space_booking.framework.dto;

import imd.ufrn.com.br.smart_space_booking.framework.enums.AuditoriaTipo;
import imd.ufrn.com.br.smart_space_booking.framework.model.Auditoria;

import java.time.LocalDateTime;
import java.util.List;

public record AuditoriaResponseDTO(
        Long id,
        Long reservaId,
        AuditoriaTipo tipo,
        Boolean aprovado,
        String observacaoGeral,
        List<AvaliacaoCriterioDTO> criterios,
        List<String> imageIds,
        Integer deltaTrustScoreAplicado,
        LocalDateTime dateCreated
) {
    public static AuditoriaResponseDTO fromEntity(Auditoria auditoria) {
        return new AuditoriaResponseDTO(
                auditoria.getId(),
                auditoria.getReserva().getId(),
                auditoria.getTipo(),
                auditoria.getAprovado(),
                auditoria.getObservacaoGeral(),
                auditoria.getCriterios(),
                auditoria.getImageIds(),
                auditoria.getDeltaTrustScoreAplicado(),
                auditoria.getDateCreated()
        );
    }
}
