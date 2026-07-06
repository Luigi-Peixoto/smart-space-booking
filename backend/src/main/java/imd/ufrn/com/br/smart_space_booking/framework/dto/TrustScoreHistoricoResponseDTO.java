package imd.ufrn.com.br.smart_space_booking.framework.dto;

import imd.ufrn.com.br.smart_space_booking.framework.enums.TrustScoreHistoricoTipo;
import imd.ufrn.com.br.smart_space_booking.framework.model.TrustScoreHistorico;

import java.time.ZonedDateTime;

public record TrustScoreHistoricoResponseDTO(
        Long id,
        TrustScoreHistoricoTipo tipo,
        Long usuarioId,
        Long reservaId,
        Long recursoId,
        String recursoNome,
        Long regraId,
        String regraNome,
        Long regraTrustScoreId,
        String regraTrustScoreChave,
        Integer delta,
        Integer scoreAnterior,
        Integer scorePosterior,
        String descricao,
        ZonedDateTime criadoEm
) {
    public static TrustScoreHistoricoResponseDTO fromEntity(TrustScoreHistorico h) {
        return new TrustScoreHistoricoResponseDTO(
                h.getId(),
                h.getTipo(),
                h.getUsuario().getId(),
                h.getReserva() != null ? h.getReserva().getId() : null,
                h.getRecurso() != null ? h.getRecurso().getId() : null,
                h.getRecurso() != null ? h.getRecurso().getNome() : null,
                h.getRegra() != null ? h.getRegra().getId() : null,
                h.getRegra() != null ? h.getRegra().getNome() : null,
                h.getRegraTrustScore() != null ? h.getRegraTrustScore().getId() : null,
                h.getRegraTrustScore() != null ? h.getRegraTrustScore().getChave() : null,
                h.getDelta(),
                h.getScoreAnterior(),
                h.getScorePosterior(),
                h.getDescricao(),
                h.getCriadoEm()
        );
    }
}
