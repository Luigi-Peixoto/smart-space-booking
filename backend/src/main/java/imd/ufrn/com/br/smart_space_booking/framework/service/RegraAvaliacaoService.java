package imd.ufrn.com.br.smart_space_booking.framework.service;

import imd.ufrn.com.br.smart_space_booking.framework.dto.RegraAvaliacaoRequestDTO;
import imd.ufrn.com.br.smart_space_booking.framework.dto.RegraAvaliacaoResponseDTO;
import imd.ufrn.com.br.smart_space_booking.framework.exception.RegraNegocioException;
import imd.ufrn.com.br.smart_space_booking.framework.model.RegraAvaliacao;
import imd.ufrn.com.br.smart_space_booking.framework.repository.RegraAvaliacaoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RegraAvaliacaoService {

    private final RegraAvaliacaoRepository regraRepository;

    public RegraAvaliacaoService(RegraAvaliacaoRepository regraRepository) {
        this.regraRepository = regraRepository;
    }

    public List<RegraAvaliacaoResponseDTO> listarTodas() {
        return regraRepository.findAllByOrderByIdAsc()
                .stream()
                .map(RegraAvaliacaoResponseDTO::fromEntity)
                .toList();
    }

    public List<RegraAvaliacao> buscarTodasParaPrompt() {
        return regraRepository.findAllByOrderByIdAsc();
    }

    @Transactional
    public RegraAvaliacaoResponseDTO criar(RegraAvaliacaoRequestDTO dto) {
        validar(dto);

        RegraAvaliacao regra = new RegraAvaliacao();
        preencherCampos(regra, dto);

        return RegraAvaliacaoResponseDTO.fromEntity(regraRepository.save(regra));
    }

    @Transactional
    public RegraAvaliacaoResponseDTO atualizar(Long id, RegraAvaliacaoRequestDTO dto) {
        validar(dto);

        RegraAvaliacao regra = buscar(id);
        preencherCampos(regra, dto);

        return RegraAvaliacaoResponseDTO.fromEntity(regraRepository.save(regra));
    }

    @Transactional
    public void deletar(Long id) {
        if (!regraRepository.existsById(id)) {
            throw new RuntimeException("Regra não encontrada: " + id);
        }
        regraRepository.deleteById(id);
    }

    private void preencherCampos(RegraAvaliacao regra, RegraAvaliacaoRequestDTO dto) {
        regra.setNome(dto.nome().trim());
        regra.setDescricao(dto.descricao().trim());
        regra.setLimiBonus(dto.limiBonus());
        regra.setDeltaBonus(dto.deltaBonus());
        regra.setLimiPenalidade(dto.limiPenalidade());
        regra.setDeltaPenalidade(dto.deltaPenalidade());
    }

    private RegraAvaliacao buscar(Long id) {
        return regraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Regra não encontrada: " + id));
    }

    private void validar(RegraAvaliacaoRequestDTO dto) {
        if (dto.nome() == null || dto.nome().isBlank())
            throw new RegraNegocioException("O nome da regra é obrigatório.");
        if (dto.nome().length() > 100)
            throw new RegraNegocioException("O nome deve ter no máximo 100 caracteres.");

        if (dto.descricao() == null || dto.descricao().isBlank())
            throw new RegraNegocioException("A descrição/instrução é obrigatória.");
        if (dto.descricao().length() > 500)
            throw new RegraNegocioException("A descrição deve ter no máximo 500 caracteres.");

        if (dto.limiBonus() == null || dto.limiPenalidade() == null)
            throw new RegraNegocioException("Limiares de bônus e penalidade são obrigatórios.");
        if (dto.limiBonus() < 0 || dto.limiBonus() > 10)
            throw new RegraNegocioException("Limiar de bônus deve estar entre 0 e 10.");
        if (dto.limiPenalidade() < 0 || dto.limiPenalidade() > 10)
            throw new RegraNegocioException("Limiar de penalidade deve estar entre 0 e 10.");
        if (dto.limiPenalidade() >= dto.limiBonus())
            throw new RegraNegocioException("Limiar de bônus deve ser maior que o de penalidade.");

        if (dto.deltaBonus() == null || dto.deltaBonus() < 0)
            throw new RegraNegocioException("Delta de bônus deve ser >= 0.");
        if (dto.deltaBonus() > 50)
            throw new RegraNegocioException("Delta de bônus deve ser no máximo 50.");

        if (dto.deltaPenalidade() == null || dto.deltaPenalidade() > 0)
            throw new RegraNegocioException("Delta de penalidade deve ser <= 0.");
        if (dto.deltaPenalidade() < -50)
            throw new RegraNegocioException("Delta de penalidade deve ser no mínimo -50.");
    }
}