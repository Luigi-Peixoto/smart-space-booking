package imd.ufrn.com.br.smart_space_booking.instancia_veiculo.service;

import imd.ufrn.com.br.smart_space_booking.framework.service.RecursoService;
import imd.ufrn.com.br.smart_space_booking.instancia_veiculo.dto.VeiculoResponseDTO;
import imd.ufrn.com.br.smart_space_booking.instancia_veiculo.exception.VeiculoNotFoundException;
import imd.ufrn.com.br.smart_space_booking.instancia_veiculo.model.Veiculo;
import imd.ufrn.com.br.smart_space_booking.instancia_veiculo.repository.VeiculoRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class VeiculoService extends RecursoService<Veiculo, VeiculoResponseDTO> {

    private final VeiculoRepository veiculoRepository;

    public VeiculoService(VeiculoRepository veiculoRepository) {
        this.veiculoRepository = veiculoRepository;
    }

    @Override
    protected JpaRepository<Veiculo, Long> getRepository() {
        return veiculoRepository;
    }

    @Override
    protected VeiculoResponseDTO convertToDTO(Veiculo veiculo) {
        return VeiculoResponseDTO.fromEntity(veiculo);
    }

    @Override
    protected Veiculo atualizarCampos(Veiculo existente, Veiculo dadosNovos) {
        existente.setNome(dadosNovos.getNome());
        existente.setStatus(dadosNovos.getStatus());
        existente.setPlaca(dadosNovos.getPlaca());
        existente.setChassi(dadosNovos.getChassi());
        existente.setRenavam(dadosNovos.getRenavam());
        existente.setModelo(dadosNovos.getModelo());
        existente.setMarca(dadosNovos.getMarca());
        existente.setCor(dadosNovos.getCor());
        existente.setImagens(dadosNovos.getImagens());
        return existente;
    }

    @Override
    protected RuntimeException notFoundException(Long id) {
        return new VeiculoNotFoundException(id);
    }
}