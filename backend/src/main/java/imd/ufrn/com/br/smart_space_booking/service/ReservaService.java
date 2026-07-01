package imd.ufrn.com.br.smart_space_booking.service;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;

import imd.ufrn.com.br.smart_space_booking.dto.ReservaRequestDTO;
import imd.ufrn.com.br.smart_space_booking.dto.ReservaResponseDTO;
import imd.ufrn.com.br.smart_space_booking.enums.ReservaStatus;
import imd.ufrn.com.br.smart_space_booking.exception.AcessoNegadoException;
import imd.ufrn.com.br.smart_space_booking.exception.RegraNegocioException;
import imd.ufrn.com.br.smart_space_booking.exception.ReservaNotFoundException;
import imd.ufrn.com.br.smart_space_booking.exception.UsuarioNotFoundException;
import imd.ufrn.com.br.smart_space_booking.model.RegraAvaliacao;
import imd.ufrn.com.br.smart_space_booking.model.Reserva;
import imd.ufrn.com.br.smart_space_booking.model.Usuario;
import imd.ufrn.com.br.smart_space_booking.repository.RegraAvaliacaoRepository;
import imd.ufrn.com.br.smart_space_booking.repository.ReservaRepository;
import imd.ufrn.com.br.smart_space_booking.repository.UsuarioRepository;
import imd.ufrn.com.br.smart_space_booking.strategy.TrustScoreStrategy;
import jakarta.transaction.Transactional;

public abstract class ReservaService {

    protected final ReservaRepository reservaRepository;
    protected final UsuarioRepository usuarioRepository;
    protected final RegraAvaliacaoRepository regraAvaliacaoRepository;
    protected final TrustScoreService trustScoreService;

    protected ReservaService(ReservaRepository reservaRepository,
                             UsuarioRepository usuarioRepository,
                             RegraAvaliacaoRepository regraAvaliacaoRepository,
                             TrustScoreService trustScoreService) {
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.regraAvaliacaoRepository = regraAvaliacaoRepository;
        this.trustScoreService = trustScoreService;
    }
    

    // ─── Métodos abstratos — cada hotspot implementa ──────────────────────────

    public abstract ReservaResponseDTO create(ReservaRequestDTO dto);

    protected abstract TrustScoreStrategy getTrustScoreStrategy();

    protected long getJanelaLimiteCancelamentoEmHoras() {
        return getTrustScoreStrategy().getJanelaCancelamentoEmHoras();
    }

    protected abstract void executarPosCancelamento(Reserva reserva);

    protected abstract void executarPosNoShowAutomatico(Reserva reserva);

    // ─── Ciclo de vida — fixo para qualquer hotspot ───────────────────────────

    @Transactional
    public void validarCheckin(Long reservaId, Long usuarioLogadoId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ReservaNotFoundException("Reserva não encontrada"));

        if (reserva.getUsuario() == null)
            throw new RuntimeException("Erro interno: reserva sem usuário associado.");

        if (!reserva.getUsuario().getId().equals(usuarioLogadoId))
            throw new RegraNegocioException("Acesso negado: reserva de outro usuário.");

        if (reserva.getStatus() == ReservaStatus.CANCELADA)
            throw new RegraNegocioException("Não é possível fazer check-in de reserva cancelada.");

        if (reserva.getDataHoraCheckin() != null && reserva.getStatus() == ReservaStatus.EM_ANDAMENTO)
            throw new RegraNegocioException("Check-in já realizado para esta reserva.");

        ZonedDateTime agora = ZonedDateTime.now();
        ZonedDateTime inicio = reserva.getInicioDateTime();
        ZonedDateTime limite = inicio.plusMinutes(10);

        if (agora.isBefore(inicio))
            throw new RegraNegocioException("O horário da reserva ainda não começou.");

