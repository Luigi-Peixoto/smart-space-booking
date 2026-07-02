package imd.ufrn.com.br.smart_space_booking.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import imd.ufrn.com.br.smart_space_booking.framework.dto.HorarioOcupadoDTO;
import imd.ufrn.com.br.smart_space_booking.framework.dto.ReservaRequestDTO;
import imd.ufrn.com.br.smart_space_booking.framework.dto.ReservaResponseDTO;
import imd.ufrn.com.br.smart_space_booking.framework.enums.ReservaStatus;
import imd.ufrn.com.br.smart_space_booking.framework.enums.ReservaTipo;
import imd.ufrn.com.br.smart_space_booking.framework.exception.ConflitoHorarioException;
import imd.ufrn.com.br.smart_space_booking.framework.exception.RegraNegocioException;
import imd.ufrn.com.br.smart_space_booking.exception.SalaNotFoundException;
import imd.ufrn.com.br.smart_space_booking.framework.exception.UsuarioNotFoundException;
import imd.ufrn.com.br.smart_space_booking.framework.model.Reserva;
import imd.ufrn.com.br.smart_space_booking.model.Sala;
import imd.ufrn.com.br.smart_space_booking.framework.model.Usuario;
import imd.ufrn.com.br.smart_space_booking.framework.repository.RegraTrustScoreEventoRepository;
import imd.ufrn.com.br.smart_space_booking.framework.repository.ReservaRepository;
import imd.ufrn.com.br.smart_space_booking.repository.SalaRepository;
import imd.ufrn.com.br.smart_space_booking.framework.repository.UsuarioRepository;
import imd.ufrn.com.br.smart_space_booking.framework.service.ReservaService;
import imd.ufrn.com.br.smart_space_booking.framework.service.TrustScoreService;
import imd.ufrn.com.br.smart_space_booking.framework.strategy.TrustScoreStrategy;
import jakarta.transaction.Transactional;

@Service
public class ReservaSalaService extends ReservaService {

    private final SalaRepository salaRepository;

    public ReservaSalaService(ReservaRepository reservaRepository,
                              UsuarioRepository usuarioRepository,
                              RegraTrustScoreEventoRepository regraTrustScoreEventoRepository,
                              TrustScoreService trustScoreService,
                              List<TrustScoreStrategy> trustScoreStrategies,
                              SalaRepository salaRepository) {
        super(reservaRepository, usuarioRepository, regraTrustScoreEventoRepository, trustScoreService, trustScoreStrategies);
        this.salaRepository = salaRepository;
    }

    @Override
    @Transactional
    public ReservaResponseDTO create(ReservaRequestDTO dto) {
        if (dto.fimDateTime().isBefore(dto.inicioDateTime()))
            throw new RegraNegocioException("A data de fim não pode ser anterior à data de início.");

        ZonedDateTime fimComBuffer = dto.fimDateTime().plusMinutes(15);

        boolean existeConflito = reservaRepository.existeConflito(
                dto.recursoId(), dto.inicioDateTime(), fimComBuffer);

        if (existeConflito)
            throw new ConflitoHorarioException("A sala já está ocupada neste horário (considerando limpeza).");

        Sala sala = salaRepository.findById(dto.recursoId())
                .orElseThrow(() -> new SalaNotFoundException("Nenhuma sala encontrada com o ID: " + dto.recursoId()));
        Usuario usuario = usuarioRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new UsuarioNotFoundException("Nenhum usuário encontrado com o ID: " + dto.usuarioId()));

        Reserva reserva = new Reserva();
        reserva.setInicioDateTime(dto.inicioDateTime());
        reserva.setFimDateTime(dto.fimDateTime());
        reserva.setTipo(dto.tipo());
        reserva.setStatus(ReservaStatus.CONFIRMADA);
        reserva.setUsuario(usuario);
        reserva.setRecurso(sala);
        reservaRepository.save(reserva);

        Reserva manutencao = new Reserva();
        manutencao.setInicioDateTime(dto.fimDateTime());
        manutencao.setFimDateTime(fimComBuffer);
        manutencao.setTipo(ReservaTipo.MANUTENCAO);
        manutencao.setStatus(ReservaStatus.CONFIRMADA);
        manutencao.setRecurso(sala);
        reservaRepository.save(manutencao);

        return ReservaResponseDTO.fromEntity(reserva);
    }

    @Override
    protected void executarPosCancelamento(Reserva reserva) {
        reservaRepository.findByRecursoIdAndInicioDateTimeAndTipo(
                reserva.getRecurso().getId(),
                reserva.getFimDateTime(),
                ReservaTipo.MANUTENCAO
        ).ifPresent(manutencao -> {
            manutencao.setStatus(ReservaStatus.CANCELADA);
            reservaRepository.save(manutencao);
        });
    }

    @Override
    protected void executarPosNoShowAutomatico(Reserva reserva) {
        reservaRepository.findByRecursoIdAndInicioDateTimeAndTipo(
                reserva.getRecurso().getId(),
                reserva.getFimDateTime(),
                ReservaTipo.MANUTENCAO
        ).ifPresent(manutencao -> {
            manutencao.setStatus(ReservaStatus.CANCELADA);
            reservaRepository.save(manutencao);
        });
    }

    public List<HorarioOcupadoDTO> findOcupados(Long recursoId, LocalDate data) {
        ZonedDateTime inicioDia = data.atStartOfDay(ZoneId.of("America/Fortaleza"));
        ZonedDateTime fimDia = inicioDia.plusDays(1).minusNanos(1);

        return reservaRepository.findReservasPorRecursoNoDia(recursoId, inicioDia, fimDia)
                .stream()
                .map(HorarioOcupadoDTO::fromEntity)
                .toList();
    }
}