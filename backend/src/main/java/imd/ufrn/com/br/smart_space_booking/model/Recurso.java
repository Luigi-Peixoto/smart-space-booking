package imd.ufrn.com.br.smart_space_booking.model;

import imd.ufrn.com.br.smart_space_booking.enums.StatusRecurso;
import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "recurso")
public abstract class Recurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusRecurso status;

    protected Recurso() {}

    protected Recurso(String nome, StatusRecurso status) {
        this.nome = nome;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public StatusRecurso getStatus() { return status; }
    public void setStatus(StatusRecurso status) { this.status = status; }
}