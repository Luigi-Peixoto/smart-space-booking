package imd.ufrn.com.br.smart_space_booking.framework.enums;

/**
 * Nível de exigência de TrustScore para reservar um recurso, derivado da
 * sensibilidade do recurso (ver {@code TrustScoreStrategy.fatorSeveridade}).
 * Conjunto fechado — o admin ajusta o score mínimo de cada nível (via
 * RegraTrustScore), não os níveis em si.
 *
 * O score mínimo padrão de cada nível mora aqui — fonte única usada tanto
 * pelo fallback de {@code TrustScoreStrategy.scoreMinimoPadrao} (gate por
 * recurso) quanto pelo cálculo de {@code Usuario.nivelRestricao} (snapshot
 * global do usuário, independente de hotspot).
 */
public enum NivelExigencia {
    BAIXA(0),
    MEDIA(40),
    ALTA(70);

    private final int scoreMinimoPadrao;

    NivelExigencia(int scoreMinimoPadrao) {
        this.scoreMinimoPadrao = scoreMinimoPadrao;
    }

    public int scoreMinimoPadrao() {
        return scoreMinimoPadrao;
    }
}
