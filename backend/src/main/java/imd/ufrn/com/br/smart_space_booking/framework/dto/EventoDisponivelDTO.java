package imd.ufrn.com.br.smart_space_booking.framework.dto;

import imd.ufrn.com.br.smart_space_booking.framework.evento.EventoTrustScore;

public record EventoDisponivelDTO(String chave, String descricao, int deltaPadrao, Integer parametroPadrao) {
    public static EventoDisponivelDTO fromEvento(EventoTrustScore e) {
        return new EventoDisponivelDTO(e.chave(), e.descricao(), e.deltaPadrao(), e.parametroPadrao());
    }
}
