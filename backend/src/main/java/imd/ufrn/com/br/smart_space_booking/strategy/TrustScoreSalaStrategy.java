package imd.ufrn.com.br.smart_space_booking.strategy;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

import imd.ufrn.com.br.smart_space_booking.framework.enums.TrustScoreEvento;
import imd.ufrn.com.br.smart_space_booking.framework.model.RegraTrustScoreEvento;
import imd.ufrn.com.br.smart_space_booking.framework.model.Recurso;
import imd.ufrn.com.br.smart_space_booking.framework.strategy.TrustScoreContexto;
import imd.ufrn.com.br.smart_space_booking.framework.strategy.TrustScoreDecisao;
import imd.ufrn.com.br.smart_space_booking.framework.strategy.TrustScoreStrategy;
import imd.ufrn.com.br.smart_space_booking.model.Sala;

@Component
public class TrustScoreSalaStrategy implements TrustScoreStrategy {

    // Fallbacks usados quando o admin ainda não cadastrou a RegraTrustScoreEvento do evento.
    private static final long JANELA_CANCELAMENTO_PADRAO_EM_HORAS = 2L;
    private static final long LIMITE_CANCELAMENTOS_PADRAO = 3L;
    private static final int DELTA_PADRAO_NO_SHOW = -15;
    private static final int DELTA_PADRAO_CANCELAMENTO_TARDIO = -10;
    private static final int DELTA_PADRAO_EXCESSO_CANCELAMENTOS = -20;

    @Override
    public boolean suporta(Recurso recurso) {
        return Hibernate.unproxy(recurso) instanceof Sala;
    }

    @Override
    public TrustScoreDecisao avaliar(TrustScoreEvento evento, TrustScoreContexto contexto) {
        return switch (evento) {
            case CANCELAMENTO_TARDIO -> avaliarCancelamentoTardio(contexto);
            case NO_SHOW -> avaliarNoShow(contexto);
            case EXCESSO_CANCELAMENTOS -> avaliarExcessoCancelamentos(contexto);
        };
    }

    private TrustScoreDecisao avaliarCancelamentoTardio(TrustScoreContexto contexto) {
        RegraTrustScoreEvento regra = contexto.regra();
        long janela = parametroOu(regra, JANELA_CANCELAMENTO_PADRAO_EM_HORAS);
        long horas = contexto.horasDeAntecedencia();

        if (horas >= janela) {
            return TrustScoreDecisao.naoAplicavel();
        }

        int delta = regra != null ? regra.getDelta() : DELTA_PADRAO_CANCELAMENTO_TARDIO;
        return TrustScoreDecisao.aplicavel(delta,
                "Cancelamento com " + horas + "h de antecedência (janela: " + janela + "h).");
    }

    private TrustScoreDecisao avaliarNoShow(TrustScoreContexto contexto) {
        RegraTrustScoreEvento regra = contexto.regra();
        int delta = regra != null ? regra.getDelta() : DELTA_PADRAO_NO_SHOW;
        return TrustScoreDecisao.aplicavel(delta, "Reserva cancelada automaticamente por no-show.");
    }

    private TrustScoreDecisao avaliarExcessoCancelamentos(TrustScoreContexto contexto) {
        RegraTrustScoreEvento regra = contexto.regra();
        long limite = parametroOu(regra, LIMITE_CANCELAMENTOS_PADRAO);
        long cancelamentos = contexto.cancelamentosNaSemana();

        if (cancelamentos <= limite) {
            return TrustScoreDecisao.naoAplicavel();
        }

        int delta = regra != null ? regra.getDelta() : DELTA_PADRAO_EXCESSO_CANCELAMENTOS;
        return TrustScoreDecisao.aplicavel(delta,
                "Excesso de cancelamentos na semana (" + cancelamentos + " cancelamentos, limite " + limite + ").");
    }

    private long parametroOu(RegraTrustScoreEvento regra, long padrao) {
        return regra != null && regra.getParametro() != null ? regra.getParametro() : padrao;
    }
}
