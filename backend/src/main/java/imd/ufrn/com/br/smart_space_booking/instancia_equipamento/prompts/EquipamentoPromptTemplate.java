package imd.ufrn.com.br.smart_space_booking.instancia_equipamento.prompts;

import imd.ufrn.com.br.smart_space_booking.framework.prompts.AuditoriaPromptTemplate;
import org.springframework.stereotype.Component;

/**
 * Preenche os slots variáveis do AuditoriaPromptTemplate com a semântica de equipamentos.
 * Todo o esqueleto (etapas, formatação e contrato JSON) vem da classe base.
 *
 * Convenção de imagens (deve casar com AuditoriaEquipamentoStrategy.imagensEsperadas()):
 *   1ª = visão GERAL do equipamento, 2ª = visão do KIT completo (todos os acessórios juntos).
 *
 * A verificação de completude do kit é feita por COMPARAÇÃO com as imagens de referência
 * do equipamento e de seus subitens — a strategy é quem junta essas referências.
 */
@Component
public class EquipamentoPromptTemplate extends AuditoriaPromptTemplate {

    @Override
    protected String recurso() {
        return "equipamentos audiovisuais";
    }

    @Override
    protected String descricaoImagemValida() {
        return "um equipamento audiovisual, com as imagens SEMPRE nesta ordem: "
             + "(1ª) visão GERAL do equipamento principal e "
             + "(2ª) visão do KIT completo, com todos os acessórios dispostos juntos e visíveis";
    }

    @Override
    protected String elementosIdentidade() {
        return """
                - Tipo, marca e modelo do equipamento principal
                - Formato, cor e características físicas visíveis do aparelho
                - COMPLETUDE DO KIT: cada item que aparece nas imagens de referência dos acessórios
                  DEVE estar presente na foto do kit enviada. Liste como ausente qualquer acessório
                  de referência que não apareça na foto atual.""";
    }

    @Override
    protected String diferencasIgnoradas() {
        return "iluminação, ângulo e enquadramento da foto, ordem/disposição dos acessórios, "
             + "poeira e reflexos — desde que todos os itens do kit permaneçam identificáveis";
    }
}
