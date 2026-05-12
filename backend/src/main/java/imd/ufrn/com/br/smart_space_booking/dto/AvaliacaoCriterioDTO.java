package imd.ufrn.com.br.smart_space_booking.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

// nota que o Gemini atribui a UM critério de avaliação.
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AvaliacaoCriterioDTO {

    // ID da RegraAvaliacao
    private Long id;

    private String nome;

    private Integer nota;

    private String observacao;

    public AvaliacaoCriterioDTO() {}
}
