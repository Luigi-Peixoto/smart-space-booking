package imd.ufrn.com.br.smart_space_booking.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import imd.ufrn.com.br.smart_space_booking.client.GeminiClient;
import imd.ufrn.com.br.smart_space_booking.client.MediaStorageClient;
import imd.ufrn.com.br.smart_space_booking.dto.AuditoriaResponseDTO;
import imd.ufrn.com.br.smart_space_booking.dto.AuditoriaResultadoDTO;
import imd.ufrn.com.br.smart_space_booking.enums.AuditoriaTipo;
import imd.ufrn.com.br.smart_space_booking.exception.ImagemInvalidaException;
import imd.ufrn.com.br.smart_space_booking.model.Auditoria;
import imd.ufrn.com.br.smart_space_booking.model.RegraAvaliacao;
import imd.ufrn.com.br.smart_space_booking.model.Reserva;
import imd.ufrn.com.br.smart_space_booking.repository.AuditoriaRepository;
import imd.ufrn.com.br.smart_space_booking.repository.ReservaRepository;
import imd.ufrn.com.br.smart_space_booking.strategy.AuditoriaStrategy;
import jakarta.transaction.Transactional;

@Service
public class AuditoriaService {

    private final GeminiClient geminiClient;
    private final MediaStorageClient mediaStorageClient;
    private final AuditoriaRepository auditoriaRepository;
    private final ReservaRepository reservaRepository;
    private final ReservaService reservaService;
    private final RegraAvaliacaoService regraAvaliacaoService;
    private final TrustScoreService trustScoreService;
    private final ObjectMapper objectMapper;
    private final List<AuditoriaStrategy> estrategias;

    public AuditoriaService(GeminiClient geminiClient,
                            MediaStorageClient mediaStorageClient,
                            AuditoriaRepository auditoriaRepository,
                            ReservaRepository reservaRepository,
                            ReservaService reservaService,
                            RegraAvaliacaoService regraAvaliacaoService,
                            TrustScoreService trustScoreService,
                            ObjectMapper objectMapper,
                            List<AuditoriaStrategy> estrategias) {
        this.geminiClient = geminiClient;
        this.mediaStorageClient = mediaStorageClient;
        this.auditoriaRepository = auditoriaRepository;
        this.reservaRepository = reservaRepository;
        this.reservaService = reservaService;
        this.regraAvaliacaoService = regraAvaliacaoService;
        this.trustScoreService = trustScoreService;
        this.objectMapper = objectMapper;
        this.estrategias = estrategias;
    }

    public List<AuditoriaResponseDTO> findAll() {
        return auditoriaRepository.findAll().stream()
                .map(AuditoriaResponseDTO::fromEntity)
                .toList();
    }

    public List<AuditoriaResponseDTO> findByReservaId(Long reservaId) {
        return auditoriaRepository.findByReservaId(reservaId).stream()
                .map(AuditoriaResponseDTO::fromEntity)
                .toList();
    }

    // ─── Check-in ────────────────────────────────────────────────────────────

    @Transactional
    public Auditoria realizarCheckIn(Long reservaId, Long usuarioLogadoId,
                                     List<MultipartFile> imagens, List<String> imageIds) {
        reservaService.validarCheckin(reservaId, usuarioLogadoId);
        Reserva reserva = buscarReserva(reservaId);
        validarAuditoriaInexistente(reservaId, AuditoriaTipo.CHECK_IN);

        AuditoriaStrategy strategy = resolver(reserva);
        validarQuantidadeImagens(strategy, imagens, imageIds);

        // Check-in usa prompt fixo do hotspot — sem critérios de nota
        List<byte[]> imagensReferencia = carregarReferencia(strategy, reserva);
        String respostaTexto = geminiClient.analisar(
                strategy.promptCheckIn(), imagensReferencia, imagens);

        AuditoriaResultadoDTO resultado = parsearResposta(respostaTexto);
        validarResultado(strategy, resultado, imageIds);

        reservaService.registrarCheckin(reservaId);

        Auditoria auditoria = new Auditoria();
        auditoria.setReserva(reserva);
        auditoria.setTipo(AuditoriaTipo.CHECK_IN);
        auditoria.setAprovado(resultado.isAprovado());
        auditoria.setObservacaoGeral(resultado.getObservacaoGeral());
        auditoria.setCriterios(List.of()); // check-in não tem notas por critério
        auditoria.setDeltaTrustScoreAplicado(null);
        auditoria.setImageIds(imageIds != null ? imageIds : List.of());
        auditoria.setResultadoIa(respostaTexto);

        return auditoriaRepository.save(auditoria);
    }

    // ─── Check-out ───────────────────────────────────────────────────────────

