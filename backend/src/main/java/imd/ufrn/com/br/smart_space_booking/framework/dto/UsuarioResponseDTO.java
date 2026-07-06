package imd.ufrn.com.br.smart_space_booking.framework.dto;

import imd.ufrn.com.br.smart_space_booking.framework.enums.NivelExigencia;

public record UsuarioResponseDTO(Long id, String email, String nome, Integer trustScore,
                                  NivelExigencia nivelRestricao, String perfil, String status) {
}