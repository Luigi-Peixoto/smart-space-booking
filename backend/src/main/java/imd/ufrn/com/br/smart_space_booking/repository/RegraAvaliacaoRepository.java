package imd.ufrn.com.br.smart_space_booking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import imd.ufrn.com.br.smart_space_booking.model.RegraAvaliacao;

public interface RegraAvaliacaoRepository extends JpaRepository<RegraAvaliacao, Long> {
    List<RegraAvaliacao> findAllByOrderByIdAsc();
    Optional<RegraAvaliacao> findByNomeIgnoreCase(String nome);
}