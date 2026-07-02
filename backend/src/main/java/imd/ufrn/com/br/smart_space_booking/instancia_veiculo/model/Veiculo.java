package imd.ufrn.com.br.smart_space_booking.instancia_veiculo.model;

import imd.ufrn.com.br.smart_space_booking.framework.enums.StatusRecurso;
import imd.ufrn.com.br.smart_space_booking.framework.model.Recurso;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "veiculo")
public class Veiculo extends Recurso {

    @Column(nullable = false, unique = true, length = 10)
    private String placa;

    @Column(nullable = false, unique = true, length = 17)
    private String chassi;

    @Column(nullable = false, unique = true, length = 11)
    private String renavam;

    @Column(nullable = false)
    private String modelo;

    @Column(nullable = false)
    private String marca;

    @Column(nullable = false)
    private String cor;

    @ElementCollection
    @CollectionTable(name = "veiculo_imagens", joinColumns = @JoinColumn(name = "veiculo_id"))
    @Column(name = "imagem")
    @NotEmpty(message = "O veículo deve ter pelo menos uma imagem.")
    private List<String> imagens = new ArrayList<>();

    public Veiculo() {}

    public Veiculo(String nome, StatusRecurso status, String placa, String chassi,
                   String renavam, String modelo, String marca, String cor,
                   List<String> imagens) {
        super(nome, status);
        this.placa = placa;
        this.chassi = chassi;
        this.renavam = renavam;
        this.modelo = modelo;
        this.marca = marca;
        this.cor = cor;
        this.imagens = imagens;
    }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public String getChassi() { return chassi; }
    public void setChassi(String chassi) { this.chassi = chassi; }

    public String getRenavam() { return renavam; }
    public void setRenavam(String renavam) { this.renavam = renavam; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getCor() { return cor; }
    public void setCor(String cor) { this.cor = cor; }

    public List<String> getImagens() { return imagens; }
    public void setImagens(List<String> imagens) { this.imagens = imagens; }
}