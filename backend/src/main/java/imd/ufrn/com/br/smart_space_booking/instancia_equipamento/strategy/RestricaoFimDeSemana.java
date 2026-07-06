package imd.ufrn.com.br.smart_space_booking.instancia_equipamento.strategy;

import java.time.DayOfWeek;
import java.util.List;

import imd.ufrn.com.br.smart_space_booking.framework.model.RegraTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.model.Reserva;
import imd.ufrn.com.br.smart_space_booking.framework.model.Usuario;
import imd.ufrn.com.br.smart_space_booking.framework.restricao.RestricaoTrustScore;

/**
 * Restrição boba própria do Equipamento: reservar pra sábado ou domingo exige
 * TrustScore alto — se o equipamento sumir ou quebrar no fim de semana,
 * ninguém do suporte vai atrás até segunda-feira.
 *
 * {@code valorPrincipal} não é usado (gate categórico, sem "limite" numérico);
 * só {@code valorSecundario} (score mínimo) importa.
 */
public class RestricaoFimDeSemana implements RestricaoTrustScore {

    private final String chave;
    private final int scoreMinimoParaExcederPadrao;

    public RestricaoFimDeSemana(String chave, int scoreMinimoParaExcederPadrao) {
        this.chave = chave;
        this.scoreMinimoParaExcederPadrao = scoreMinimoParaExcederPadrao;
    }

    @Override
    public String chave() {
        return chave;
    }

    @Override
    public String descricao() {
        return "TrustScore mínimo pra reservar equipamento com início no sábado ou domingo (valor limite não é usado).";
    }

    @Override
    public String avaliar(Usuario usuario, Reserva reservaTentativa, List<Reserva> reservasAtivasMesmoTipo,
                          RegraTrustScore regraConfigurada) {
        DayOfWeek dia = reservaTentativa.getInicioDateTime().getDayOfWeek();
        boolean fimDeSemana = dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY;

        if (!fimDeSemana) {
            return null;
        }

        int scoreMinimoParaExceder = regraConfigurada != null && regraConfigurada.getValorSecundario() != null
                ? regraConfigurada.getValorSecundario() : scoreMinimoParaExcederPadrao;
        Integer scoreAtual = usuario.getTrustScore();
        int score = scoreAtual != null ? scoreAtual : 100;

        if (score < scoreMinimoParaExceder) {
            return "Reservas de equipamento no fim de semana exigem TrustScore alto — se sumir, ninguém vai atrás "
                    + "até segunda. Mínimo exigido: " + scoreMinimoParaExceder + " (atual: " + score + ").";
        }
        return null;
    }

    /** Não usado por esta restrição — devolve um valor fixo só pra satisfazer a forma genérica de RegraTrustScore. */
    @Override
    public int valorPrincipalPadrao() {
        return 1;
    }

    @Override
    public boolean valorPrincipalUsado() {
        return false;
    }

    @Override
    public int valorSecundarioPadrao() {
        return scoreMinimoParaExcederPadrao;
    }
}
