package imd.ufrn.com.br.smart_space_booking.framework.model;

import imd.ufrn.com.br.smart_space_booking.framework.dto.AvaliacaoCriterioDTO;
import imd.ufrn.com.br.smart_space_booking.framework.enums.AuditoriaTipo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "auditoria")
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reserva reserva;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private AuditoriaTipo tipo;

    @Column(name = "resultado_ia", columnDefinition = "TEXT")
    private String resultadoIa;

    @Column(name = "aprovado")
    private Boolean aprovado;

    @Column(name = "observacao_geral", length = 500)
    private String observacaoGeral;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "criterios_json", columnDefinition = "jsonb")
    private List<AvaliacaoCriterioDTO> criterios = new ArrayList<>();

    @Column(name = "delta_trust_score_aplicado")
    private Integer deltaTrustScoreAplicado;

    @ElementCollection
    @CollectionTable(name = "audit_image_ids", joinColumns = @JoinColumn(name = "audit_id"))
    @Column(name = "image_id", nullable = false)
    private List<String> imageIds = new ArrayList<>();

    @Column(name = "date_created", nullable = false, updatable = false)
    private LocalDateTime dateCreated;

    @Column(name = "date_updated")
    private LocalDateTime dateUpdated;

    @PrePersist
    protected void onCreate() {
        this.dateCreated = LocalDateTime.now();
        this.dateUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.dateUpdated = LocalDateTime.now();
    }
}