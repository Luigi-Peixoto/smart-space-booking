package imd.ufrn.com.br.smart_space_booking.instancia_sala.strategy;

import java.util.List;
import java.util.Locale;

import org.hibernate.Hibernate;

import imd.ufrn.com.br.smart_space_booking.framework.model.RegraTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.model.Reserva;
import imd.ufrn.com.br.smart_space_booking.framework.model.Usuario;
import imd.ufrn.com.br.smart_space_booking.framework.restricao.RestricaoTrustScore;
import imd.ufrn.com.br.smart_space_booking.instancia_sala.model.Sala;

/**
 * Restrição boba própria da Sala: quem reserva uma sala sem "Ar Condicionado"
 * na lista de características precisa ter TrustScore alto — a lógica (torta,
 * de propósito) é que quem desiste no meio do calor cancela de última hora, e
 * a gente já não confia em quem cancela.
 *
 * {valorPrincipal} não é usado por esta restrição (não há "limite"
 * numérico, é um gate categórico) — só {valorSecundario} (score mínimo)
 * importa; mantido preenchido só pra caber na forma genérica de RegraTrustScore.
 */
public class RestricaoSemArCondicionado implements RestricaoTrustScore {

    private static final String AR_CONDICIONADO = "ar condicionado";

    private final String chave;
    private final int scoreMinimoParaExcederPadrao;

    public RestricaoSemArCondicionado(String chave, int scoreMinimoParaExcederPadrao) {
        this.chave = chave;
        this.scoreMinimoParaExcederPadrao = scoreMinimoParaExcederPadrao;
    }

    @Override
    public String chave() {
        return chave;
    }

    @Override
    public String descricao() {
        return "TrustScore mínimo pra reservar uma sala sem ar condicionado (valor limite não é usado).";
    }

    @Override
    public String avaliar(Usuario usuario, Reserva reservaTentativa, List<Reserva> reservasAtivasMesmoTipo,
                          RegraTrustScore regraConfigurada) {
        Sala sala = (Sala) Hibernate.unproxy(reservaTentativa.getRecurso());
        List<String> caracteristicas = sala.getCaracteristicas();
        boolean temArCondicionado = caracteristicas != null && caracteristicas.stream()
                .anyMatch(c -> c != null && c.toLowerCase(Locale.ROOT).contains(AR_CONDICIONADO));

        if (temArCondicionado) {
            return null;
        }

        int scoreMinimoParaExceder = regraConfigurada != null && regraConfigurada.getValorSecundario() != null
                ? regraConfigurada.getValorSecundario() : scoreMinimoParaExcederPadrao;
        Integer scoreAtual = usuario.getTrustScore();
        int score = scoreAtual != null ? scoreAtual : 100;

        if (score < scoreMinimoParaExceder) {
            return "Esta sala não tem ar condicionado — só quem já provou que não desiste no meio do calor "
                    + "reserva sem TrustScore alto. Mínimo exigido: " + scoreMinimoParaExceder
                    + " (atual: " + score + ").";
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
