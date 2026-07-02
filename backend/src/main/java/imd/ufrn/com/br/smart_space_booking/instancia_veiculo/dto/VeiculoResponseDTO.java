package imd.ufrn.com.br.smart_space_booking.instancia_veiculo.dto;

import imd.ufrn.com.br.smart_space_booking.instancia_veiculo.model.Veiculo;

import java.util.List;

public record VeiculoResponseDTO(
        Long id,
        String nome,
        String placa,
        String chassi,
        String renavam,
        String modelo,
        String marca,
        String cor,
        String status,
        List<String> imagens
) {
    public static VeiculoResponseDTO fromEntity(Veiculo veiculo) {
        return new VeiculoResponseDTO(
                veiculo.getId(),
                veiculo.getNome(),
                veiculo.getPlaca(),
                veiculo.getChassi(),
                veiculo.getRenavam(),
                veiculo.getModelo(),
                veiculo.getMarca(),
                veiculo.getCor(),
                veiculo.getStatus().toString(),
                veiculo.getImagens());
    }
}