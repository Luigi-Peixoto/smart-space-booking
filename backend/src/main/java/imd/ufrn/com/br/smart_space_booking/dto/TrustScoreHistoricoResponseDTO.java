package imd.ufrn.com.br.smart_space_booking.dto;

import imd.ufrn.com.br.smart_space_booking.model.TrustScoreHistorico;

import java.time.ZonedDateTime;

public record TrustScoreHistoricoResponseDTO(
        Long id,
        Long usuarioId,
        Long reservaId,
        Long regraId,
        String regraNome,
        Long regraEventoId,
        String regraEvento,
        Integer delta,
        Integer scoreAnterior,
        Integer scorePosterior,
        String descricao,
        ZonedDateTime criadoEm
) {
    public static TrustScoreHistoricoResponseDTO fromEntity(TrustScoreHistorico h) {
        return new TrustScoreHistoricoResponseDTO(
                h.getId(),
                h.getUsuario().getId(),
                h.getReserva() != null ? h.getReserva().getId() : null,
                h.getRegra() != null ? h.getRegra().getId() : null,
                h.getRegra() != null ? h.getRegra().getNome() : null,
                h.getRegraEvento() != null ? h.getRegraEvento().getId() : null,
                h.getRegraEvento() != null ? h.getRegraEvento().getEvento().name() : null,
                h.getDelta(),
                h.getScoreAnterior(),
                h.getScorePosterior(),
                h.getDescricao(),
                h.getCriadoEm()
        );
    }
}