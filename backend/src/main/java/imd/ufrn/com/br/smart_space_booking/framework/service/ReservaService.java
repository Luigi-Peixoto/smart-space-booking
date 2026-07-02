package imd.ufrn.com.br.smart_space_booking.framework.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;

import imd.ufrn.com.br.smart_space_booking.framework.dto.HorarioOcupadoDTO;
import imd.ufrn.com.br.smart_space_booking.framework.dto.ReservaRequestDTO;
import imd.ufrn.com.br.smart_space_booking.framework.dto.ReservaResponseDTO;
import imd.ufrn.com.br.smart_space_booking.framework.enums.ReservaStatus;
import imd.ufrn.com.br.smart_space_booking.framework.enums.ReservaTipo;
import imd.ufrn.com.br.smart_space_booking.framework.enums.TrustScoreEvento;
import imd.ufrn.com.br.smart_space_booking.framework.exception.AcessoNegadoException;
import imd.ufrn.com.br.smart_space_booking.framework.exception.ConflitoHorarioException;
import imd.ufrn.com.br.smart_space_booking.framework.exception.RecursoNotFoundException;
import imd.ufrn.com.br.smart_space_booking.framework.exception.RegraNegocioException;
import imd.ufrn.com.br.smart_space_booking.framework.exception.ReservaNotFoundException;
import imd.ufrn.com.br.smart_space_booking.framework.exception.UsuarioNotFoundException;
import imd.ufrn.com.br.smart_space_booking.framework.model.Recurso;
import imd.ufrn.com.br.smart_space_booking.framework.model.RegraTrustScoreEvento;
import imd.ufrn.com.br.smart_space_booking.framework.model.Reserva;
import imd.ufrn.com.br.smart_space_booking.framework.model.Usuario;
import imd.ufrn.com.br.smart_space_booking.framework.repository.RecursoRepository;
import imd.ufrn.com.br.smart_space_booking.framework.repository.RegraTrustScoreEventoRepository;
import imd.ufrn.com.br.smart_space_booking.framework.repository.ReservaRepository;
import imd.ufrn.com.br.smart_space_booking.framework.repository.UsuarioRepository;
import imd.ufrn.com.br.smart_space_booking.framework.strategy.TrustScoreContexto;
import imd.ufrn.com.br.smart_space_booking.framework.strategy.TrustScoreDecisao;
import imd.ufrn.com.br.smart_space_booking.framework.strategy.TrustScoreStrategy;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

/**
 * Ciclo de vida de reserva — um único bean, igual pra qualquer hotspot.
 * Não há nada que varie por tipo de recurso a ponto de justificar subclasses:
 * a única diferença observada (buffer pós-uso, ex: limpeza de sala) é só um
 * número, então vira parâmetro de {@link #create(ReservaRequestDTO, long)}
 * em vez de exigir uma subclasse por hotspot.
 */
@Service
public class ReservaService {

    protected final ReservaRepository reservaRepository;
    protected final UsuarioRepository usuarioRepository;
    protected final RecursoRepository recursoRepository;
    protected final RegraTrustScoreEventoRepository regraTrustScoreEventoRepository;
    protected final TrustScoreService trustScoreService;
    private final List<TrustScoreStrategy> trustScoreStrategies;

    public ReservaService(ReservaRepository reservaRepository,
                          UsuarioRepository usuarioRepository,
                          RecursoRepository recursoRepository,
                          RegraTrustScoreEventoRepository regraTrustScoreEventoRepository,
                          TrustScoreService trustScoreService,
                          List<TrustScoreStrategy> trustScoreStrategies) {
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.recursoRepository = recursoRepository;
        this.regraTrustScoreEventoRepository = regraTrustScoreEventoRepository;
        this.trustScoreService = trustScoreService;
        this.trustScoreStrategies = trustScoreStrategies;
    }

    // ─── Criação — genérica pra qualquer Recurso ──────────────────────────────

