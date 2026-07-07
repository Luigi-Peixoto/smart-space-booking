package imd.ufrn.com.br.smart_space_booking.instancia_veiculo.service;

import imd.ufrn.com.br.smart_space_booking.framework.exception.RegraNegocioException;
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

    @Override
    protected void validarEspecifico(Veiculo veiculo) {
        if (veiculo.getPlaca() == null || !veiculo.getPlaca().matches("[A-Z]{3}[0-9][A-Z0-9][0-9]{2}")) {
            throw new RegraNegocioException("Placa inválida. Use o formato Mercosul ou antigo.");
        }
        if (veiculo.getChassi() == null || veiculo.getChassi().length() != 17) {
            throw new RegraNegocioException("O chassi deve ter exatamente 17 caracteres.");
        }
        if (veiculo.getRenavam() == null || !veiculo.getRenavam().matches("[0-9]{11}")) {
            throw new RegraNegocioException("O RENAVAM deve ter 11 dígitos.");
        }
    }

}