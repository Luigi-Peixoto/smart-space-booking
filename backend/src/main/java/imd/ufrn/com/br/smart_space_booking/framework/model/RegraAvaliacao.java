package imd.ufrn.com.br.smart_space_booking.framework.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "regra_avaliacao")
public class RegraAvaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Column(name = "limi_bonus", nullable = false)
    private Integer limiBonus; // nota minima p o bonus

    @Column(name = "delta_bonus", nullable = false)
    private Integer deltaBonus; //pontos add bonus

    @Column(name = "limi_penalidade", nullable = false)
    private Integer limiPenalidade; //nota minima p n perder pontos

    @Column(name = "delta_penalidade", nullable = false)
    private Integer deltaPenalidade; // pontos deduct perda

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