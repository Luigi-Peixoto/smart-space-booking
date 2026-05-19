package imd.ufrn.com.br.smart_space_booking.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import imd.ufrn.com.br.smart_space_booking.dto.HorarioOcupadoDTO;
import imd.ufrn.com.br.smart_space_booking.dto.ReservaRequestDTO;
import imd.ufrn.com.br.smart_space_booking.dto.ReservaResponseDTO;
import imd.ufrn.com.br.smart_space_booking.enums.ReservaStatus;
import imd.ufrn.com.br.smart_space_booking.enums.ReservaTipo;
import imd.ufrn.com.br.smart_space_booking.exception.AcessoNegadoException;
import imd.ufrn.com.br.smart_space_booking.exception.ConflitoHorarioException;
import imd.ufrn.com.br.smart_space_booking.exception.RegraNegocioException;
import imd.ufrn.com.br.smart_space_booking.exception.ReservaNotFoundException;
import imd.ufrn.com.br.smart_space_booking.exception.SalaNotFoundException;
import imd.ufrn.com.br.smart_space_booking.exception.UsuarioNotFoundException;
import imd.ufrn.com.br.smart_space_booking.model.RegraAvaliacao;
import imd.ufrn.com.br.smart_space_booking.model.Reserva;
import imd.ufrn.com.br.smart_space_booking.model.Sala;
import imd.ufrn.com.br.smart_space_booking.model.Usuario;
import imd.ufrn.com.br.smart_space_booking.repository.RegraAvaliacaoRepository;
import imd.ufrn.com.br.smart_space_booking.repository.ReservaRepository;
import imd.ufrn.com.br.smart_space_booking.repository.SalaRepository;
import imd.ufrn.com.br.smart_space_booking.repository.UsuarioRepository;
import jakarta.transaction.Transactional;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final SalaRepository salaRepository;
    private final RegraAvaliacaoRepository regraAvaliacaoRepository;
    private final TrustScoreService trustScoreService;

    public ReservaService(ReservaRepository reservaRepository,
                          UsuarioRepository usuarioRepository,
                          SalaRepository salaRepository,
                          RegraAvaliacaoRepository regraAvaliacaoRepository,
                          TrustScoreService trustScoreService) {
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.salaRepository = salaRepository;
        this.regraAvaliacaoRepository = regraAvaliacaoRepository;
        this.trustScoreService = trustScoreService;
    }

    @Transactional
    public ReservaResponseDTO create(ReservaRequestDTO dto) {
        if (dto.fimDateTime().isBefore(dto.inicioDateTime())) {
            throw new RegraNegocioException("A data de fim não pode ser anterior à data de início.");
        }

        ZonedDateTime fimComBuffer = dto.fimDateTime().plusMinutes(15);

        boolean existeConflito = reservaRepository.existeConflito(
                dto.salaId(), dto.inicioDateTime(), fimComBuffer);

        if (existeConflito) {
            throw new ConflitoHorarioException("A sala já está ocupada neste horário (considerando limpeza).");
        }

        Sala sala = salaRepository.findById(dto.salaId())
                .orElseThrow(() -> new SalaNotFoundException("Nenhuma sala encontrada com o ID: " + dto.salaId()));
        Usuario usuario = usuarioRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new UsuarioNotFoundException("Nenhum usuário encontrado com o ID: " + dto.usuarioId()));

        Reserva reserva = new Reserva();
        reserva.setInicioDateTime(dto.inicioDateTime());
        reserva.setFimDateTime(dto.fimDateTime());
        reserva.setTipo(dto.tipo());
        reserva.setStatus(ReservaStatus.CONFIRMADA);
        reserva.setUsuario(usuario);
        reserva.setSala(sala);
        reserva.setDataHoraCheckin(null);
        reserva.setDataHoraCheckout(null);
        reserva.setMotivoCancelamento(null);
        reserva.setDataHoraCancelamento(null);

        reservaRepository.save(reserva);

        Reserva manutencao = new Reserva();
        manutencao.setInicioDateTime(dto.fimDateTime());
        manutencao.setFimDateTime(fimComBuffer);
        manutencao.setTipo(ReservaTipo.MANUTENCAO);
        manutencao.setStatus(ReservaStatus.CONFIRMADA);
        manutencao.setSala(sala);
        manutencao.setUsuario(null);

        reservaRepository.save(manutencao);

        return ReservaResponseDTO.fromEntity(reserva);
    }

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

        // Penalidade por cancelamento tardio (menos de 2h de antecedência)
        if (horasDeAntecedencia < 2) {
            RegraAvaliacao regraTardio = regraAvaliacaoRepository
                    .findByNomeIgnoreCase("Cancelamento Tardio")
                    .orElse(null);

            int delta = regraTardio != null ? regraTardio.getDeltaPenalidade() : -10;
            String descricao = "Cancelamento com " + horasDeAntecedencia + "h de antecedência.";

            trustScoreService.registrarAlteracao(usuario, delta, regraTardio, reserva, descricao);
        }

        ZonedDateTime umaSemanaAtras = ZonedDateTime.now().minusDays(7);
        long cancelamentosNaSemana = reservaRepository.countByUsuarioIdAndStatusAndDataHoraCancelamento(
                usuario.getId(), ReservaStatus.CANCELADA, umaSemanaAtras);

        regraAvaliacaoRepository.findByNomeIgnoreCase("Excesso de Cancelamentos").ifPresent(regraExcesso -> {
            if (cancelamentosNaSemana > regraExcesso.getLimiPenalidade()) {
                trustScoreService.registrarAlteracao(
                        usuario,
                        regraExcesso.getDeltaPenalidade(),
                        regraExcesso,
                        reserva,
                        "Excesso de cancelamentos na semana (" + cancelamentosNaSemana + " cancelamentos)."
                );
            }
        });

        reservaRepository.findBySalaIdAndInicioDateTimeAndTipo(
                reserva.getSala().getId(),
                reserva.getFimDateTime(),
                ReservaTipo.MANUTENCAO
        ).ifPresent(manutencao -> {
            manutencao.setStatus(ReservaStatus.CANCELADA);
            reservaRepository.save(manutencao);
        });
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

                // Registra penalidade de no-show se houver usuário
                if (reserva.getUsuario() != null) {
                    RegraAvaliacao regraNoShow = regraAvaliacaoRepository
                            .findByNomeIgnoreCase("No-Show")
                            .orElse(null);

                    int delta = regraNoShow != null ? regraNoShow.getDeltaPenalidade() : -15;

                    trustScoreService.registrarAlteracao(
                            reserva.getUsuario(),
                            delta,
                            regraNoShow,
                            reserva,
                            "Reserva cancelada automaticamente por no-show."
                    );
                }

                reservaRepository.findBySalaIdAndInicioDateTimeAndTipo(
                        reserva.getSala().getId(),
                        reserva.getFimDateTime(),
                        ReservaTipo.MANUTENCAO
                ).ifPresent(manutencao -> {
                    manutencao.setStatus(ReservaStatus.CANCELADA);
                    reservaRepository.save(manutencao);
                });
            }
            reservaRepository.saveAll(reservasExpiradas);
        }
    }

    public List<HorarioOcupadoDTO> findOcupados(Long salaId, LocalDate data) {
        ZonedDateTime inicioDia = data.atStartOfDay(ZoneId.of("America/Fortaleza"));
        ZonedDateTime fimDia = inicioDia.plusDays(1).minusNanos(1);

        return reservaRepository.findReservasPorSalaNoDia(salaId, inicioDia, fimDia)
                .stream()
                .map(HorarioOcupadoDTO::fromEntity)
                .toList();
    }

    public List<ReservaResponseDTO> findAll() {
        return reservaRepository.findAll()
                .stream()
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
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return reservaRepository.findReservasPorUsuario(usuarioId)
                .stream()
                .map(ReservaResponseDTO::fromEntity)
                .toList();
    }
}