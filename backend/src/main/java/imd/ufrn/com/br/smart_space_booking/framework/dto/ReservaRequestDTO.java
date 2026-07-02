package imd.ufrn.com.br.smart_space_booking.framework.dto;

import imd.ufrn.com.br.smart_space_booking.framework.enums.ReservaTipo;

import java.time.ZonedDateTime;

public record ReservaRequestDTO(
        ZonedDateTime inicioDateTime,
        ZonedDateTime fimDateTime,
        ReservaTipo tipo,
        Long usuarioId,
        Long recursoId
) {}