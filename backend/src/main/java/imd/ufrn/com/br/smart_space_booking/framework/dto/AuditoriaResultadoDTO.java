package imd.ufrn.com.br.smart_space_booking.framework.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/*
 * Formato esperado do Gemini:
 * {
 *   "imagemValida": true,
 *   "observacaoGeral": "Sala em boas condições, apenas chão com poeira leve.",
 *   "criterios": [
 *     { "id": 1, "nome": "Limpeza geral",           "nota": 7, "observacao": "..." },
 *     { "id": 2, "nome": "Organização do mobiliário","nota": 9, "observacao": "..." },
 *     { "id": 3, "nome": "Integridade dos equipamentos","nota": 10,"observacao": "..." }
 *   ]
 * }
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditoriaResultadoDTO {

    @JsonProperty("imagemValida")
    private boolean imagemValida = true;

    @JsonProperty("recursoCorreto")
    private boolean recursoCorreto = true;

    private boolean aprovado;

    @JsonProperty("observacaoGeral")
    private String observacaoGeral;

    private List<AvaliacaoCriterioDTO> criterios = new ArrayList<>(); //so no checkout

    public AuditoriaResultadoDTO() {}
}