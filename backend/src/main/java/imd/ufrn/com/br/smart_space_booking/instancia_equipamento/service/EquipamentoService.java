package imd.ufrn.com.br.smart_space_booking.instancia_equipamento.service;

import imd.ufrn.com.br.smart_space_booking.framework.service.RecursoService;
import imd.ufrn.com.br.smart_space_booking.instancia_equipamento.dto.EquipamentoResponseDTO;
import imd.ufrn.com.br.smart_space_booking.instancia_equipamento.exception.EquipamentoNotFoundException;
import imd.ufrn.com.br.smart_space_booking.instancia_equipamento.model.Equipamento;
import imd.ufrn.com.br.smart_space_booking.instancia_equipamento.repository.EquipamentoRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class EquipamentoService extends RecursoService<Equipamento, EquipamentoResponseDTO> {

    private final EquipamentoRepository equipamentoRepository;

    public EquipamentoService(EquipamentoRepository equipamentoRepository) {
        this.equipamentoRepository = equipamentoRepository;
    }

    @Override
    protected JpaRepository<Equipamento, Long> getRepository() {
        return equipamentoRepository;
    }

    @Override
    protected EquipamentoResponseDTO convertToDTO(Equipamento equipamento) {
        return EquipamentoResponseDTO.fromEntity(equipamento);
    }

    @Override
    protected Equipamento atualizarCampos(Equipamento existente, Equipamento dadosNovos) {
        existente.setNome(dadosNovos.getNome());
        existente.setStatus(dadosNovos.getStatus());
        existente.setNumeroSerie(dadosNovos.getNumeroSerie());
        existente.setMarca(dadosNovos.getMarca());
        existente.setModelo(dadosNovos.getModelo());
        existente.setTipo(dadosNovos.getTipo());
        existente.setImagens(dadosNovos.getImagens());
        // subItens: orphanRemoval cuida da limpeza dos removidos
        existente.getSubItens().clear();
        existente.getSubItens().addAll(dadosNovos.getSubItens());
        return existente;
    }

    @Override
    protected RuntimeException notFoundException(Long id) {
        return new EquipamentoNotFoundException(id);
    }
}