    /** Cria uma reserva sem buffer pós-uso. */
    @Transactional
    public ReservaResponseDTO create(ReservaRequestDTO dto) {
        return create(dto, 0);
    }

    /**
     * Cria uma reserva reservando {@code minutosBuffer} minutos extras após o uso
     * (ex: 15min de limpeza pra sala). 0 = sem buffer.
     */
    @Transactional
    public ReservaResponseDTO create(ReservaRequestDTO dto, long minutosBuffer) {
        if (dto.fimDateTime().isBefore(dto.inicioDateTime()))
            throw new RegraNegocioException("A data de fim não pode ser anterior à data de início.");

        ZonedDateTime fimComBuffer = dto.fimDateTime().plusMinutes(minutosBuffer);

        boolean existeConflito = reservaRepository.existeConflito(
                dto.recursoId(), dto.inicioDateTime(), fimComBuffer);

        if (existeConflito)
            throw new ConflitoHorarioException(minutosBuffer > 0
                    ? "O recurso já está ocupado neste horário (considerando o intervalo pós-uso)."
                    : "O recurso já está ocupado neste horário.");

        Recurso recurso = recursoRepository.findById(dto.recursoId())
                .orElseThrow(() -> new RecursoNotFoundException("Nenhum recurso encontrado com o ID: " + dto.recursoId()));
        Usuario usuario = usuarioRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new UsuarioNotFoundException("Nenhum usuário encontrado com o ID: " + dto.usuarioId()));

        Reserva reserva = new Reserva();
        reserva.setInicioDateTime(dto.inicioDateTime());
        reserva.setFimDateTime(dto.fimDateTime());
        reserva.setTipo(dto.tipo());
        reserva.setStatus(ReservaStatus.CONFIRMADA);
        reserva.setUsuario(usuario);
        reserva.setRecurso(recurso);
        reservaRepository.save(reserva);

        if (minutosBuffer > 0) {
            Reserva bufferReserva = new Reserva();
            bufferReserva.setInicioDateTime(dto.fimDateTime());
            bufferReserva.setFimDateTime(fimComBuffer);
            bufferReserva.setTipo(ReservaTipo.MANUTENCAO);
            bufferReserva.setStatus(ReservaStatus.CONFIRMADA);
            bufferReserva.setRecurso(recurso);
            reservaRepository.save(bufferReserva);
        }

