package imd.ufrn.com.br.smart_space_booking.instancia_veiculo.prompts;

import imd.ufrn.com.br.smart_space_booking.framework.prompts.AuditoriaPromptTemplate;
import org.springframework.stereotype.Component;

/**
 * Preenche os slots variáveis do AuditoriaPromptTemplate com a semântica de veículos.
 * Todo o esqueleto (etapas, formatação e contrato JSON) vem da classe base.
 *
 * Convenção de imagens (deve casar com AuditoriaVeiculoStrategy.imagensEsperadas()):
 *   1ª = visão EXTERNA da lataria, 2ª = visão INTERNA, 3ª = foto da PLACA.
 */
@Component
public class VeiculoPromptTemplate extends AuditoriaPromptTemplate {

    @Override
    protected String recurso() {
        return "veículos da frota corporativa";
    }

    @Override
    protected String descricaoImagemValida() {
        return "um veículo automotor, com as imagens SEMPRE nesta ordem: "
             + "(1ª) visão EXTERNA da lataria, "
             + "(2ª) visão INTERNA (bancos e painel) e "
             + "(3ª) foto aproximada e legível da PLACA";
    }

    @Override
    protected String elementosIdentidade() {
        return """
                - Os caracteres da PLACA (compare exatamente com a foto de referência — este é o sinal mais forte)
                - Marca e modelo do veículo
                - Cor predominante da lataria
                - Formato geral da carroceria (sedã, hatch, SUV, van ou utilitário)""";
    }

    @Override
    protected String diferencasIgnoradas() {
        return "sujeira, poeira, respingos de água, reflexos, ângulo e enquadramento da foto, "
             + "iluminação e o local onde o veículo está estacionado";
    }
}