        if (agora.isAfter(limite)) {
            reserva.setStatus(ReservaStatus.CANCELADA);
            reserva.setMotivoCancelamento("NO_SHOW");
            reserva.setDataHoraCancelamento(ZonedDateTime.now());
            reservaRepository.save(reserva);
            throw new RegraNegocioException("Tempo de check-in expirado. Reserva cancelada por No-Show.");
        }
    }

    @Transactional
    public void registrarCheckin(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId).orElseThrow();
        reserva.setDataHoraCheckin(ZonedDateTime.now());
        reserva.setStatus(ReservaStatus.EM_ANDAMENTO);
        reservaRepository.save(reserva);
    }

    public void validarCheckout(Long reservaId, Long usuarioLogadoId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ReservaNotFoundException("Reserva não encontrada"));

        if (!reserva.getUsuario().getId().equals(usuarioLogadoId))
            throw new RegraNegocioException("Acesso negado: reserva de outro usuário.");

        if (reserva.getStatus() != ReservaStatus.EM_ANDAMENTO)
            throw new RegraNegocioException("Check-out só pode ser feito em reservas EM_ANDAMENTO.");

        if (reserva.getDataHoraCheckout() != null)
            throw new RegraNegocioException("Check-out já realizado para esta reserva.");
    }

    @Transactional
    public void registrarCheckout(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId).orElseThrow();
        reserva.setDataHoraCheckout(ZonedDateTime.now());
        reserva.setStatus(ReservaStatus.ENCERRADA);
        reservaRepository.save(reserva);
    }

    @Transactional
    public void cancelarReserva(Long reservaId, Long usuarioLogadoId, String motivo) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ReservaNotFoundException("Reserva não encontrada com o ID: " + reservaId));

        if (!reserva.getUsuario().getId().equals(usuarioLogadoId))
            throw new AcessoNegadoException("Acesso negado: você não pode cancelar a reserva de outro usuário.");

        if (reserva.getStatus() != ReservaStatus.CONFIRMADA)
            throw new RegraNegocioException("Apenas reservas confirmadas podem ser canceladas.");

        reserva.setStatus(ReservaStatus.CANCELADA);
        reserva.setMotivoCancelamento(motivo);
        reserva.setDataHoraCancelamento(ZonedDateTime.now());
        reservaRepository.save(reserva);

        Usuario usuario = reserva.getUsuario();
        long horasDeAntecedencia = ChronoUnit.HOURS.between(ZonedDateTime.now(), reserva.getInicioDateTime());

        if (horasDeAntecedencia < getJanelaLimiteCancelamentoEmHoras()) {
            RegraAvaliacao regraTardio = regraAvaliacaoRepository
                    .findByNomeIgnoreCase("Cancelamento Tardio")
                    .orElse(null);

            int delta = regraTardio != null 
                ? regraTardio.getDeltaPenalidade() 
                : getTrustScoreStrategy().getDeltaPadraoCancelamentoTardio();
            String descricao = "Cancelamento com " + horasDeAntecedencia + "h de antecedência.";
            trustScoreService.registrarAlteracao(usuario, delta, regraTardio, reserva, descricao);
        }

        ZonedDateTime umaSemanaAtras = ZonedDateTime.now().minusDays(7);
        long cancelamentosNaSemana = reservaRepository.countByUsuarioIdAndStatusAndDataHoraCancelamento(
                usuario.getId(), ReservaStatus.CANCELADA, umaSemanaAtras);

        regraAvaliacaoRepository.findByNomeIgnoreCase("Excesso de Cancelamentos").ifPresent(regraExcesso -> {
            if (cancelamentosNaSemana > regraExcesso.getLimiPenalidade()) {
                trustScoreService.registrarAlteracao(usuario, regraExcesso.getDeltaPenalidade(),
                        regraExcesso, reserva,
                        "Excesso de cancelamentos na semana (" + cancelamentosNaSemana + " cancelamentos).");
            }
        });

        executarPosCancelamento(reserva);
    }

    @Scheduled(fixedRate = 30000)
    @Transactional
    public void cancelarReservasExpiradasPorNoShow() {
        ZonedDateTime agora = ZonedDateTime.now();
        ZonedDateTime limiteTolerancia = agora.minusMinutes(10);

        List<Reserva> reservasExpiradas = reservaRepository.findReservasPendentesExpiradas(limiteTolerancia);

        if (!reservasExpiradas.isEmpty()) {
            for (Reserva reserva : reservasExpiradas) {
                reserva.setStatus(ReservaStatus.CANCELADA);
                reserva.setMotivoCancelamento("NO_SHOW_AUTOMATICO");
                reserva.setDataHoraCancelamento(ZonedDateTime.now());

                if (reserva.getUsuario() != null) {
                    RegraAvaliacao regraNoShow = regraAvaliacaoRepository
                            .findByNomeIgnoreCase("No-Show")
                            .orElse(null);

                    int delta = regraNoShow != null 
                        ? regraNoShow.getDeltaPenalidade() 
                        : getTrustScoreStrategy().getDeltaPadraoNoShow();
                    trustScoreService.registrarAlteracao(reserva.getUsuario(), delta, regraNoShow,
                            reserva, "Reserva cancelada automaticamente por no-show.");
                }

                executarPosNoShowAutomatico(reserva);
            }
            reservaRepository.saveAll(reservasExpiradas);
        }
    }

    public List<ReservaResponseDTO> findAll() {
        return reservaRepository.findAll().stream()
                .map(ReservaResponseDTO::fromEntity)
                .toList();
    }

    public ReservaResponseDTO findById(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ReservaNotFoundException("Reserva não encontrada com o ID: " + id));
        return ReservaResponseDTO.fromEntity(reserva);
    }

    @Transactional
    public void delete(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ReservaNotFoundException("Não é possível deletar. Reserva não encontrada com o ID: " + id));
        reservaRepository.delete(reserva);
    }

    public List<ReservaResponseDTO> findByUsuario(Long usuarioId) {
        usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuário não encontrado"));
        return reservaRepository.findReservasPorUsuario(usuarioId).stream()
                .map(ReservaResponseDTO::fromEntity)
                .toList();
    }
}