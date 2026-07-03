package imd.ufrn.com.br.smart_space_booking.instancia_equipamento.service;

import imd.ufrn.com.br.smart_space_booking.framework.exception.RegraNegocioException;
import imd.ufrn.com.br.smart_space_booking.framework.service.RecursoService;
import imd.ufrn.com.br.smart_space_booking.instancia_equipamento.dto.EquipamentoResponseDTO;
import imd.ufrn.com.br.smart_space_booking.instancia_equipamento.exception.EquipamentoNotFoundException;
import imd.ufrn.com.br.smart_space_booking.instancia_equipamento.model.Equipamento;
import imd.ufrn.com.br.smart_space_booking.instancia_equipamento.model.SubItem;
import imd.ufrn.com.br.smart_space_booking.instancia_equipamento.repository.EquipamentoRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Override
    protected void validarEspecifico(Equipamento equipamento) {
        if (equipamento.getNumeroSerie() == null || equipamento.getNumeroSerie().isBlank()) {
            throw new RegraNegocioException("O número de série é obrigatório.");
        }

        List<String> nomes = equipamento.getSubItens().stream()
                .map(SubItem::getNome)
                .map(String::toLowerCase)
                .toList();
        if (nomes.size() != nomes.stream().distinct().count()) {
            throw new RegraNegocioException("O kit não pode ter subitens com nomes duplicados.");
        }
    }

}