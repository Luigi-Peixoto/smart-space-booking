// strategy/auditoria/AuditoriaSalaStrategy.java
package imd.ufrn.com.br.smart_space_booking.strategy;

import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

import imd.ufrn.com.br.smart_space_booking.framework.dto.AuditoriaResultadoDTO;
import imd.ufrn.com.br.smart_space_booking.framework.enums.TipoImagem;
import imd.ufrn.com.br.smart_space_booking.framework.exception.ImagemInvalidaException;
import imd.ufrn.com.br.smart_space_booking.exception.SalaIncorretaException;
import imd.ufrn.com.br.smart_space_booking.framework.model.Recurso;
import imd.ufrn.com.br.smart_space_booking.framework.model.RegraAvaliacao;
import imd.ufrn.com.br.smart_space_booking.framework.model.Reserva;
import imd.ufrn.com.br.smart_space_booking.framework.strategy.AuditoriaStrategy;
import imd.ufrn.com.br.smart_space_booking.model.Sala;
import imd.ufrn.com.br.smart_space_booking.prompts.SalaPromptTemplate;

@Component
public class AuditoriaSalaStrategy implements AuditoriaStrategy {

    private final SalaPromptTemplate prompts;

    public AuditoriaSalaStrategy(SalaPromptTemplate prompts) {
        this.prompts = prompts;
    }

    @Override
    public boolean suporta(Recurso recurso) {
        return Hibernate.unproxy(recurso) instanceof Sala;
    }

    @Override
    public List<TipoImagem> imagensEsperadas() {
        return List.of(TipoImagem.GERAL); // sala: 1 imagem
    }

    @Override
    public List<String> imagensReferencia(Reserva reserva) {
        Sala sala = (Sala) Hibernate.unproxy(reserva.getRecurso());
        return sala.getImagens();
    }
    @Override
    public String promptCheckIn() {
        return prompts.promptCheckIn();
    }

    @Override
    public String promptCheckOut(List<RegraAvaliacao> regras) {
        return prompts.promptCheckOut(regras);
    }

    @Override
    public void validarResultado(AuditoriaResultadoDTO resultado) {
        if (!resultado.isImagemValida()) {
            throw new ImagemInvalidaException(
                    "As imagens enviadas não correspondem a um ambiente interno. " +
                            "Por favor, fotografe a sala corretamente.");
        }
        if (!resultado.isRecursoCorreto()) {            // antes: isSalaCorreta()
            throw new SalaIncorretaException(
                    "A sala fotografada não corresponde à sala reservada. " +
                            "Por favor, fotografe a sala correta.");
        }
    }
}