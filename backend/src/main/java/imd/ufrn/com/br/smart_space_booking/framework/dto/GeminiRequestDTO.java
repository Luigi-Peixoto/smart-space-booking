package imd.ufrn.com.br.smart_space_booking.framework.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class GeminiRequestDTO {

    private List<Content> contents;

    public GeminiRequestDTO(List<Content> contents) {
        this.contents = contents;
    }

    @Getter
    public static class Content {
        private List<Part> parts;

        public Content(List<Part> parts) {
            this.parts = parts;
        }

    }

    @Getter
    public static class Part {
        // Para texto
        private String text;

        // Para imagem
        @JsonProperty("inline_data")
        private InlineData inlineData;

        // Construtor para parte de texto
        public Part(String text) {
            this.text = text;
        }

        // Construtor para parte de imagem
        public Part(InlineData inlineData) {
            this.inlineData = inlineData;
        }

    }

    @Getter
    public static class InlineData {
        @JsonProperty("mime_type")
        private String mimeType;

        private String data; // Base64 da imagem

        public InlineData(String mimeType, String data) {
            this.mimeType = mimeType;
            this.data = data;
        }

    }
}