package imd.ufrn.com.br.smart_space_booking.instancia_equipamento.strategy;

import java.util.List;

import imd.ufrn.com.br.smart_space_booking.framework.model.RegraTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.model.Reserva;
import imd.ufrn.com.br.smart_space_booking.framework.model.Usuario;
import imd.ufrn.com.br.smart_space_booking.framework.restricao.RestricaoTrustScore;
import imd.ufrn.com.br.smart_space_booking.instancia_equipamento.model.Equipamento;

/**
 * Restrição própria do Equipamento — quanto maior o kit, mais crítico é o recurso, 
 * e mais alto precisa ser o TrustScore do usuário pra reservar. Se o kit for grande demais, 
 * a reserva é bloqueada.
 */
public class LimiteTamanhoKitRestricao implements RestricaoTrustScore {

    private final String chave;
    private final int tamanhoMaximoPadrao;
    private final int scoreMinimoParaExcederPadrao;

    public LimiteTamanhoKitRestricao(String chave, int tamanhoMaximoPadrao, int scoreMinimoParaExcederPadrao) {
        this.chave = chave;
        this.tamanhoMaximoPadrao = tamanhoMaximoPadrao;
        this.scoreMinimoParaExcederPadrao = scoreMinimoParaExcederPadrao;
    }

    @Override
    public String chave() {
        return chave;
    }

    @Override
    public String descricao() {
        return "Tamanho máximo do kit (nº de subitens) que pode ser reservado.";
    }

    @Override
    public String avaliar(Usuario usuario, Reserva reservaTentativa, List<Reserva> reservasAtivasMesmoTipo,
                          RegraTrustScore regraConfigurada) {
        Equipamento equipamento = (Equipamento) reservaTentativa.getRecurso();
        int tamanhoKit = equipamento.getSubItens() != null ? equipamento.getSubItens().size() : 0;

        int tamanhoMaximo = regraConfigurada != null ? regraConfigurada.getValorPrincipal() : tamanhoMaximoPadrao;
        int scoreMinimoParaExceder = regraConfigurada != null && regraConfigurada.getValorSecundario() != null
                ? regraConfigurada.getValorSecundario() : scoreMinimoParaExcederPadrao;

        Integer scoreAtual = usuario.getTrustScore();
        int score = scoreAtual != null ? scoreAtual : 100;

        if (tamanhoKit > tamanhoMaximo && score < scoreMinimoParaExceder) {
            return "Kit de " + tamanhoKit + " item(ns) excede o limite de " + tamanhoMaximo + " para seu TrustScore atual. "
                    + "Para exceder, seu TrustScore precisa ser pelo menos " + scoreMinimoParaExceder
                    + " (atual: " + score + ").";
        }
        return null;
    }

    @Override
    public int valorPrincipalPadrao() {
        return tamanhoMaximoPadrao;
    }

    @Override
    public int valorSecundarioPadrao() {
        return scoreMinimoParaExcederPadrao;
    }
}
