package imd.ufrn.com.br.smart_space_booking.repository;

import imd.ufrn.com.br.smart_space_booking.model.TrustScoreHistorico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrustScoreHistoricoRepository extends JpaRepository<TrustScoreHistorico, Long> {

    List<TrustScoreHistorico> findByUsuarioIdOrderByCriadoEmDesc(Long usuarioId);
}
