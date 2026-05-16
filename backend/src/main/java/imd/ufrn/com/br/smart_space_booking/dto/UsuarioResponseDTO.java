package imd.ufrn.com.br.smart_space_booking.dto;

public record UsuarioResponseDTO(Long id, String email, String nome, Integer trustScore, String perfil, String status) {
}