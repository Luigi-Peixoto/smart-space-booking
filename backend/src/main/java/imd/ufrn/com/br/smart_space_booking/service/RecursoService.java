package imd.ufrn.com.br.smart_space_booking.service;

import java.util.List;
import java.util.Optional;

public interface RecursoService<R, DTO> {

    List<DTO> listarTodos();

    Optional<DTO> buscarPorId(Long id);

    DTO salvar(R recurso);

    DTO atualizar(Long id, R dadosNovos);

    boolean deletar(Long id);
}