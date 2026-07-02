package imd.ufrn.com.br.smart_space_booking.instancia_equipamento.model;
import imd.ufrn.com.br.smart_space_booking.framework.model.Recurso;
import imd.ufrn.com.br.smart_space_booking.framework.enums.StatusRecurso;
import imd.ufrn.com.br.smart_space_booking.instancia_equipamento.enums.TipoEquipamento;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "equipamento")
public class Equipamento extends Recurso {

    @Column(name = "numero_serie", nullable = false, unique = true)
    private String numeroSerie;

    @Column(nullable = false)
    private String marca;

    @Column(nullable = false)
    private String modelo;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoEquipamento tipo;

    // Composição de kit — subitens pertencem ao equipamento
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "equipamento_id")
    private List<SubItem> subItens = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "equipamento_imagens", joinColumns = @JoinColumn(name = "equipamento_id"))
    @Column(name = "imagem")
    @NotEmpty(message = "O equipamento deve ter pelo menos uma imagem.")
    private List<String> imagens = new ArrayList<>();

    public Equipamento() {}

    public Equipamento(String nome, StatusRecurso status, String numeroSerie,
                       String marca, String modelo, TipoEquipamento tipo,
                       List<SubItem> subItens, List<String> imagens) {
        super(nome, status);
        this.numeroSerie = numeroSerie;
        this.marca = marca;
        this.modelo = modelo;
        this.tipo = tipo;
        this.subItens = subItens;
        this.imagens = imagens;
    }

    public String getNumeroSerie() { return numeroSerie; }
    public void setNumeroSerie(String numeroSerie) { this.numeroSerie = numeroSerie; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public TipoEquipamento getTipo() { return tipo; }
    public void setTipo(TipoEquipamento tipo) { this.tipo = tipo; }

    public List<SubItem> getSubItens() { return subItens; }
    public void setSubItens(List<SubItem> subItens) { this.subItens = subItens; }

    public List<String> getImagens() { return imagens; }
    public void setImagens(List<String> imagens) { this.imagens = imagens; }
}