package imd.ufrn.com.br.smart_space_booking.repository;

import imd.ufrn.com.br.smart_space_booking.model.Equipamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipamentoRepository extends JpaRepository<Equipamento, Long> {
}