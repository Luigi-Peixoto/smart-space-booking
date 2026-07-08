package imd.ufrn.com.br.smart_space_booking.instancia_veiculo.prompts;

import imd.ufrn.com.br.smart_space_booking.framework.prompts.AuditoriaPromptTemplate;
import org.springframework.stereotype.Component;

/**
 * Preenche os slots variáveis do AuditoriaPromptTemplate com a semântica de veículos.
 * Todo o esqueleto (etapas, formatação e contrato JSON) vem da classe base.
 *
 * Convenção de imagens (deve casar com AuditoriaVeiculoStrategy.imagensEsperadas()):
 * das imagens ENVIADAS PELO USUÁRIO (ignorando as de referência, que a base já
 * declara separadamente por quantidade): 1ª = visão EXTERNA da lataria,
 * 2ª = visão INTERNA, 3ª = foto da PLACA.
 */
@Component
public class VeiculoPromptTemplate extends AuditoriaPromptTemplate {

    @Override
    protected String recurso() {
        return "veículos da frota corporativa";
    }

    @Override
    protected String descricaoImagemValida() {
        return "um veículo automotor. Das imagens ENVIADAS PELO USUÁRIO especificamente "
                + "(não conte as imagens de referência, que já foram identificadas por "
                + "quantidade acima), a ordem SEMPRE é: "
                + "(1ª foto do usuário) visão EXTERNA da lataria, "
                + "(2ª foto do usuário) visão INTERNA (bancos e painel) e "
                + "(3ª foto do usuário) foto aproximada e legível da PLACA";
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