package imd.ufrn.com.br.smart_space_booking.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import imd.ufrn.com.br.smart_space_booking.enums.ReservaStatus;
import imd.ufrn.com.br.smart_space_booking.enums.ReservaTipo;
import imd.ufrn.com.br.smart_space_booking.model.Reserva;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    @Query("SELECT r FROM Reserva r WHERE r.sala.id = :salaId " +
            "AND r.inicioDateTime >= :inicioDia AND r.fimDateTime <= :fimDia " +
            "AND r.status <> 'CANCELADA' " +
            "ORDER BY r.inicioDateTime ASC")
    List<Reserva> findReservasPorSalaNoDia(
            @Param("salaId") Long salaId,
            @Param("inicioDia") ZonedDateTime inicioDia,
            @Param("fimDia") ZonedDateTime fimDia);

    @Query("""
        SELECT COUNT(r) > 0 FROM Reserva r
        WHERE r.sala.id = :salaId
        AND r.status <> 'CANCELADA'
        AND r.inicioDateTime < :fim
        AND r.fimDateTime > :inicio
    """)
    boolean existeConflito(
            @Param("salaId") Long salaId,
            @Param("inicio") ZonedDateTime inicio,
            @Param("fim") ZonedDateTime fim
    );

    Optional<Reserva> findBySalaIdAndInicioDateTimeAndTipo(Long salaId, ZonedDateTime inicioDateTime, ReservaTipo tipo);

    long countByUsuarioIdAndStatusAndDataHoraCancelamento(Long usuarioId, ReservaStatus status, ZonedDateTime dataLimite);

    @Query("SELECT r FROM Reserva r WHERE r.usuario.id = :usuarioId " +
            "AND r.tipo <> 'MANUTENCAO' " +
            "ORDER BY r.createdAt DESC")
    List<Reserva> findReservasPorUsuario(@Param("usuarioId") Long usuarioId);

    @Query("SELECT r FROM Reserva r WHERE r.status = 'CONFIRMADA' " +
            "AND r.dataHoraCheckin IS NULL " +
            "AND r.inicioDateTime <= :limiteTolerancia")
    List<Reserva> findReservasPendentesExpiradas(@Param("limiteTolerancia") ZonedDateTime limiteTolerancia);
}
