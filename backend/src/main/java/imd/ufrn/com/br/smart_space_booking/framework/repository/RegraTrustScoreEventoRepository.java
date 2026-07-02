package imd.ufrn.com.br.smart_space_booking.framework.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import imd.ufrn.com.br.smart_space_booking.framework.enums.TrustScoreEvento;
import imd.ufrn.com.br.smart_space_booking.framework.model.RegraTrustScoreEvento;

public interface RegraTrustScoreEventoRepository extends JpaRepository<RegraTrustScoreEvento, Long> {
    Optional<RegraTrustScoreEvento> findByEvento(TrustScoreEvento evento);
}
