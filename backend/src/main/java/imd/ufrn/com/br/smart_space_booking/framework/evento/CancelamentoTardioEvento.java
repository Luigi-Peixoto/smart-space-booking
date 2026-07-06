package imd.ufrn.com.br.smart_space_booking.framework.evento;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.ToDoubleFunction;

import imd.ufrn.com.br.smart_space_booking.framework.enums.MomentoEvento;
import imd.ufrn.com.br.smart_space_booking.framework.model.RegraTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.model.Reserva;

/**
 * Penaliza cancelar a reserva com menos de {@code janelaPadrao} horas de
 * antecedência — pronto pro framework, cada hotspot instancia com sua
 * própria janela/delta padrão e função de severidade (ex: Veículo escala
 * pela duração da viagem).
 */
public class CancelamentoTardioEvento implements EventoTrustScore {

    private final String chave;
    private final long janelaPadrao;
    private final int deltaPadrao;
    private final ToDoubleFunction<Reserva> fatorSeveridade;
    private final MomentoEvento momento;

    public CancelamentoTardioEvento(String chave, long janelaPadrao, int deltaPadrao,
                                    ToDoubleFunction<Reserva> fatorSeveridade, MomentoEvento momento) {
        this.chave = chave;
        this.janelaPadrao = janelaPadrao;
        this.deltaPadrao = deltaPadrao;
        this.fatorSeveridade = fatorSeveridade;
        this.momento = momento;
    }

    @Override
    public String chave() {
        return chave;
    }

    @Override
    public String descricao() {
        return "Penalidade por cancelar com pouca antecedência (janela em horas e delta configuráveis).";
    }

    @Override
    public MomentoEvento momento() {
        return momento;
    }

    @Override
    public int avaliar(Reserva reserva, List<Reserva> reservasRelacionadas, RegraTrustScore regraConfigurada,
                       StringBuilder descricaoSaida) {
        long horasDeAntecedencia = ChronoUnit.HOURS.between(ZonedDateTime.now(), reserva.getInicioDateTime());
        long janela = regraConfigurada != null && regraConfigurada.getValorSecundario() != null
                ? regraConfigurada.getValorSecundario() : janelaPadrao;
        if (horasDeAntecedencia >= janela) {
            return 0;
        }

        int deltaBase = regraConfigurada != null ? regraConfigurada.getValorPrincipal() : deltaPadrao;
        int delta = EventoTrustScore.escalar(deltaBase, fatorSeveridade.applyAsDouble(reserva));
        descricaoSaida.append("Cancelamento com ").append(horasDeAntecedencia).append("h de antecedência (janela: ")
                .append(janela).append("h)").append(EventoTrustScore.sufixoAjuste(deltaBase, delta)).append(".");
        return delta;
    }

    @Override
    public int deltaPadrao() {
        return deltaPadrao;
    }

    @Override
    public Integer parametroPadrao() {
        return (int) janelaPadrao;
    }
}
