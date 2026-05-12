package imd.ufrn.com.br.smart_space_booking.prompts;

import imd.ufrn.com.br.smart_space_booking.model.RegraAvaliacao;

import java.util.List;
import java.util.stream.Collectors;

public class AuditoriaPrompts {

    private AuditoriaPrompts() {}

    // ─── Bloco de identidade reutilizado nos dois prompts ────────────────────

    private static final String BLOCO_IDENTIDADE = """

            ETAPA 1 — VALIDAÇÃO DA IMAGEM:
            Verifique se as imagens enviadas pelo usuário mostram claramente
            um ambiente interno (sala, escritório ou espaço de trabalho).
            Se não for um ambiente interno, defina "imagemValida": false e "salaCorreta": false na resposta.

            ETAPA 2 — VERIFICAÇÃO DE IDENTIDADE DA SALA:
            Compare os elementos ESTRUTURAIS e FIXOS entre as fotos de referência
            e as fotos enviadas pelo usuário:
            - Layout geral e proporções do espaço
            - Posição e formato das janelas e portas
            - Cor e textura das paredes e piso
            - Mobiliário fixo (mesas, armários embutidos, quadros brancos)
            - Equipamentos permanentes (projetores, TVs fixas, ar-condicionado)

            Ignore diferenças de: iluminação, objetos pessoais, posição de cadeiras,
            itens sobre as mesas — esses mudam naturalmente entre usos.

            Se a sala fotografada NÃO for a mesma sala das fotos de referência,
            defina "salaCorreta": false na resposta. Seja criterioso: salas diferentes
            de um mesmo escritório tendem a ser parecidas, então exija
            correspondência de pelo menos 3 elementos estruturais distintos.

            ATENÇÃO: Responda SEMPRE com JSON estritamente válido.
            Todas as chaves e valores de texto DEVEM estar entre aspas duplas.
            NUNCA use aspas simples. NUNCA omita as aspas das chaves.
            """;

    // ─── CHECK-IN ────────────────────────────────────────────────────────────

    private static final String PROMPT_CHECKIN = """
            Você é um auditor de espaços corporativos.

            As PRIMEIRAS imagens mostram o estado PADRÃO esperado da sala (fotos de referência do sistema).
            As imagens SEGUINTES foram fotografadas agora pelo usuário no momento do check-in.

            Siga as etapas abaixo em ordem. Se qualquer etapa falhar, pare e retorne imediatamente.
            """
            + BLOCO_IDENTIDADE +
            """

            Se salaCorreta: false, responda APENAS:
            {
              "imagemValida": false,
              "salaCorreta": false,
              "aprovado": false,
              "observacaoGeral": "A sala fotografada não corresponde à sala reservada."
            }

            ETAPA 3 — AVALIAÇÃO DO ESTADO ATUAL:
            A sala está em condições adequadas para receber o usuário?
            Verifique organização, limpeza e integridade dos itens comparando com o padrão.

            Responda SOMENTE com um JSON válido, sem texto adicional:
            {
              "imagemValida": true,
              "salaCorreta": true,
              "aprovado": true,
              "observacaoGeral": "Descreva brevemente o estado atual em até 200 caracteres."
            }

            Regras finais:
            - "imagemValida": false se não for ambiente interno.
            - "salaCorreta": false se a sala fotografada não for a mesma das referências.
            - "aprovado": true se o estado está condizente com o padrão esperado.
            - "observacaoGeral": destaque diferenças relevantes em relação ao padrão.
            """;

    // ─── CHECK-OUT ───────────────────────────────────────────────────────────

    private static final String PROMPT_CHECKOUT_CABECALHO = """
            Você é um auditor de espaços corporativos.

            As PRIMEIRAS imagens mostram o estado PADRÃO esperado da sala (fotos de referência do sistema).
            As imagens SEGUINTES mostram o estado ATUAL da sala APÓS o uso do usuário.

            Siga as etapas abaixo em ordem. Se qualquer etapa falhar, pare e retorne imediatamente.
            """
            + BLOCO_IDENTIDADE +
            """

            Se salaCorreta: false, responda APENAS:
            {
              "imagemValida": false,
              "salaCorreta": false,
              "aprovado": false,
              "observacaoGeral": "A sala fotografada não corresponde à sala reservada.",
              "criterios": []
            }

            ETAPA 3 — AVALIAÇÃO PÓS-USO:
            Avalie como o usuário deixou a sala comparando com o padrão de referência.
            Atribua uma nota de 0 a 10 para CADA critério abaixo.
            Seja rigoroso: 0 = péssimo, 5 = aceitável, 10 = perfeito.

            CRITÉRIOS PARA AVALIAÇÃO:
            """;

    private static final String PROMPT_CHECKOUT_RODAPE = """

            Responda SOMENTE com um JSON válido, sem texto adicional:
            {
              "imagemValida": true,
              "salaCorreta": true,
              "aprovado": true,
              "observacaoGeral": "Resumo geral do estado da sala em até 200 caracteres.",
              "criterios": [
                { "id": <id_do_criterio>, "nome": "<nome>", "nota": <0-10>, "observacao": "Justificativa em até 100 caracteres." }
              ]
            }

            Regras finais:
            - "imagemValida": false se não for ambiente interno.
            - "salaCorreta": false se a sala fotografada não for a mesma das referências.
            - "aprovado": true se o estado geral for aceitável, false se houver problema grave.
            - "criterios": UM objeto para CADA critério listado acima, na mesma ordem.
            - "nota": número inteiro de 0 a 10.
            - "observacao": justifique brevemente a nota atribuída.
            """;

    // ─── Métodos públicos ────────────────────────────────────────────────────

    public static String promptCheckIn() {
        return PROMPT_CHECKIN;
    }

    public static String promptCheckOut(List<RegraAvaliacao> regras) {
        if (regras == null || regras.isEmpty()) {
            return promptCheckOutSemCriterios();
        }

        String listaCriterios = regras.stream()
                .map(r -> String.format("- ID %d | %s: %s", r.getId(), r.getNome(), r.getDescricao()))
                .collect(Collectors.joining("\n"));

        return PROMPT_CHECKOUT_CABECALHO + listaCriterios + PROMPT_CHECKOUT_RODAPE;
    }

    /** Fallback quando nenhuma regra está ativa — avaliação genérica com identidade */
    private static String promptCheckOutSemCriterios() {
        return PROMPT_CHECKOUT_CABECALHO
                + "- Avaliação geral: limpeza, organização e integridade dos equipamentos."
                + PROMPT_CHECKOUT_RODAPE;
    }
}