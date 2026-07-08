package imd.ufrn.com.br.smart_space_booking.framework.prompts;

import imd.ufrn.com.br.smart_space_booking.framework.model.RegraAvaliacao;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Esqueleto (Template Method) de construção dos prompts de auditoria.
 *
 * O framework é dono do que NÃO pode variar entre hotspots:
 *  - o fluxo em 3 etapas (validação da imagem → identidade → avaliação);
 *  - as regras de formatação JSON;
 *  - o CONTRATO JSON de saída, que precisa bater EXATAMENTE com AuditoriaResultadoDTO;
 *  - a contagem explícita de quantas imagens são de referência, para o Gemini nunca
 *    ter que "adivinhar" a partir de qual posição começam as fotos do usuário.
 *
 * Cada hotspot preenche apenas os "slots" de domínio (o que é uma imagem válida,
 * quais elementos definem a identidade do recurso, o que ignorar na comparação).
 * Assim, um autor de hotspot não consegue, por engano, emitir um JSON com formato
 * diferente do que o parser espera.
 */
public abstract class AuditoriaPromptTemplate {

    // ─── Template Methods — o framework define O QUE e EM QUE ORDEM ────────────

    /**
     * @param quantidadeReferencia número de imagens de referência do recurso que
     *                             precedem as imagens do usuário no request ao Gemini —
     *                             necessário para ancorar a ordem das fotos do usuário
     *                             corretamente, já que hotspots podem ter 0, 1 ou N
     *                             imagens de referência cadastradas.
     */
    public final String promptCheckIn(int quantidadeReferencia) {
        return PAPEL.formatted(recurso())
                + INTRO_CHECKIN.formatted(quantidadeReferencia, quantidadeReferencia + 1)
                + ETAPA_VALIDACAO.formatted(descricaoImagemValida())
                + etapaIdentidade()
                + RESPOSTA_IDENTIDADE_FALHA
                + ETAPA_AVALIACAO_CHECKIN
                + CONTRATO_JSON_CHECKIN;
    }

    public final String promptCheckOut(List<RegraAvaliacao> regras, int quantidadeReferencia) {
        return PAPEL.formatted(recurso())
                + INTRO_CHECKOUT.formatted(quantidadeReferencia, quantidadeReferencia + 1)
                + ETAPA_VALIDACAO.formatted(descricaoImagemValida())
                + etapaIdentidade()
                + RESPOSTA_IDENTIDADE_FALHA
                + ETAPA_AVALIACAO_CHECKOUT
                + criteriosFormatados(regras)
                + CONTRATO_JSON_CHECKOUT;
    }

    // ─── Slots variáveis por hotspot — cada um implementa ─────────────────────

    /** Nome do recurso auditado. Ex.: "espaços corporativos (salas)", "veículos". */
    protected abstract String recurso();

    /**
     * O que caracteriza uma imagem válida, e a ordem esperada das fotos ENVIADAS
     * PELO USUÁRIO — nunca descreva posição absoluta no conjunto de imagens do
     * request (as de referência entram antes e deslocam a contagem); descreva
     * sempre em relação às imagens do usuário, ex.: "a 1ª foto enviada pelo
     * usuário deve ser X, a 2ª deve ser Y".
     */
    protected abstract String descricaoImagemValida();

    /** Elementos ESTRUTURAIS/FIXOS a comparar para confirmar a identidade do recurso. */
    protected abstract String elementosIdentidade();

    /** Diferenças que devem ser IGNORADAS na comparação de identidade. */
    protected abstract String diferencasIgnoradas();

    /** Hook opcional: quantos elementos estruturais distintos exigir para confirmar identidade. */
    protected int minimoElementosIdentidade() {
        return 3;
    }

    // ─── Partes fixas montadas a partir dos slots ─────────────────────────────

    private String etapaIdentidade() {
        return ETAPA_IDENTIDADE.formatted(
                elementosIdentidade(),
                diferencasIgnoradas(),
                minimoElementosIdentidade());
    }

    private String criteriosFormatados(List<RegraAvaliacao> regras) {
        if (regras == null || regras.isEmpty()) {
            return "- Avaliação geral: limpeza, organização e integridade dos itens.";
        }
        return regras.stream()
                .map(r -> String.format("- ID %d | %s: %s", r.getId(), r.getNome(), r.getDescricao()))
                .collect(Collectors.joining("\n"));
    }

    // ─── Blocos fixos do framework (constantes) ───────────────────────────────

    private static final String PAPEL = """
            Você é um auditor de %s.
            """;

