package imd.ufrn.com.br.smart_space_booking.dto;

import imd.ufrn.com.br.smart_space_booking.model.Equipamento;

import java.util.List;

public record EquipamentoResponseDTO(
        Long id,
        String nome,
        String numeroSerie,
        String marca,
        String modelo,
        String tipo,
        String status,
        List<SubItemDTO> subItens,
        List<String> imagens
) {
    public static EquipamentoResponseDTO fromEntity(Equipamento equipamento) {
        return new EquipamentoResponseDTO(
                equipamento.getId(),
                equipamento.getNome(),
                equipamento.getNumeroSerie(),
                equipamento.getMarca(),
                equipamento.getModelo(),
                equipamento.getTipo().toString(),
                equipamento.getStatus().toString(),
                equipamento.getSubItens().stream().map(SubItemDTO::fromEntity).toList(),
                equipamento.getImagens());
    }
}