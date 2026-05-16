package imd.ufrn.com.br.smart_space_booking.dto;

public record UsuarioRequestDTO(
        String email,
        String nome,
        String perfil,
        Integer trustScore,
        String status
) {
}