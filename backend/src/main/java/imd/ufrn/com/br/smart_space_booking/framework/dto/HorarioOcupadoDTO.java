package imd.ufrn.com.br.smart_space_booking.framework.dto;

import imd.ufrn.com.br.smart_space_booking.framework.model.Reserva;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public record HorarioOcupadoDTO(String inicio, String fim, String label) {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    public static HorarioOcupadoDTO fromEntity(Reserva reserva) {
        ZoneId zone = ZoneId.of("America/Fortaleza");
        String inicioStr = reserva.getInicioDateTime().withZoneSameInstant(zone).format(formatter);
        String fimStr = reserva.getFimDateTime().withZoneSameInstant(zone).format(formatter);

        String sufixo = (reserva.getUsuario() == null) ? " (Limpeza)" : " (Ocupado)";

        return new HorarioOcupadoDTO(
                inicioStr,
                fimStr,
                inicioStr + " - " + fimStr + sufixo
        );
    }
}