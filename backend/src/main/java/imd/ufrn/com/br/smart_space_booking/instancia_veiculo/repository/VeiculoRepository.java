package imd.ufrn.com.br.smart_space_booking.instancia_veiculo.repository;

import imd.ufrn.com.br.smart_space_booking.instancia_veiculo.model.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {
}