package imd.ufrn.com.br.smart_space_booking.framework.model;

import java.time.ZonedDateTime;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import imd.ufrn.com.br.smart_space_booking.framework.enums.TrustScoreHistoricoTipo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "trust_score_historico")
public class TrustScoreHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /**
     * PENALIDADE (delta de score) ou BLOQUEIO (reserva barrada, score não muda).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TrustScoreHistoricoTipo tipo = TrustScoreHistoricoTipo.PENALIDADE;

    /**
     * Reserva relacionada, se houver.
     * Nullable — bloqueios não têm Reserva persistida (a tentativa foi barrada
     * antes de criar).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id")
    private Reserva reserva;

    /**
     * Recurso da tentativa de reserva, quando não há Reserva persistida (BLOQUEIO).
     * Nullable — penalidades já têm o recurso via {@link #reserva}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurso_id")
    private Recurso recurso;

    /**
     * RegraAvaliacao (critério de nota do checkout via IA) que originou a alteração.
     * Nullable — populada apenas quando a origem é a avaliação por critério; null em
     * ajustes manuais e em eventos estruturais/restrições (ver regraTrustScore).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regra_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private RegraAvaliacao regra;

    /**
     * RegraTrustScore (qualquer categoria — EVENTO, EXIGENCIA ou RESTRICAO) que
     * originou a alteração ou o bloqueio. Nullable — populada apenas quando a
     * origem é uma dessas; null em ajustes manuais e na avaliação por critério,
     * e também null quando o bloqueio usou fallback do hotspot (sem regra
     * cadastrada pelo admin).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regra_trust_score_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private RegraTrustScore regraTrustScore;

    /**
     * Variação aplicada — positiva (bonificação), negativa (penalidade) ou 0 (bloqueio).
     */
    @Column(name = "delta", nullable = false)
    private Integer delta;

    /**
     * Valor do TrustScore antes da alteração (igual a scorePosterior em bloqueios).
     */
    @Column(name = "score_anterior", nullable = false)
    private Integer scoreAnterior;

    /**
     * Valor do TrustScore após a alteração (igual a scoreAnterior em bloqueios).
     */
    @Column(name = "score_posterior", nullable = false)
    private Integer scorePosterior;

    /**
     * Descrição opcional para contexto adicional.
     */
    @Column(name = "descricao", length = 300)
    private String descricao;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private ZonedDateTime criadoEm;

    @PrePersist
    protected void onCreate() {
        this.criadoEm = ZonedDateTime.now();
    }
}
