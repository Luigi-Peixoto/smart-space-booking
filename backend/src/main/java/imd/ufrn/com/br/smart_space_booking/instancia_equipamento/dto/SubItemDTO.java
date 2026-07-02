package imd.ufrn.com.br.smart_space_booking.instancia_equipamento.dto;

import imd.ufrn.com.br.smart_space_booking.instancia_equipamento.model.SubItem;

import java.util.List;

public record SubItemDTO(
        Long id,
        String nome,
        String descricao,
        List<String> imagens
) {
    public static SubItemDTO fromEntity(SubItem subItem) {
        return new SubItemDTO(
                subItem.getId(),
                subItem.getNome(),
                subItem.getDescricao(),
                subItem.getImagens());
    }
}