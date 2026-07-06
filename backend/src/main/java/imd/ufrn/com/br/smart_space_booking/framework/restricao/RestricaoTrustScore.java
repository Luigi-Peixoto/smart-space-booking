package imd.ufrn.com.br.smart_space_booking.framework.restricao;

import java.util.List;

import imd.ufrn.com.br.smart_space_booking.framework.model.RegraTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.model.Reserva;
import imd.ufrn.com.br.smart_space_booking.framework.model.Usuario;
import imd.ufrn.com.br.smart_space_booking.framework.strategy.TrustScoreStrategy;

/**
 * Interface que define uma restrição de TrustScore — usada pelo framework
 * para avaliar se uma reserva é permitida ou não, dado o TrustScore atual do
 * usuário. Cada hotspot pode instanciar suas próprias restrições, com seus
 * próprios limites e mensagens de bloqueio, ou usar as restrições genéricas
 * fornecidas pelo framework.
 */
public interface RestricaoTrustScore {

    /** Chave estável usada pelo admin pra configurar esta restrição via RegraTrustScore. */
    String chave();

    /** Explica o que o limite numérico mede — exibido na UI admin. */
    String descricao();

    /**
     * Avalia se a reserva é permitida ou não, dado o TrustScore atual do usuário e
     * o limite configurado (ou padrão) da restrição. Retorna null se permitido, ou uma mensagem de bloqueio se não permitido.
     * 
     */
    String avaliar(Usuario usuario, Reserva reservaTentativa, List<Reserva> reservasAtivasMesmoTipo, RegraTrustScore regraConfigurada);

    /** Valor limite padrão (usado quando o admin não configurou uma RegraTrustScore) — exibido na UI admin como sugestão. */
    int valorPrincipalPadrao();

    /** TrustScore mínimo pra exceder o limite, padrão — exibido na UI admin como sugestão. */
    int valorSecundarioPadrao();

    /**
     * Indica se o valor principal da restrição é usado na avaliação ou não. 
     * Algumas restrições são "gates" categóricos, que só usam o valor secundário (TrustScore mínimo) e ignoram o valor principal.
     */
    default boolean valorPrincipalUsado() {
        return true;
    }
}
