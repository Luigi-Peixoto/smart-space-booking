package imd.ufrn.com.br.smart_space_booking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import imd.ufrn.com.br.smart_space_booking.client.GeminiClient;
import imd.ufrn.com.br.smart_space_booking.client.MediaStorageClient;
import imd.ufrn.com.br.smart_space_booking.dto.AuditoriaResponseDTO;
import imd.ufrn.com.br.smart_space_booking.dto.AuditoriaResultadoDTO;
import imd.ufrn.com.br.smart_space_booking.enums.AuditoriaTipo;
import imd.ufrn.com.br.smart_space_booking.exception.ImagemInvalidaException;
import imd.ufrn.com.br.smart_space_booking.exception.SalaIncorretaException;
import imd.ufrn.com.br.smart_space_booking.model.Auditoria;
import imd.ufrn.com.br.smart_space_booking.model.RegraAvaliacao;
import imd.ufrn.com.br.smart_space_booking.model.Reserva;
import imd.ufrn.com.br.smart_space_booking.prompts.AuditoriaPrompts;
import imd.ufrn.com.br.smart_space_booking.repository.AuditoriaRepository;
import imd.ufrn.com.br.smart_space_booking.repository.ReservaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    public AuditoriaService(GeminiClient geminiClient,
                            MediaStorageClient mediaStorageClient,
                            AuditoriaRepository auditoriaRepository,
                            ReservaRepository reservaRepository,
                            ReservaService reservaService,
                            RegraAvaliacaoService regraAvaliacaoService,
                            TrustScoreService trustScoreService,
                            ObjectMapper objectMapper) {
        this.geminiClient = geminiClient;
        this.mediaStorageClient = mediaStorageClient;
        this.auditoriaRepository = auditoriaRepository;
        this.reservaRepository = reservaRepository;
        this.reservaService = reservaService;
        this.regraAvaliacaoService = regraAvaliacaoService;
        this.trustScoreService = trustScoreService;
        this.objectMapper = objectMapper;
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

        List<byte[]> imagensReferencia = buscarImagensReferencia(reserva);
        // Check-in usa prompt fixo — sem critérios de nota
        String respostaTexto = geminiClient.analisar(
                AuditoriaPrompts.promptCheckIn(), imagensReferencia, imagens);

        AuditoriaResultadoDTO resultado = parsearResposta(respostaTexto);
        validarImagemValida(resultado, imageIds);
        validarSalaCorreta(resultado, imageIds);

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

        List<RegraAvaliacao> regras = regraAvaliacaoService.buscarTodasParaPrompt();
        String prompt = AuditoriaPrompts.promptCheckOut(regras);

        List<byte[]> imagensReferencia = buscarImagensReferencia(reserva);
        String respostaTexto = geminiClient.analisar(prompt, imagensReferencia, imagens);

        AuditoriaResultadoDTO resultado = parsearResposta(respostaTexto);
        validarImagemValida(resultado, imageIds);
        validarSalaCorreta(resultado, imageIds);

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

    // ─── Helpers privados ────────────────────────────────────────────────────

    private List<byte[]> buscarImagensReferencia(Reserva reserva) {
        return reserva.getSala().getImagens().stream()
                .map(mediaStorageClient::buscarImagem)
                .toList();
    }

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

    private void validarImagemValida(AuditoriaResultadoDTO resultado, List<String> imageIds) {
        if (!resultado.isImagemValida()) {
            deletarImagens(imageIds);
            throw new ImagemInvalidaException(
                    "As imagens enviadas não correspondem a um ambiente interno. " +
                            "Por favor, fotografe a sala corretamente.");
        }
    }

    private void validarSalaCorreta(AuditoriaResultadoDTO resultado, List<String> imageIds) {
        if (!resultado.isSalaCorreta()) {
            deletarImagens(imageIds);
            throw new SalaIncorretaException(
                    "A sala fotografada não corresponde à sala reservada. " +
                            "Por favor, fotografe a sala correta.");
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