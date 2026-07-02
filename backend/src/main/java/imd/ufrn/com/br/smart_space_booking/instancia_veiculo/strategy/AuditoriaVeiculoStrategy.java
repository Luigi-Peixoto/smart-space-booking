package imd.ufrn.com.br.smart_space_booking.instancia_veiculo.strategy;

import imd.ufrn.com.br.smart_space_booking.framework.dto.AuditoriaResultadoDTO;
import imd.ufrn.com.br.smart_space_booking.framework.enums.TipoImagem;
import imd.ufrn.com.br.smart_space_booking.framework.exception.ImagemInvalidaException;
import imd.ufrn.com.br.smart_space_booking.framework.exception.RecursoIncorretoException;
import imd.ufrn.com.br.smart_space_booking.framework.model.RegraAvaliacao;
import imd.ufrn.com.br.smart_space_booking.framework.model.Recurso;
import imd.ufrn.com.br.smart_space_booking.framework.model.Reserva;
import imd.ufrn.com.br.smart_space_booking.framework.strategy.AuditoriaStrategy;
import imd.ufrn.com.br.smart_space_booking.instancia_veiculo.model.Veiculo;
import imd.ufrn.com.br.smart_space_booking.instancia_veiculo.prompts.VeiculoPromptTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Estratégia de auditoria do hotspot Veículo.
 *
 * Pontos variáveis em relação a Sala:
 *  - exige 3 imagens tipadas (externa, interna, placa);
 *  - identidade é confirmada principalmente pela leitura da placa;
 *  - usa o VeiculoPromptTemplate.
 */
@Component
public class AuditoriaVeiculoStrategy implements AuditoriaStrategy {

    private final VeiculoPromptTemplate prompts;

    public AuditoriaVeiculoStrategy(VeiculoPromptTemplate prompts) {
        this.prompts = prompts;
    }

    @Override
    public boolean suporta(Recurso recurso) {
        return recurso instanceof Veiculo;
    }

    @Override
    public List<TipoImagem> imagensEsperadas() {
        // A ORDEM importa: deve casar com a ordem declarada no VeiculoPromptTemplate.
        return List.of(TipoImagem.EXTERNO, TipoImagem.INTERNO, TipoImagem.PLACA);
    }

    @Override
    public List<String> imagensReferencia(Reserva reserva) {
        Veiculo veiculo = (Veiculo) reserva.getRecurso();
        return veiculo.getImagens();
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
                "As imagens enviadas não correspondem a um veículo nas 3 vistas exigidas " +
                "(externa, interna e placa). Por favor, refaça as fotos.");
        }
        if (!resultado.isRecursoCorreto()) {
            throw new RecursoIncorretoException(
                "A placa fotografada não corresponde ao veículo reservado. " +
                "Verifique se está fotografando o veículo correto.");
        }
    }
}
