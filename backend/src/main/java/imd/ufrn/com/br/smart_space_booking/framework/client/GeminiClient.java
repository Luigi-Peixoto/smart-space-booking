package imd.ufrn.com.br.smart_space_booking.framework.client;

import imd.ufrn.com.br.smart_space_booking.framework.config.GeminiConfig;
import imd.ufrn.com.br.smart_space_booking.framework.dto.GeminiRequestDTO;
import imd.ufrn.com.br.smart_space_booking.framework.dto.GeminiResponseDTO;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component
public class GeminiClient {

    private final GeminiConfig geminiConfig;
    private final RestTemplate restTemplate;

    public GeminiClient(GeminiConfig geminiConfig) {
        this.geminiConfig = geminiConfig;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Envia prompt + imagens de referência da sala + imagens do usuário para o Gemini.
     *
     * @param prompt             Instrução para o Gemini
     * @param imagensReferencia  Bytes das imagens cadastradas na sala (estado esperado)
     * @param imagensUsuario     Arquivos enviados pelo usuário no check-in/check-out
     */
    public String analisar(String prompt, List<byte[]> imagensReferencia, List<MultipartFile> imagensUsuario) {
        try {
            GeminiRequestDTO request = montarRequest(prompt, imagensReferencia, imagensUsuario);
            return chamarApi(request);
        } catch (Exception e) {
            throw new RuntimeException("Erro na comunicação com o Gemini: " + e.getMessage(), e);
        }
    }

    private GeminiRequestDTO montarRequest(String prompt,
                                           List<byte[]> imagensReferencia,
                                           List<MultipartFile> imagensUsuario) throws Exception {
        List<GeminiRequestDTO.Part> parts = new ArrayList<>();

        // 1. Prompt de texto
        parts.add(new GeminiRequestDTO.Part(prompt));

        // 2. Imagens de referência da sala (estado esperado) — entram primeiro
        for (byte[] bytes : imagensReferencia) {
            String base64 = Base64.getEncoder().encodeToString(bytes);
            parts.add(new GeminiRequestDTO.Part(new GeminiRequestDTO.InlineData("image/jpeg", base64)));
        }

        // 3. Imagens do usuário (estado atual) — entram depois
        for (MultipartFile imagem : imagensUsuario) {
            String mimeType = imagem.getContentType() != null ? imagem.getContentType() : "image/jpeg";
            String base64 = Base64.getEncoder().encodeToString(imagem.getBytes());
            parts.add(new GeminiRequestDTO.Part(new GeminiRequestDTO.InlineData(mimeType, base64)));
        }

        return new GeminiRequestDTO(List.of(new GeminiRequestDTO.Content(parts)));
    }

    private String chamarApi(GeminiRequestDTO request) {
        String url = geminiConfig.getApiUrl()
                + "/v1beta/models/" + geminiConfig.getModel()
                + ":generateContent?key=" + geminiConfig.getApiKey();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<GeminiResponseDTO> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                GeminiResponseDTO.class
        );

        if (response.getBody() == null) {
            throw new RuntimeException("Resposta vazia da API Gemini.");
        }

        String texto = response.getBody().extrairTexto();
        if (texto == null || texto.isBlank()) {
            throw new RuntimeException("Gemini não retornou texto na resposta.");
        }

        return texto;
    }
}