package imd.ufrn.com.br.smart_space_booking.framework.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiResponseDTO {

    private List<Candidate> candidates;

    /**
     * Extrai o texto da primeira resposta do Gemini.
     * Retorna null se a resposta estiver vazia ou malformada.
     */
    public String extrairTexto() {
        if (candidates == null || candidates.isEmpty()) return null;

        Candidate candidate = candidates.getFirst();
        if (candidate.getContent() == null) return null;

        List<Part> parts = candidate.getContent().getParts();
        if (parts == null || parts.isEmpty()) return null;

        return parts.getFirst().getText();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Candidate {
        private Content content;

        @JsonProperty("finishReason")
        private String finishReason;

        public Content getContent() {
            return content;
        }

        public String getFinishReason() {
            return finishReason;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Content {
        private List<Part> parts;
        private String role;

        public List<Part> getParts() {
            return parts;
        }

        public String getRole() {
            return role;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Part {
        private String text;

        public String getText() {
            return text;
        }
    }
}