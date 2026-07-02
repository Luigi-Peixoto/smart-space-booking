package imd.ufrn.com.br.smart_space_booking.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import imd.ufrn.com.br.smart_space_booking.enums.TrustScoreEvento;
import imd.ufrn.com.br.smart_space_booking.model.RegraTrustScoreEvento;

public interface RegraTrustScoreEventoRepository extends JpaRepository<RegraTrustScoreEvento, Long> {
    Optional<RegraTrustScoreEvento> findByEvento(TrustScoreEvento evento);
}
