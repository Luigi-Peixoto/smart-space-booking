package imd.ufrn.com.br.smart_space_booking.model;

import java.time.LocalDateTime;

import imd.ufrn.com.br.smart_space_booking.enums.IncidenteStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "incidente")
public class Incidente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "recurso_id", nullable = false)
    private Recurso recurso; 

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidenteStatus status = IncidenteStatus.ABERTO;

    @Column(name = "data_reporte", nullable = false, updatable = false)
    private LocalDateTime dataReporte;

    @PrePersist
    protected void onCreate() {
        this.dataReporte = LocalDateTime.now();
    }

    // Getters e Setters
}