package imd.ufrn.com.br.smart_space_booking.framework.service;

import imd.ufrn.com.br.smart_space_booking.framework.dto.EventoDisponivelDTO;
import imd.ufrn.com.br.smart_space_booking.framework.dto.NivelExigenciaDisponivelDTO;
import imd.ufrn.com.br.smart_space_booking.framework.dto.RegraTrustScoreRequestDTO;
import imd.ufrn.com.br.smart_space_booking.framework.dto.RegraTrustScoreResponseDTO;
import imd.ufrn.com.br.smart_space_booking.framework.dto.RestricaoDisponivelDTO;
import imd.ufrn.com.br.smart_space_booking.framework.enums.CategoriaRegraTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.enums.NivelExigencia;
import imd.ufrn.com.br.smart_space_booking.framework.evento.EventoTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.exception.RegraNegocioException;
import imd.ufrn.com.br.smart_space_booking.framework.model.RegraTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.repository.RegraTrustScoreRepository;
import imd.ufrn.com.br.smart_space_booking.framework.restricao.RestricaoTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.strategy.TrustScoreStrategy;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Serviço de regras de negócio do TrustScore — CRUD de regras, validação de
 * chaves, listagem de restrições e eventos registrados pelos hotspots.
 */
@Service
public class RegraTrustScoreService {

    private final RegraTrustScoreRepository regraRepository;
    private final List<TrustScoreStrategy> trustScoreStrategies;

    public RegraTrustScoreService(RegraTrustScoreRepository regraRepository,
                                  List<TrustScoreStrategy> trustScoreStrategies) {
        this.regraRepository = regraRepository;
        this.trustScoreStrategies = trustScoreStrategies;
    }

    public List<RegraTrustScoreResponseDTO> listarPorCategoria(CategoriaRegraTrustScore categoria) {
        return regraRepository.findByCategoria(categoria)
                .stream()
                .map(RegraTrustScoreResponseDTO::fromEntity)
                .toList();
    }

    /**
     * Lista todas as restrições registradas pelos hotspots, filtrando por tipo de recurso se fornecido.
     * Cada hotspot declara suas próprias restrições, e o framework apenas agrega e repassa.
     * 
     */
    public List<RestricaoDisponivelDTO> listarRestricoesDisponiveis(String tipoRecurso) {
        return trustScoreStrategies.stream()
                .filter(s -> tipoRecurso == null || s.tipoRecurso().equals(tipoRecurso))
                .flatMap(s -> s.restricoes().stream())
                .map(RestricaoDisponivelDTO::fromRestricao)
                .distinct()
                .toList();
    }

    /**
     * Lista todos os eventos registrados pelos hotspots, filtrando por tipo de recurso se fornecido.
     * Cada hotspot declara seus próprios eventos, e o framework apenas agrega e repassa.
     * 
     */
    public List<EventoDisponivelDTO> listarEventosDisponiveis(String tipoRecurso) {
        return trustScoreStrategies.stream()
                .filter(s -> tipoRecurso == null || s.tipoRecurso().equals(tipoRecurso))
                .flatMap(s -> s.eventos().stream())
                .map(EventoDisponivelDTO::fromEvento)
                .distinct()
                .toList();
    }

    /**
     * Lista todos os níveis de exigência disponíveis para o tipo de recurso fornecido.
     * Cada hotspot decide como quer mapear seus níveis de exigência, e o framework apenas agrega e repassa.
     */
    public List<NivelExigenciaDisponivelDTO> listarNiveisExigenciaDisponiveis(String tipoRecurso) {
        TrustScoreStrategy strategy = trustScoreStrategies.stream()
                .filter(s -> s.tipoRecurso().equals(tipoRecurso))
                .findFirst()
                .orElseThrow(() -> new RegraNegocioException("Tipo de recurso desconhecido: " + tipoRecurso));

        return Arrays.stream(NivelExigencia.values())
                .map(nivel -> new NivelExigenciaDisponivelDTO(
                        nivel.name(), strategy.descricaoNivelExigencia(nivel), nivel.scoreMinimoPadrao()))
                .toList();
    }