    @Transactional
    public Auditoria realizarCheckOut(Long reservaId, Long usuarioLogadoId,
                                      List<MultipartFile> imagens, List<String> imageIds) {
        reservaService.validarCheckout(reservaId, usuarioLogadoId);
        Reserva reserva = buscarReserva(reservaId);
        validarAuditoriaInexistente(reservaId, AuditoriaTipo.CHECK_OUT);

        AuditoriaStrategy strategy = resolver(reserva);
        validarQuantidadeImagens(strategy, imagens, imageIds);

        List<RegraAvaliacao> regras = regraAvaliacaoService.buscarTodasParaPrompt();
        String prompt = strategy.promptCheckOut(regras);

        List<byte[]> imagensReferencia = carregarReferencia(strategy, reserva);
        String respostaTexto = geminiClient.analisar(prompt, imagensReferencia, imagens);

        AuditoriaResultadoDTO resultado = parsearResposta(respostaTexto);
        validarResultado(strategy, resultado, imageIds);

        reservaService.registrarCheckout(reservaId);

        // Calcula e aplica o delta de Trust Score com base nas notas por critério
        int deltaAplicado = trustScoreService.aplicarDelta(
                reserva.getUsuario().getId(), resultado.getCriterios(), reserva);

        Auditoria auditoria = new Auditoria();
        auditoria.setReserva(reserva);
        auditoria.setTipo(AuditoriaTipo.CHECK_OUT);
        auditoria.setAprovado(resultado.isAprovado());
        auditoria.setObservacaoGeral(resultado.getObservacaoGeral());
        auditoria.setCriterios(resultado.getCriterios());
        auditoria.setDeltaTrustScoreAplicado(deltaAplicado);
        auditoria.setImageIds(imageIds != null ? imageIds : List.of());
        auditoria.setResultadoIa(respostaTexto);

        return auditoriaRepository.save(auditoria);
    }

    // ─── Pontos variáveis: delegação à Strategy ──────────────────────────────

    /** Escolhe a estratégia de auditoria pelo tipo do recurso da reserva. */
    private AuditoriaStrategy resolver(Reserva reserva) {
        return estrategias.stream()
                .filter(s -> s.suporta(reserva.getRecurso()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Nenhuma AuditoriaStrategy para o recurso: "
                                + reserva.getRecurso().getClass().getSimpleName()));
    }

    private void validarQuantidadeImagens(AuditoriaStrategy strategy,
                                          List<MultipartFile> imagens, List<String> imageIds) {
        int esperado = strategy.imagensEsperadas().size();
        int recebido = imagens != null ? imagens.size() : 0;
        if (recebido != esperado) {
            deletarImagens(imageIds);
            throw new ImagemInvalidaException(
                    "Esperado " + esperado + " imagem(ns) para este recurso, recebido " + recebido + ".");
        }
    }

    private List<byte[]> carregarReferencia(AuditoriaStrategy strategy, Reserva reserva) {
        return strategy.imagensReferencia(reserva).stream()
                .map(mediaStorageClient::buscarImagem)
                .toList();
    }

    /** Aplica a validação de domínio do hotspot, centralizando a limpeza de imagens em caso de falha. */
    private void validarResultado(AuditoriaStrategy strategy,
                                  AuditoriaResultadoDTO resultado, List<String> imageIds) {
        try {
            strategy.validarResultado(resultado);
        } catch (RuntimeException e) {
            deletarImagens(imageIds);
            throw e;
        }
    }

    // ─── Infraestrutura fixa do template ─────────────────────────────────────

    private AuditoriaResultadoDTO parsearResposta(String textoResposta) {
        try {
            String limpo = textoResposta
                    .replaceAll("(?s)```json\\s*", "")
                    .replaceAll("(?s)```\\s*", "")
                    .trim();

            // Extrai apenas o bloco JSON caso o Gemini adicione texto antes/depois
            int inicio = limpo.indexOf('{');
            int fim = limpo.lastIndexOf('}');
            if (inicio != -1 && fim != -1 && fim > inicio) {
                limpo = limpo.substring(inicio, fim + 1);
            }

            return objectMapper.readValue(limpo, AuditoriaResultadoDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao interpretar resposta do Gemini: " + e.getMessage(), e);
        }
    }

    private Reserva buscarReserva(Long reservaId) {
        return reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada: " + reservaId));
    }

    private void validarAuditoriaInexistente(Long reservaId, AuditoriaTipo tipo) {
        if (auditoriaRepository.findByReservaIdAndTipo(reservaId, tipo).isPresent()) {
            throw new RuntimeException(tipo + " já realizado para a reserva: " + reservaId);
        }
    }

    private void deletarImagens(List<String> imageIds) {
        if (imageIds != null) {
            imageIds.forEach(id -> {
                try { mediaStorageClient.deletarImagem(id); } catch (Exception ignored) {}
            });
        }
    }
}