        return ReservaResponseDTO.fromEntity(reserva);
    }

    public List<HorarioOcupadoDTO> findOcupados(Long recursoId, LocalDate data) {
        ZonedDateTime inicioDia = data.atStartOfDay(ZoneId.of("America/Fortaleza"));
        ZonedDateTime fimDia = inicioDia.plusDays(1).minusNanos(1);

        return reservaRepository.findReservasPorRecursoNoDia(recursoId, inicioDia, fimDia)
                .stream()
                .map(HorarioOcupadoDTO::fromEntity)
                .toList();
    }

    // ─── TrustScore — resolução da strategy e aplicação da decisão ───────────

    /** Escolhe a estratégia de TrustScore pelo tipo do recurso da reserva. */
    private TrustScoreStrategy resolverTrustScoreStrategy(Reserva reserva) {
        return trustScoreStrategies.stream()
                .filter(s -> s.suporta(reserva.getRecurso()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Nenhuma TrustScoreStrategy para o recurso: "
                                + reserva.getRecurso().getClass().getSimpleName()));
    }

    private RegraTrustScoreEvento buscarRegra(TrustScoreEvento evento) {
        return regraTrustScoreEventoRepository.findByEvento(evento).orElse(null);
    }

    /** Pede a decisão à strategy e, se aplicável, persiste via TrustScoreService. */
    private void aplicarDecisao(TrustScoreStrategy strategy, TrustScoreEvento evento,
                                TrustScoreContexto contexto, Usuario usuario, Reserva reserva) {
        TrustScoreDecisao decisao = strategy.avaliar(evento, contexto);
        if (decisao.aplicavel()) {
            trustScoreService.registrarAlteracaoPorEvento(usuario, decisao.delta(), contexto.regra(), reserva, decisao.descricao());
        }
    }

    /** Cancela a reserva e o eventual buffer vinculado, por no-show — e aplica a penalidade de TrustScore. */
    private void cancelarPorNoShow(Reserva reserva, String motivo) {
        reserva.setStatus(ReservaStatus.CANCELADA);
        reserva.setMotivoCancelamento(motivo);
        reserva.setDataHoraCancelamento(ZonedDateTime.now());

        if (reserva.getUsuario() != null) {
            TrustScoreStrategy strategy = resolverTrustScoreStrategy(reserva);
            RegraTrustScoreEvento regraNoShow = buscarRegra(TrustScoreEvento.NO_SHOW);
            aplicarDecisao(strategy, TrustScoreEvento.NO_SHOW,
                    TrustScoreContexto.paraNoShow(regraNoShow), reserva.getUsuario(), reserva);
        }

        cancelarBufferSeExistir(reserva);
    }

    /**
     * Cancela a reserva de buffer (manutenção) vinculada, se existir uma — não custa
     * nada perguntar pra hotspots que nunca criam buffer, e evita duplicar em código
     * a informação de "este hotspot usa buffer ou não" (que já está implícita em
     * existir ou não uma reserva MANUTENCAO vinculada).
     */
    private void cancelarBufferSeExistir(Reserva reserva) {
        reservaRepository.findByRecursoIdAndInicioDateTimeAndTipo(
                reserva.getRecurso().getId(),
                reserva.getFimDateTime(),
                ReservaTipo.MANUTENCAO
        ).ifPresent(buffer -> {
            buffer.setStatus(ReservaStatus.CANCELADA);
            reservaRepository.save(buffer);
        });
    }

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
            cancelarPorNoShow(reserva, "NO_SHOW");
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
        TrustScoreStrategy strategy = resolverTrustScoreStrategy(reserva);

        long horasDeAntecedencia = ChronoUnit.HOURS.between(ZonedDateTime.now(), reserva.getInicioDateTime());
        RegraTrustScoreEvento regraTardio = buscarRegra(TrustScoreEvento.CANCELAMENTO_TARDIO);
        aplicarDecisao(strategy, TrustScoreEvento.CANCELAMENTO_TARDIO,
                TrustScoreContexto.paraCancelamentoTardio(regraTardio, horasDeAntecedencia), usuario, reserva);

        ZonedDateTime umaSemanaAtras = ZonedDateTime.now().minusDays(7);
        long cancelamentosNaSemana = reservaRepository.countByUsuarioIdAndStatusAndDataHoraCancelamento(
                usuario.getId(), ReservaStatus.CANCELADA, umaSemanaAtras);
        RegraTrustScoreEvento regraExcesso = buscarRegra(TrustScoreEvento.EXCESSO_CANCELAMENTOS);
        aplicarDecisao(strategy, TrustScoreEvento.EXCESSO_CANCELAMENTOS,
                TrustScoreContexto.paraExcessoCancelamentos(regraExcesso, cancelamentosNaSemana), usuario, reserva);

        cancelarBufferSeExistir(reserva);
    }

    @Scheduled(fixedRate = 30000)
    @Transactional
    public void cancelarReservasExpiradasPorNoShow() {
        ZonedDateTime agora = ZonedDateTime.now();
        ZonedDateTime limiteTolerancia = agora.minusMinutes(10);

        List<Reserva> reservasExpiradas = reservaRepository.findReservasPendentesExpiradas(limiteTolerancia);

        if (!reservasExpiradas.isEmpty()) {
            for (Reserva reserva : reservasExpiradas) {
                cancelarPorNoShow(reserva, "NO_SHOW_AUTOMATICO");
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