    @Transactional
    public RegraTrustScoreResponseDTO criar(RegraTrustScoreRequestDTO dto) {
        validar(dto);

        if (regraRepository.findByCategoriaAndChave(dto.categoria(), dto.chave()).isPresent())
            throw new RegraNegocioException(
                    "Já existe uma regra cadastrada para " + dto.categoria() + ":" + dto.chave());

        RegraTrustScore regra = new RegraTrustScore();
        preencherCampos(regra, dto);

        return RegraTrustScoreResponseDTO.fromEntity(regraRepository.save(regra));
    }

    @Transactional
    public RegraTrustScoreResponseDTO atualizar(Long id, RegraTrustScoreRequestDTO dto) {
        validar(dto);

        RegraTrustScore regra = buscar(id);
        preencherCampos(regra, dto);

        return RegraTrustScoreResponseDTO.fromEntity(regraRepository.save(regra));
    }

    @Transactional
    public void deletar(Long id) {
        if (!regraRepository.existsById(id))
            throw new RegraNegocioException("Regra não encontrada: " + id);
        regraRepository.deleteById(id);
    }

    private void preencherCampos(RegraTrustScore regra, RegraTrustScoreRequestDTO dto) {
        regra.setCategoria(dto.categoria());
        regra.setChave(dto.chave());
        regra.setValorPrincipal(dto.valorPrincipal());
        regra.setValorSecundario(dto.valorSecundario());
        regra.setDescricao(dto.descricao());
    }

    private RegraTrustScore buscar(Long id) {
        return regraRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Regra não encontrada: " + id));
    }

    private void validar(RegraTrustScoreRequestDTO dto) {
        if (dto.categoria() == null)
            throw new RegraNegocioException("A categoria é obrigatória.");
        if (dto.chave() == null || dto.chave().isBlank())
            throw new RegraNegocioException("A chave é obrigatória.");
        if (dto.valorPrincipal() == null)
            throw new RegraNegocioException("O valor principal é obrigatório.");

        switch (dto.categoria()) {
            case EXIGENCIA -> {
                if (dto.valorPrincipal() < 0 || dto.valorPrincipal() > 100)
                    throw new RegraNegocioException("O score mínimo deve estar entre 0 e 100.");
                if (!chaveEhNivelExigenciaValido(dto.chave()))
                    throw new RegraNegocioException(
                            "Chave desconhecida para EXIGENCIA: " + dto.chave() + ". Use um dos níveis: "
                                    + Arrays.toString(NivelExigencia.values()));
            }
            case RESTRICAO -> {
                if (dto.valorPrincipal() < 1)
                    throw new RegraNegocioException("O valor limite deve ser pelo menos 1.");
                if (dto.valorSecundario() == null || dto.valorSecundario() < 0 || dto.valorSecundario() > 100)
                    throw new RegraNegocioException("O score mínimo para exceder deve estar entre 0 e 100.");
                if (!chavesRestricaoRegistradas().contains(dto.chave()))
                    throw new RegraNegocioException(
                            "Chave desconhecida para RESTRICAO: " + dto.chave()
                                    + ". Nenhum hotspot registrou essa restrição.");
            }
            case EVENTO -> {
                if (dto.valorSecundario() != null && dto.valorSecundario() < 0)
                    throw new RegraNegocioException("O parâmetro, quando informado, não pode ser negativo.");
                if (!chavesEventoRegistradas().contains(dto.chave()))
                    throw new RegraNegocioException(
                            "Chave desconhecida para EVENTO: " + dto.chave()
                                    + ". Nenhum hotspot registrou esse evento.");
            }
        }
    }

    private boolean chaveEhNivelExigenciaValido(String chave) {
        try {
            NivelExigencia.valueOf(chave);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private Set<String> chavesRestricaoRegistradas() {
        return trustScoreStrategies.stream()
                .flatMap(s -> s.restricoes().stream())
                .map(RestricaoTrustScore::chave)
                .collect(Collectors.toSet());
    }

    private Set<String> chavesEventoRegistradas() {
        return trustScoreStrategies.stream()
                .flatMap(s -> s.eventos().stream())
                .map(EventoTrustScore::chave)
                .collect(Collectors.toSet());
    }
}
