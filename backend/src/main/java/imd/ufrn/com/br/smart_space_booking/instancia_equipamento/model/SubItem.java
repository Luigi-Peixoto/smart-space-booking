package imd.ufrn.com.br.smart_space_booking.instancia_equipamento.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sub_item")
public class SubItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(length = 300)
    private String descricao;

    @ElementCollection
    @CollectionTable(name = "sub_item_imagens", joinColumns = @JoinColumn(name = "sub_item_id"))
    @Column(name = "imagem")
    private List<String> imagens = new ArrayList<>();

    public SubItem() {}

    public SubItem(String nome, String descricao, List<String> imagens) {
        this.nome = nome;
        this.descricao = descricao;
        this.imagens = imagens;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public List<String> getImagens() { return imagens; }
    public void setImagens(List<String> imagens) { this.imagens = imagens; }
}