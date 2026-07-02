package imd.ufrn.com.br.smart_space_booking.framework.service;

import imd.ufrn.com.br.smart_space_booking.framework.dto.RegraTrustScoreEventoRequestDTO;
import imd.ufrn.com.br.smart_space_booking.framework.dto.RegraTrustScoreEventoResponseDTO;
import imd.ufrn.com.br.smart_space_booking.framework.exception.RegraNegocioException;
import imd.ufrn.com.br.smart_space_booking.framework.model.RegraTrustScoreEvento;
import imd.ufrn.com.br.smart_space_booking.framework.repository.RegraTrustScoreEventoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * CRUD do admin sobre a severidade dos eventos estruturais de TrustScore
 * (cancelamento tardio, no-show, excesso de cancelamentos). Diferente de
 * RegraAvaliacaoService — aqui o conjunto de "nomes" possíveis é fechado
 * (um TrustScoreEvento), então cada evento só pode ter uma regra cadastrada.
 */
@Service
public class RegraTrustScoreEventoService {

    private final RegraTrustScoreEventoRepository regraRepository;

    public RegraTrustScoreEventoService(RegraTrustScoreEventoRepository regraRepository) {
        this.regraRepository = regraRepository;
    }

    public List<RegraTrustScoreEventoResponseDTO> listarTodas() {
        return regraRepository.findAll()
                .stream()
                .map(RegraTrustScoreEventoResponseDTO::fromEntity)
                .toList();
    }

    @Transactional
    public RegraTrustScoreEventoResponseDTO criar(RegraTrustScoreEventoRequestDTO dto) {
        validar(dto);

        if (regraRepository.findByEvento(dto.evento()).isPresent())
            throw new RegraNegocioException("Já existe uma regra cadastrada para o evento: " + dto.evento());

        RegraTrustScoreEvento regra = new RegraTrustScoreEvento();
        preencherCampos(regra, dto);

        return RegraTrustScoreEventoResponseDTO.fromEntity(regraRepository.save(regra));
    }

    @Transactional
    public RegraTrustScoreEventoResponseDTO atualizar(Long id, RegraTrustScoreEventoRequestDTO dto) {
        validar(dto);

        RegraTrustScoreEvento regra = buscar(id);
        preencherCampos(regra, dto);

        return RegraTrustScoreEventoResponseDTO.fromEntity(regraRepository.save(regra));
    }

    @Transactional
    public void deletar(Long id) {
        if (!regraRepository.existsById(id))
            throw new RegraNegocioException("Regra não encontrada: " + id);
        regraRepository.deleteById(id);
    }

    private void preencherCampos(RegraTrustScoreEvento regra, RegraTrustScoreEventoRequestDTO dto) {
        regra.setEvento(dto.evento());
        regra.setDelta(dto.delta());
        regra.setParametro(dto.parametro());
        regra.setDescricao(dto.descricao());
    }

    private RegraTrustScoreEvento buscar(Long id) {
        return regraRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Regra não encontrada: " + id));
    }

    private void validar(RegraTrustScoreEventoRequestDTO dto) {
        if (dto.evento() == null)
            throw new RegraNegocioException("O evento é obrigatório.");
        if (dto.delta() == null)
            throw new RegraNegocioException("O delta é obrigatório.");
        if (dto.parametro() != null && dto.parametro() < 0)
            throw new RegraNegocioException("O parâmetro, quando informado, não pode ser negativo.");
    }
}
