package imd.ufrn.com.br.smart_space_booking.instancia_sala.prompts;

import imd.ufrn.com.br.smart_space_booking.framework.prompts.AuditoriaPromptTemplate;
import org.springframework.stereotype.Component;

/**
 * Preenche os slots variáveis do AuditoriaPromptTemplate com a semântica de salas.
 * Todo o esqueleto (etapas, formatação e contrato JSON) vem da classe base.
 */
@Component
public class SalaPromptTemplate extends AuditoriaPromptTemplate {

    @Override
    protected String recurso() {
        return "espaços corporativos (salas)";
    }

    @Override
    protected String descricaoImagemValida() {
        return "um ambiente interno (sala, escritório ou espaço de trabalho)";
    }

    @Override
    protected String elementosIdentidade() {
        return """
                - Layout geral e proporções do espaço
                - Posição e formato das janelas e portas
                - Cor e textura das paredes e piso
                - Mobiliário fixo (mesas, armários embutidos, quadros brancos)
                - Equipamentos permanentes (projetores, TVs fixas, ar-condicionado)""";
    }

    @Override
    protected String diferencasIgnoradas() {
        return "iluminação, objetos pessoais, posição de cadeiras, itens sobre as mesas";
    }
}