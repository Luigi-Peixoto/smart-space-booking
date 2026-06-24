package imd.ufrn.com.br.smart_space_booking.dto;

import imd.ufrn.com.br.smart_space_booking.enums.ReservaTipo;

import java.time.ZonedDateTime;

public record ReservaRequestDTO(
        ZonedDateTime inicioDateTime,
        ZonedDateTime fimDateTime,
        ReservaTipo tipo,
        Long usuarioId,
        Long recursoId
) {}