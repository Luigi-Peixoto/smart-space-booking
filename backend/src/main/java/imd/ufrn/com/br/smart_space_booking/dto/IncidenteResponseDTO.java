// IncidenteResponseDTO.java
package imd.ufrn.com.br.smart_space_booking.dto;

import java.time.LocalDateTime;

import imd.ufrn.com.br.smart_space_booking.enums.IncidenteStatus;
import imd.ufrn.com.br.smart_space_booking.model.Incidente;

public record IncidenteResponseDTO(
        Long id,
        Long recursoId,
        String recursoNome,
        Long usuarioId,
        String usuarioNome,
        String usuarioEmail,
        String descricao,
        IncidenteStatus status,
        LocalDateTime dataReporte
) {
    public static IncidenteResponseDTO fromEntity(Incidente i) {
        return new IncidenteResponseDTO(
                i.getId(),
                i.getRecurso().getId(),
                i.getRecurso().getNome(),
                i.getUsuario().getId(),
                i.getUsuario().getNome(),
                i.getUsuario().getEmail(),
                i.getDescricao(),
                i.getStatus(),
                i.getDataReporte()
        );
    }
}