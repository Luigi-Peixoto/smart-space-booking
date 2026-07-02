package imd.ufrn.com.br.smart_space_booking.framework.strategy;

import imd.ufrn.com.br.smart_space_booking.framework.dto.AuditoriaResultadoDTO;
import imd.ufrn.com.br.smart_space_booking.framework.enums.TipoImagem;
import imd.ufrn.com.br.smart_space_booking.framework.model.RegraAvaliacao;
import imd.ufrn.com.br.smart_space_booking.framework.model.Recurso;
import imd.ufrn.com.br.smart_space_booking.framework.model.Reserva;

import java.util.List;

public interface AuditoriaStrategy {

    /** O resolver usa isso para escolher a estratégia certa pelo tipo do recurso. */
    boolean suporta(Recurso recurso);

    /** Tipos (e, por tabela, a quantidade) de imagens que o usuário deve enviar. Sala = [GERAL]. */
    List<TipoImagem> imagensEsperadas();

    /** URLs das imagens de referência cadastradas no recurso. Sala = getImagens(). */
    List<String> imagensReferencia(Reserva reserva);

    String promptCheckIn();

    String promptCheckOut(List<RegraAvaliacao> regras);

    /** Validações de domínio sobre o resultado do Gemini. Só decide o que é válido — NÃO mexe em imagens. */
    void validarResultado(AuditoriaResultadoDTO resultado);
}