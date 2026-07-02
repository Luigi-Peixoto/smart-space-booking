package imd.ufrn.com.br.smart_space_booking.framework.repository;

import imd.ufrn.com.br.smart_space_booking.framework.model.Incidente;
import imd.ufrn.com.br.smart_space_booking.framework.enums.IncidenteStatus;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidenteRepository extends JpaRepository<Incidente, Long> {
    List<Incidente> findByStatus(IncidenteStatus status);
}