    private static final String INTRO_CHECKIN = """

            As PRIMEIRAS %d imagem(ns) deste conjunto mostram o estado PADRÃO esperado
            (fotos de referência cadastradas no sistema para este recurso específico).
            A PARTIR DA IMAGEM DE NÚMERO %d (contando desde a primeira imagem enviada
            nesta mensagem), todas as imagens seguintes foram fotografadas AGORA pelo
            usuário no momento do check-in. Use essa contagem exata — não assuma que
            a foto de referência é uma foto do usuário.

            Siga as etapas abaixo em ordem. Se qualquer etapa falhar, pare e retorne imediatamente.
            """;

    private static final String INTRO_CHECKOUT = """

            As PRIMEIRAS %d imagem(ns) deste conjunto mostram o estado PADRÃO esperado
            (fotos de referência cadastradas no sistema para este recurso específico).
            A PARTIR DA IMAGEM DE NÚMERO %d (contando desde a primeira imagem enviada
            nesta mensagem), todas as imagens seguintes mostram o estado ATUAL do
            recurso APÓS o uso do usuário. Use essa contagem exata — não assuma que
            a foto de referência é uma foto do usuário.

            Siga as etapas abaixo em ordem. Se qualquer etapa falhar, pare e retorne imediatamente.
            """;

    private static final String ETAPA_VALIDACAO = """

            ETAPA 1 — VALIDAÇÃO DA IMAGEM:
            Verifique se as imagens enviadas pelo usuário (ou seja, ignorando as de
            referência) mostram claramente %s.
            Se não corresponder, defina "imagemValida": false e "recursoCorreto": false na resposta.
            """;

    private static final String ETAPA_IDENTIDADE = """

            ETAPA 2 — VERIFICAÇÃO DE IDENTIDADE DO RECURSO:
            Compare os elementos ESTRUTURAIS e FIXOS entre as fotos de referência e as enviadas:
            %s

            Ignore diferenças de: %s — esses mudam naturalmente entre usos.

            Se o recurso fotografado NÃO for o mesmo das fotos de referência, defina
            "recursoCorreto": false. Seja criterioso: exija correspondência de pelo menos
            %d elementos estruturais distintos.

            ATENÇÃO: Responda SEMPRE com JSON estritamente válido. Todas as chaves e valores de
            texto DEVEM estar entre aspas duplas. NUNCA use aspas simples nem omita aspas das chaves.
            """;

    private static final String RESPOSTA_IDENTIDADE_FALHA = """

            Se recursoCorreto: false, responda APENAS:
            {
              "imagemValida": false,
              "recursoCorreto": false,
              "aprovado": false,
              "observacaoGeral": "O recurso fotografado não corresponde ao reservado.",
              "criterios": []
            }
            """;

    private static final String ETAPA_AVALIACAO_CHECKIN = """

            ETAPA 3 — AVALIAÇÃO DO ESTADO ATUAL:
            O recurso está em condições adequadas para ser utilizado? Verifique organização,
            limpeza e integridade dos itens comparando com o padrão de referência.
            """;

    private static final String ETAPA_AVALIACAO_CHECKOUT = """

            ETAPA 3 — AVALIAÇÃO PÓS-USO:
            Avalie como o usuário deixou o recurso comparando com o padrão de referência.
            Atribua uma nota de 0 a 10 para CADA critério abaixo.
            Seja rigoroso: 0 = péssimo, 5 = aceitável, 10 = perfeito.

            CRITÉRIOS PARA AVALIAÇÃO:
            """;

    private static final String CONTRATO_JSON_CHECKIN = """

            Responda SOMENTE com um JSON válido, sem texto adicional:
            {
              "imagemValida": true,
              "recursoCorreto": true,
              "aprovado": true,
              "observacaoGeral": "Descreva brevemente o estado atual em até 200 caracteres."
            }

            Regras finais:
            - "imagemValida": false se a imagem não corresponder ao esperado.
            - "recursoCorreto": false se o recurso fotografado não for o das referências.
            - "aprovado": true se o estado está condizente com o padrão esperado.
            """;

    private static final String CONTRATO_JSON_CHECKOUT = """

            Responda SOMENTE com um JSON válido, sem texto adicional:
            {
              "imagemValida": true,
              "recursoCorreto": true,
              "aprovado": true,
              "observacaoGeral": "Resumo geral do estado do recurso em até 200 caracteres.",
              "criterios": [
                { "id": <id_do_criterio>, "nome": "<nome>", "nota": <0-10>, "observacao": "Justificativa em até 100 caracteres." }
              ]
            }

            Regras finais:
            - "imagemValida": false se a imagem não corresponder ao esperado.
            - "recursoCorreto": false se o recurso fotografado não for o das referências.
            - "aprovado": true se o estado geral for aceitável, false se houver problema grave.
            - "criterios": UM objeto para CADA critério listado acima, na mesma ordem.
            - "nota": número inteiro de 0 a 10.
            - "observacao": justifique brevemente a nota atribuída.
            """;
}