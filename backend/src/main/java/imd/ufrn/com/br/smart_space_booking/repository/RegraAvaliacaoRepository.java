package imd.ufrn.com.br.smart_space_booking.repository;

import imd.ufrn.com.br.smart_space_booking.model.RegraAvaliacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegraAvaliacaoRepository extends JpaRepository<RegraAvaliacao, Long> {
    List<RegraAvaliacao> findAllByOrderByIdAsc();
}