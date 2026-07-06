package imd.ufrn.com.br.smart_space_booking.framework.dto;

import imd.ufrn.com.br.smart_space_booking.framework.restricao.RestricaoTrustScore;

public record RestricaoDisponivelDTO(String chave, String descricao, int valorPrincipalPadrao,
                                     boolean valorPrincipalUsado, int valorSecundarioPadrao) {
    public static RestricaoDisponivelDTO fromRestricao(RestricaoTrustScore r) {
        return new RestricaoDisponivelDTO(r.chave(), r.descricao(), r.valorPrincipalPadrao(),
                r.valorPrincipalUsado(), r.valorSecundarioPadrao());
    }
}
