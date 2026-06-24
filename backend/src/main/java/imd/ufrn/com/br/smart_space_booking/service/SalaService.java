package imd.ufrn.com.br.smart_space_booking.service;

import imd.ufrn.com.br.smart_space_booking.dto.SalaResponseDTO;
import imd.ufrn.com.br.smart_space_booking.exception.SalaNotFoundException;
import imd.ufrn.com.br.smart_space_booking.model.Sala;
import imd.ufrn.com.br.smart_space_booking.repository.SalaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SalaService implements RecursoService<Sala, SalaResponseDTO> {

    private final SalaRepository salaRepository;

    public SalaService(SalaRepository salaRepository) {
        this.salaRepository = salaRepository;
    }

    @Override
    public List<SalaResponseDTO> listarTodos() {
        return salaRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    public Optional<SalaResponseDTO> buscarPorId(Long id) {
        return salaRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Override
    public SalaResponseDTO salvar(Sala sala) {
        return convertToDTO(salaRepository.save(sala));
    }

    @Override
    public SalaResponseDTO atualizar(Long id, Sala dadosNovos) {
        return salaRepository.findById(id).map(salaExistente -> {
            salaExistente.setNome(dadosNovos.getNome());
            salaExistente.setCapacidade(dadosNovos.getCapacidade());
            salaExistente.setLocal(dadosNovos.getLocal());
            salaExistente.setStatus(dadosNovos.getStatus());
            salaExistente.setTipoSala(dadosNovos.getTipoSala());
            salaExistente.setCaracteristicas(dadosNovos.getCaracteristicas());
            salaExistente.setImagens(dadosNovos.getImagens());
            return convertToDTO(salaRepository.save(salaExistente));
        }).orElseThrow(() -> new SalaNotFoundException(id));
    }

    @Override
    public boolean deletar(Long id) {
        return salaRepository.findById(id).map(sala -> {
            salaRepository.delete(sala);
            return true;
        }).orElse(false);
    }

    private SalaResponseDTO convertToDTO(Sala sala) {
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
}