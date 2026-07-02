package imd.ufrn.com.br.smart_space_booking.instancia_sala.repository;

import imd.ufrn.com.br.smart_space_booking.instancia_sala.model.Sala;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalaRepository extends JpaRepository<Sala, Long> {
}
