package imd.ufrn.com.br.smart_space_booking.service;

import imd.ufrn.com.br.smart_space_booking.model.Recurso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public abstract class RecursoService<R extends Recurso, DTO> {

    protected abstract JpaRepository<R, Long> getRepository();
    protected abstract DTO convertToDTO(R recurso);
    protected abstract R atualizarCampos(R existente, R dadosNovos);
    protected abstract RuntimeException notFoundException(Long id);

    public List<DTO> listarTodos() {
        return getRepository().findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public Optional<DTO> buscarPorId(Long id) {
        return getRepository().findById(id)
                .map(this::convertToDTO);
    }

    public DTO salvar(R recurso) {
        return convertToDTO(getRepository().save(recurso));
    }

    public DTO atualizar(Long id, R dadosNovos) {
        return getRepository().findById(id).map(existente -> {
            R atualizado = atualizarCampos(existente, dadosNovos);
            return convertToDTO(getRepository().save(atualizado));
        }).orElseThrow(() -> notFoundException(id));
    }

    public boolean deletar(Long id) {
        return getRepository().findById(id).map(recurso -> {
            getRepository().delete(recurso);
            return true;
        }).orElse(false);
    }
}