package imd.ufrn.com.br.smart_space_booking.instancia_equipamento.strategy;

import imd.ufrn.com.br.smart_space_booking.framework.dto.AuditoriaResultadoDTO;
import imd.ufrn.com.br.smart_space_booking.framework.enums.TipoImagem;
import imd.ufrn.com.br.smart_space_booking.framework.exception.ImagemInvalidaException;
import imd.ufrn.com.br.smart_space_booking.framework.exception.RecursoIncorretoException;
import imd.ufrn.com.br.smart_space_booking.framework.model.RegraAvaliacao;
import imd.ufrn.com.br.smart_space_booking.framework.model.Recurso;
import imd.ufrn.com.br.smart_space_booking.framework.model.Reserva;
import imd.ufrn.com.br.smart_space_booking.framework.strategy.AuditoriaStrategy;
import imd.ufrn.com.br.smart_space_booking.instancia_equipamento.model.Equipamento;
import imd.ufrn.com.br.smart_space_booking.instancia_equipamento.model.SubItem;
import imd.ufrn.com.br.smart_space_booking.instancia_equipamento.prompts.EquipamentoPromptTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Estratégia de auditoria do hotspot Equipamento.
 *
 * Pontos variáveis em relação a Sala/Veículo:
 *  - exige 2 imagens tipadas (geral do aparelho + kit completo);
 *  - a completude do kit é verificada por COMPARAÇÃO: as imagens de referência
 *    reúnem as do equipamento MAIS as de cada subitem, então a IA consegue
 *    detectar acessórios faltando sem receber nenhum dado textual da instância.
 *  - usa o EquipamentoPromptTemplate.
 */
@Component
public class AuditoriaEquipamentoStrategy implements AuditoriaStrategy {

    private final EquipamentoPromptTemplate prompts;

    public AuditoriaEquipamentoStrategy(EquipamentoPromptTemplate prompts) {
        this.prompts = prompts;
    }

    @Override
    public boolean suporta(Recurso recurso) {
        return recurso instanceof Equipamento;
    }

    @Override
    public List<TipoImagem> imagensEsperadas() {
        // A ORDEM importa: deve casar com a ordem declarada no EquipamentoPromptTemplate.
        return List.of(TipoImagem.GERAL, TipoImagem.KIT);
    }

    @Override
    public List<String> imagensReferencia(Reserva reserva) {
        Equipamento equipamento = (Equipamento) reserva.getRecurso();

        // Referências do equipamento principal + de TODOS os subitens do kit.
        List<String> referencias = new ArrayList<>(equipamento.getImagens());
        for (SubItem item : equipamento.getSubItens()) {
            referencias.addAll(item.getImagens());
        }
        return referencias;
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
                "As imagens enviadas não correspondem a um equipamento com kit visível " +
                "(visão geral + kit completo). Por favor, refaça as fotos.");
        }
        if (!resultado.isRecursoCorreto()) {
            throw new RecursoIncorretoException(
                "O equipamento fotografado não corresponde ao reservado, ou o kit está " +
                "incompleto em relação à referência. Verifique o aparelho e os acessórios.");
        }
    }
}
