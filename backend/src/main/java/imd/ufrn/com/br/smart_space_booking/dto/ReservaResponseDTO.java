package imd.ufrn.com.br.smart_space_booking.dto;

import java.time.ZonedDateTime;

import imd.ufrn.com.br.smart_space_booking.enums.ReservaStatus;
import imd.ufrn.com.br.smart_space_booking.enums.ReservaTipo;
import imd.ufrn.com.br.smart_space_booking.model.Reserva;

public record ReservaResponseDTO(
        Long id,
        ZonedDateTime inicioDateTime,
        ZonedDateTime fimDateTime,
        ReservaStatus status,
        ReservaTipo tipo,
        Long usuarioId,
        Long recursoId,
        String recursoNome,
        ZonedDateTime dataHoraCheckin,
        ZonedDateTime dataHoraCheckout,
        String motivoCancelamento,
        ZonedDateTime dataHoraCancelamento,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public static ReservaResponseDTO fromEntity(Reserva reserva) {
        return new ReservaResponseDTO(
                reserva.getId(),
                reserva.getInicioDateTime(),
                reserva.getFimDateTime(),
                reserva.getStatus(),
                reserva.getTipo(),
                reserva.getUsuario() != null ? reserva.getUsuario().getId() : null,
                reserva.getRecurso().getId(),
                reserva.getRecurso().getNome(),
                reserva.getDataHoraCheckin(),
                reserva.getDataHoraCheckout(),
                reserva.getMotivoCancelamento(),
                reserva.getDataHoraCancelamento(),
                reserva.getCreatedAt(),
                reserva.getUpdatedAt()
        );
    }
}