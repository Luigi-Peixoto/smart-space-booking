package imd.ufrn.com.br.smart_space_booking.framework.restricao;

import java.time.temporal.ChronoUnit;
import java.util.List;

import imd.ufrn.com.br.smart_space_booking.framework.model.RegraTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.model.Reserva;
import imd.ufrn.com.br.smart_space_booking.framework.model.Usuario;

/**
 * Restrição genérica e reutilizável: limita a duração (em horas) de uma única
 * reserva, exigindo um TrustScore mínimo pra reservas mais longas que o
 * limite. Só usa os horários da própria reserva — qualquer hotspot pode
 * instanciar.
 */
public class LimiteDuracaoReservaRestricao implements RestricaoTrustScore {

    private final String chave;
    private final long horasMaximaPadrao;
    private final int scoreMinimoParaExcederPadrao;

    public LimiteDuracaoReservaRestricao(String chave, long horasMaximaPadrao, int scoreMinimoParaExcederPadrao) {
        this.chave = chave;
        this.horasMaximaPadrao = horasMaximaPadrao;
        this.scoreMinimoParaExcederPadrao = scoreMinimoParaExcederPadrao;
    }

    @Override
    public String chave() {
        return chave;
    }

    @Override
    public String descricao() {
        return "Duração máxima (em horas) de uma única reserva.";
    }

    @Override
    public String avaliar(Usuario usuario, Reserva reservaTentativa, List<Reserva> reservasAtivasMesmoTipo,
                          RegraTrustScore regraConfigurada) {
        long horasMaxima = regraConfigurada != null ? regraConfigurada.getValorPrincipal() : horasMaximaPadrao;
        int scoreMinimoParaExceder = regraConfigurada != null && regraConfigurada.getValorSecundario() != null
                ? regraConfigurada.getValorSecundario() : scoreMinimoParaExcederPadrao;

        long horas = ChronoUnit.HOURS.between(reservaTentativa.getInicioDateTime(), reservaTentativa.getFimDateTime());
        Integer scoreAtual = usuario.getTrustScore();
        int score = scoreAtual != null ? scoreAtual : 100;

        if (horas > horasMaxima && score < scoreMinimoParaExceder) {
            return "Reserva de " + horas + "h excede o limite de " + horasMaxima + "h para seu TrustScore atual. "
                    + "Para exceder, seu TrustScore precisa ser pelo menos " + scoreMinimoParaExceder
                    + " (atual: " + score + ").";
        }
        return null;
    }

    @Override
    public int valorPrincipalPadrao() {
        return (int) horasMaximaPadrao;
    }

    @Override
    public int valorSecundarioPadrao() {
        return scoreMinimoParaExcederPadrao;
    }
}
