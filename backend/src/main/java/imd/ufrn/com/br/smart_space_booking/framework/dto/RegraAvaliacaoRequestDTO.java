package imd.ufrn.com.br.smart_space_booking.framework.dto;

public record RegraAvaliacaoRequestDTO(
        String nome,
        String descricao,
        Integer limiBonus,
        Integer deltaBonus,
        Integer limiPenalidade,
        Integer deltaPenalidade
) {}
