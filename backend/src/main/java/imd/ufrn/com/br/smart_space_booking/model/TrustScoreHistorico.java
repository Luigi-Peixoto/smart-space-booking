package imd.ufrn.com.br.smart_space_booking.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

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
     * Reserva relacionada ao evento, se houver.
     * Nullable — nem todo evento está vinculado a uma reserva.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id")
    private Reserva reserva;

    /**
     * Regra que originou a alteração.
     * Nullable — permite registros de ajustes manuais sem regra vinculada.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regra_id")
    private RegraAvaliacao regra;

    /**
     * Variação aplicada — positiva (bonificação) ou negativa (penalidade).
     */
    @Column(name = "delta", nullable = false)
    private Integer delta;

    /**
     * Valor do TrustScore antes da alteração.
     */
    @Column(name = "score_anterior", nullable = false)
    private Integer scoreAnterior;

    /**
     * Valor do TrustScore após a alteração.
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
