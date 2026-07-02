package imd.ufrn.com.br.smart_space_booking.instancia_sala.service;

import imd.ufrn.com.br.smart_space_booking.instancia_sala.dto.SalaResponseDTO;
import imd.ufrn.com.br.smart_space_booking.instancia_sala.exception.SalaNotFoundException;
import imd.ufrn.com.br.smart_space_booking.framework.service.RecursoService;
import imd.ufrn.com.br.smart_space_booking.instancia_sala.model.Sala;
import imd.ufrn.com.br.smart_space_booking.instancia_sala.repository.SalaRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class SalaService extends RecursoService<Sala, SalaResponseDTO> {

    private final SalaRepository salaRepository;

    public SalaService(SalaRepository salaRepository) {
        this.salaRepository = salaRepository;
    }

    @Override
    protected JpaRepository<Sala, Long> getRepository() {
        return salaRepository;
    }

    @Override
    protected SalaResponseDTO convertToDTO(Sala sala) {
        return new SalaResponseDTO(
                sala.getId(),
                sala.getNome(),
                sala.getLocal(),
                sala.getCapacidade(),
                sala.getTipoSala().toString(),
                sala.getStatus().toString(),
                sala.getCaracteristicas(),
                sala.getImagens());
    }

    @Override
    protected Sala atualizarCampos(Sala existente, Sala dadosNovos) {
        existente.setNome(dadosNovos.getNome());
        existente.setCapacidade(dadosNovos.getCapacidade());
        existente.setLocal(dadosNovos.getLocal());
        existente.setStatus(dadosNovos.getStatus());
        existente.setTipoSala(dadosNovos.getTipoSala());
        existente.setCaracteristicas(dadosNovos.getCaracteristicas());
        existente.setImagens(dadosNovos.getImagens());
        return existente;
    }

    @Override
    protected RuntimeException notFoundException(Long id) {
        return new SalaNotFoundException(id);
    }
}