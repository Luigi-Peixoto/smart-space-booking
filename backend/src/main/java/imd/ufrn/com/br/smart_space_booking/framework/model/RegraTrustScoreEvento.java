package imd.ufrn.com.br.smart_space_booking.framework.model;

import imd.ufrn.com.br.smart_space_booking.framework.enums.TrustScoreEvento;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Severidade configurável de um evento estrutural do ciclo de vida da reserva
 * (cancelamento tardio, no-show, excesso de cancelamentos). Diferente de
 * RegraAvaliacao — que é um conjunto aberto de critérios de nota (0-10) criados
 * livremente pelo admin para a avaliação por IA — aqui o conjunto de eventos é
 * fechado e definido pelo framework (TrustScoreEvento); o admin só ajusta o
 * quanto penaliza (delta) e, quando aplicável, um parâmetro numérico do evento
 * (janela em horas, limite semanal etc).
 */
@Getter
@Setter
@Entity
@Table(name = "regra_trust_score_evento")
public class RegraTrustScoreEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private TrustScoreEvento evento;

    @Column(nullable = false)
    private Integer delta;

    /**
     * Parâmetro numérico do evento — janela em horas (CANCELAMENTO_TARDIO),
     * limite de cancelamentos na semana (EXCESSO_CANCELAMENTOS). Nullable —
     * NO_SHOW não usa parâmetro; se null, a Strategy usa seu próprio fallback.
     */
    @Column(name = "parametro")
    private Integer parametro;

    @Column(length = 300)
    private String descricao;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
