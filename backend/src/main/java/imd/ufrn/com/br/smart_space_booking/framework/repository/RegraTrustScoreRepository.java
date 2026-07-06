package imd.ufrn.com.br.smart_space_booking.framework.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import imd.ufrn.com.br.smart_space_booking.framework.enums.CategoriaRegraTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.model.RegraTrustScore;

public interface RegraTrustScoreRepository extends JpaRepository<RegraTrustScore, Long> {
    Optional<RegraTrustScore> findByCategoriaAndChave(CategoriaRegraTrustScore categoria, String chave);
    List<RegraTrustScore> findByCategoria(CategoriaRegraTrustScore categoria);
}
