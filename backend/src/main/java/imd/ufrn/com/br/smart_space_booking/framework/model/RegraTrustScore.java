package imd.ufrn.com.br.smart_space_booking.framework.model;

import imd.ufrn.com.br.smart_space_booking.framework.enums.CategoriaRegraTrustScore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Regra de TrustScore configurada pelo admin — cada evento, exigência ou restrição
 * do framework ou hotspot tem uma chave estável, e o admin cadastra um valor
 * principal e secundário pra cada chave. O framework repassa a regra configurada
 * pra cada evento/exigência/restrição, e cada implementação decide como usar os
 * valores. O valor principal é obrigatório; o secundário é opcional e depende da
 * categoria da regra.
 */
@Getter
@Setter
@Entity
@Table(name = "regra_trust_score",
        uniqueConstraints = @UniqueConstraint(columnNames = {"categoria", "chave"}))
public class RegraTrustScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaRegraTrustScore categoria;

    @Column(nullable = false, length = 60)
    private String chave;

    @Column(name = "valor_principal", nullable = false)
    private Integer valorPrincipal;

    @Column(name = "valor_secundario")
    private Integer valorSecundario;

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
