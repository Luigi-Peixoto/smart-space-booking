package imd.ufrn.com.br.smart_space_booking.framework.strategy;

/**
 * Resultado de uma TrustScoreStrategy ao avaliar um evento: se o TrustScore deve
 * mudar e por quê. O template (ReservaService/TrustScoreService) só persiste.
 */
public record TrustScoreDecisao(boolean aplicavel, int delta, String descricao) {

    public static TrustScoreDecisao aplicavel(int delta, String descricao) {
        return new TrustScoreDecisao(true, delta, descricao);
    }

    public static TrustScoreDecisao naoAplicavel() {
        return new TrustScoreDecisao(false, 0, null);
    }
}
