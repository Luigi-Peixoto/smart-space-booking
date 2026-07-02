package imd.ufrn.com.br.smart_space_booking.framework.model;

import imd.ufrn.com.br.smart_space_booking.framework.enums.UsuarioStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String nome;

    @Column(name = "trust_score")
    private Integer trustScore = 100;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UsuarioStatus status = UsuarioStatus.ATIVO;

    private String perfil = "USER";

    public Usuario() {}

    public Usuario(String email, String nome) {
        this.email = email;
        this.nome = nome;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Integer getTrustScore() { return trustScore; }
    public void setTrustScore(Integer trustScore) { this.trustScore = trustScore; }

    public UsuarioStatus getStatus() { return status; }
    public void setStatus(UsuarioStatus status) { this.status = status; }

    public String getPerfil() { return perfil; }
    public void setPerfil(String perfil) { this.perfil = perfil; }
}