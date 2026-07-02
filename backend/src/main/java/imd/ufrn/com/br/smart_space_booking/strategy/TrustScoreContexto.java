package imd.ufrn.com.br.smart_space_booking.strategy;

import imd.ufrn.com.br.smart_space_booking.model.RegraTrustScoreEvento;

/**
 * Dados que uma TrustScoreStrategy precisa para decidir sobre um evento.
 * Puro (sem acesso a repository/service) — quem monta é o template (ReservaService),
 * que já buscou a RegraTrustScoreEvento e os fatos relevantes no banco.
 *
 * @param regra                 RegraTrustScoreEvento cadastrada para o evento — null se não existir (usa fallback da strategy)
 * @param horasDeAntecedencia   usado em CANCELAMENTO_TARDIO — null nos demais eventos
 * @param cancelamentosNaSemana usado em EXCESSO_CANCELAMENTOS — null nos demais eventos
 */
public record TrustScoreContexto(RegraTrustScoreEvento regra, Long horasDeAntecedencia, Long cancelamentosNaSemana) {

    public static TrustScoreContexto paraCancelamentoTardio(RegraTrustScoreEvento regra, long horasDeAntecedencia) {
        return new TrustScoreContexto(regra, horasDeAntecedencia, null);
    }

    public static TrustScoreContexto paraNoShow(RegraTrustScoreEvento regra) {
        return new TrustScoreContexto(regra, null, null);
    }

    public static TrustScoreContexto paraExcessoCancelamentos(RegraTrustScoreEvento regra, long cancelamentosNaSemana) {
        return new TrustScoreContexto(regra, null, cancelamentosNaSemana);
    }
}
