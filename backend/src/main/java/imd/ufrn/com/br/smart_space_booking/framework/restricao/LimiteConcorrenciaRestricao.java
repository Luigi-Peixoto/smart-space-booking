package imd.ufrn.com.br.smart_space_booking.framework.restricao;

import java.util.List;

import imd.ufrn.com.br.smart_space_booking.framework.model.RegraTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.model.Reserva;
import imd.ufrn.com.br.smart_space_booking.framework.model.Usuario;

/**
 * Restrição genérica e reutilizável: limita quantas reservas ativas do mesmo
 * tipo de recurso um usuário pode ter ao mesmo tempo, exigindo um TrustScore
 * mínimo pra exceder esse limite. Qualquer hotspot pode instanciar — não
 * depende de nenhum tipo concreto de recurso.
 */
public class LimiteConcorrenciaRestricao implements RestricaoTrustScore {

    private final String chave;
    private final int limitePadrao;
    private final int scoreMinimoParaExcederPadrao;

    public LimiteConcorrenciaRestricao(String chave, int limitePadrao, int scoreMinimoParaExcederPadrao) {
        this.chave = chave;
        this.limitePadrao = limitePadrao;
        this.scoreMinimoParaExcederPadrao = scoreMinimoParaExcederPadrao;
    }

    @Override
    public String chave() {
        return chave;
    }

    @Override
    public String descricao() {
        return "Reservas ativas simultâneas do mesmo tipo permitidas por usuário.";
    }

    @Override
    public String avaliar(Usuario usuario, Reserva reservaTentativa, List<Reserva> reservasAtivasMesmoTipo,
                          RegraTrustScore regraConfigurada) {
        int limite = regraConfigurada != null ? regraConfigurada.getValorPrincipal() : limitePadrao;
        int scoreMinimoParaExceder = regraConfigurada != null && regraConfigurada.getValorSecundario() != null
                ? regraConfigurada.getValorSecundario() : scoreMinimoParaExcederPadrao;

        int reservasAtivas = reservasAtivasMesmoTipo != null ? reservasAtivasMesmoTipo.size() : 0;
        Integer scoreAtual = usuario.getTrustScore();
        int score = scoreAtual != null ? scoreAtual : 100;

        if (reservasAtivas >= limite && score < scoreMinimoParaExceder) {
            return "Limite de " + limite + " reserva(s) simultânea(s) atingido. "
                    + "Para exceder, seu TrustScore precisa ser pelo menos " + scoreMinimoParaExceder
                    + " (atual: " + score + ").";
        }
        return null;
    }

    @Override
    public int valorPrincipalPadrao() {
        return limitePadrao;
    }

    @Override
    public int valorSecundarioPadrao() {
        return scoreMinimoParaExcederPadrao;
    }
}
