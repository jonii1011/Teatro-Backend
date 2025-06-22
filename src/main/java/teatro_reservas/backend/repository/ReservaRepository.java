package teatro_reservas.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import teatro_reservas.backend.entity.Cliente;
import teatro_reservas.backend.entity.Evento;
import teatro_reservas.backend.entity.Reserva;
import teatro_reservas.backend.entity.enums.EstadoReserva;
import teatro_reservas.backend.entity.enums.TipoEntrada;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    // Buscar por código de reserva
    Optional<Reserva> findByCodigoReserva(String codigoReserva);

    // Reservas de un cliente
    List<Reserva> findByClienteIdOrderByFechaReservaDesc(Long clienteId);

    // Reservas de un evento
    List<Reserva> findByEventoIdOrderByFechaReservaDesc(Long eventoId);

    // Reservas por estado
    List<Reserva> findByEstado(EstadoReserva estado);

    // Reservas confirmadas de un cliente
    @Query("SELECT r FROM Reserva r WHERE r.cliente.id = :clienteId AND r.estado = 'CONFIRMADA'")
    List<Reserva> findReservasConfirmadasByCliente(@Param("clienteId") Long clienteId);

    // Reservas pendientes (para recordatorios de pago)
    @Query("SELECT r FROM Reserva r WHERE r.estado = 'PENDIENTE' AND " +
            "r.fechaReserva < :limite")
    List<Reserva> findReservasPendientesVencidas(@Param("limite") LocalDateTime limite);

    // Contar reservas confirmadas por evento y tipo de entrada
    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.evento.id = :eventoId AND " +
            "r.tipoEntrada = :tipoEntrada AND r.estado = 'CONFIRMADA'")
    long countReservasConfirmadasByEventoAndTipo(
            @Param("eventoId") Long eventoId,
            @Param("tipoEntrada") TipoEntrada tipoEntrada);

    // Reservas de un cliente en un año (para fidelización)
    @Query("SELECT r FROM Reserva r WHERE r.cliente.id = :clienteId AND " +
            "r.estado = 'ASISTIO' AND r.fechaReserva >= :inicioAno")
    List<Reserva> findAsistenciasPorClienteEnAno(
            @Param("clienteId") Long clienteId,
            @Param("inicioAno") LocalDateTime inicioAno);

    // Ingresos por evento
    @Query("SELECT SUM(r.precioPagado) FROM Reserva r WHERE r.evento.id = :eventoId AND " +
            "r.estado IN ('CONFIRMADA', 'ASISTIO') AND r.esPaseGratuito = false")
    BigDecimal calcularIngresosPorEvento(@Param("eventoId") Long eventoId);

    // Estadísticas por período
    @Query("SELECT DATE(r.fechaReserva), COUNT(r), SUM(r.precioPagado) " +
            "FROM Reserva r WHERE r.fechaReserva BETWEEN :desde AND :hasta " +
            "AND r.estado IN ('CONFIRMADA', 'ASISTIO') " +
            "GROUP BY DATE(r.fechaReserva) ORDER BY DATE(r.fechaReserva)")
    List<Object[]> getEstadisticasPorFecha(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    // Buscar reservas que expiran pronto (para recordatorios)
    @Query("SELECT r FROM Reserva r WHERE r.estado = 'PENDIENTE' AND " +
            "r.evento.fechaHora BETWEEN :ahora AND :limite")
    List<Reserva> findReservasQueExpiranPronto(
            @Param("ahora") LocalDateTime ahora,
            @Param("limite") LocalDateTime limite);
}
