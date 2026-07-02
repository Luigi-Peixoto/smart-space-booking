package imd.ufrn.com.br.smart_space_booking.framework.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import imd.ufrn.com.br.smart_space_booking.framework.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
}