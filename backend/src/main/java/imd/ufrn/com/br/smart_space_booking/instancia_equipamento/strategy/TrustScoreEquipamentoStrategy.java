package imd.ufrn.com.br.smart_space_booking.instancia_equipamento.strategy;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import imd.ufrn.com.br.smart_space_booking.framework.enums.MomentoEvento;
import imd.ufrn.com.br.smart_space_booking.framework.evento.EventoTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.model.Recurso;
import imd.ufrn.com.br.smart_space_booking.framework.model.Reserva;
import imd.ufrn.com.br.smart_space_booking.framework.restricao.LimiteConcorrenciaRestricao;
import imd.ufrn.com.br.smart_space_booking.framework.restricao.RestricaoTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.strategy.TrustScoreStrategy;
import imd.ufrn.com.br.smart_space_booking.instancia_equipamento.model.Equipamento;

/**
 * Regra de negócio própria do Equipamento: nem todo equipamento tem a mesma
 * criticidade (câmera e projetor são mais disputados que um item genérico),
 * e quanto mais subitens o kit tem, maior o risco/prejuízo de um no-show ou
 * cancelamento de última hora (mais peças fora de circulação de uma vez só).
 * A penalidade escala pela combinação tipo x tamanho do kit.
 */
@Component
public class TrustScoreEquipamentoStrategy implements TrustScoreStrategy {

    private static final double INCREMENTO_POR_SUBITEM = 0.1;

    @Override
    public boolean suporta(Recurso recurso) {
        return recurso instanceof Equipamento;
    }

    @Override
    public String tipoRecurso() {
        return "EQUIPAMENTO";
    }

    @Override
    public double fatorSeveridade(Reserva reserva) {
        Equipamento equipamento = (Equipamento) reserva.getRecurso();

        double fatorCriticidade = switch (equipamento.getTipo()) {
            case CAMERA, PROJETOR -> 1.5; // alta demanda / baixa disponibilidade
            case NOTEBOOK, MICROFONE -> 1.2;
            case OUTRO -> 1.0;
        };

        int tamanhoKit = equipamento.getSubItens() != null ? equipamento.getSubItens().size() : 0;
        double fatorKit = 1.0 + (tamanhoKit * INCREMENTO_POR_SUBITEM);

        return fatorCriticidade * fatorKit;
    }

    /** Equipamentos são mais escassos que salas; tolera menos cancelamentos na semana antes de penalizar. */
    @Override
    public long limiteCancelamentosPadrao() {
        return 2L;
    }

    @Override
    public List<RestricaoTrustScore> restricoes() {
        return List.of(
                new LimiteConcorrenciaRestricao(tipoRecurso(), 2, 60),
                new LimiteTamanhoKitRestricao(tipoRecurso() + ":TAMANHO_KIT", 3, 50),
                new RestricaoFimDeSemana(tipoRecurso() + ":FIM_DE_SEMANA", 45)
        );
    }

    @Override
    public List<EventoTrustScore> eventos() {
        List<EventoTrustScore> eventos = new ArrayList<>(TrustScoreStrategy.super.eventos());
        eventos.add(new DevolucaoAtrasadaEvento(tipoRecurso() + ":DEVOLUCAO_ATRASADA", -10, MomentoEvento.CHECKOUT));
        eventos.add(new CancelamentoReservaCurtaEvento(tipoRecurso() + ":CANCELAMENTO_RESERVA_CURTA", -5, MomentoEvento.CANCELAMENTO));
        return eventos;
    }
}
