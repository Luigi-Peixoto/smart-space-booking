package imd.ufrn.com.br.smart_space_booking.instancia_veiculo.strategy;

import java.util.List;

import imd.ufrn.com.br.smart_space_booking.framework.model.RegraTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.model.Reserva;
import imd.ufrn.com.br.smart_space_booking.framework.model.Usuario;
import imd.ufrn.com.br.smart_space_booking.framework.restricao.RestricaoTrustScore;

/**
 * Restrição própria do Veículo: sair de madrugada (a partir da hora
 * configurada, ex: 22h, até as 6h fixas) exige TrustScore alto — dirigir de
 * madrugada é mais arriscado, então só quem já provou ser confiável reserva
 * nesse horário sem restrição.
 *
 * {valorPrincipal} = hora (0-23) a partir da qual a reserva é
 * considerada noturna (o fim do período noturno é sempre fixo às 6h — não
 * configurável, só pra manter a restrição simples).
 */
public class RestricaoHorarioNoturno implements RestricaoTrustScore {

    private static final int HORA_FIM_NOTURNO = 6;

    private final String chave;
    private final int horaInicioNoturnoPadrao;
    private final int scoreMinimoParaExcederPadrao;

    public RestricaoHorarioNoturno(String chave, int horaInicioNoturnoPadrao, int scoreMinimoParaExcederPadrao) {
        this.chave = chave;
        this.horaInicioNoturnoPadrao = horaInicioNoturnoPadrao;
        this.scoreMinimoParaExcederPadrao = scoreMinimoParaExcederPadrao;
    }

    @Override
    public String chave() {
        return chave;
    }

    @Override
    public String descricao() {
        return "Hora (0-23) a partir da qual a reserva é considerada noturna (fim sempre às 6h); "
                + "TrustScore mínimo pra reservar nesse horário.";
    }

    @Override
    public String avaliar(Usuario usuario, Reserva reservaTentativa, List<Reserva> reservasAtivasMesmoTipo,
                          RegraTrustScore regraConfigurada) {
        int horaInicioNoturno = regraConfigurada != null ? regraConfigurada.getValorPrincipal() : horaInicioNoturnoPadrao;

        int hora = reservaTentativa.getInicioDateTime().getHour();
        boolean noturno = hora >= horaInicioNoturno || hora < HORA_FIM_NOTURNO;

        if (!noturno) {
            return null;
        }

        int scoreMinimoParaExceder = regraConfigurada != null && regraConfigurada.getValorSecundario() != null
                ? regraConfigurada.getValorSecundario() : scoreMinimoParaExcederPadrao;
        Integer scoreAtual = usuario.getTrustScore();
        int score = scoreAtual != null ? scoreAtual : 100;

        if (score < scoreMinimoParaExceder) {
            return "Reservar o veículo entre " + horaInicioNoturno + "h e " + HORA_FIM_NOTURNO
                    + "h exige TrustScore alto (dirigir de madrugada é mais arriscado). Mínimo exigido: "
                    + scoreMinimoParaExceder + " (atual: " + score + ").";
        }
        return null;
    }

    @Override
    public int valorPrincipalPadrao() {
        return horaInicioNoturnoPadrao;
    }

    @Override
    public int valorSecundarioPadrao() {
        return scoreMinimoParaExcederPadrao;
    }
}
