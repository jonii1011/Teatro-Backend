package teatro_reservas.backend.service;

import teatro_reservas.backend.dto.ReservaRequestDTO;
import teatro_reservas.backend.dto.ReservaResponseDTO;
import teatro_reservas.backend.dto.ReservaResumenDTO;
import teatro_reservas.backend.entity.Reserva;
import teatro_reservas.backend.entity.enums.EstadoReserva;
import teatro_reservas.backend.entity.enums.TipoEntrada;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservaService {

    // CRUD básico
    ReservaResponseDTO crearReserva(ReservaRequestDTO reservaDTO);
    ReservaResponseDTO obtenerReservaPorId(Long id);
    void eliminarReserva(Long id);

    // Búsquedas
    List<ReservaResponseDTO> obtenerTodasLasReservas();
    List<ReservaResumenDTO> obtenerReservasPorCliente(Long clienteId);
    List<ReservaResumenDTO> obtenerReservasPorEvento(Long eventoId);
    Optional<ReservaResponseDTO> obtenerReservaPorCodigo(String codigoReserva);
    List<ReservaResponseDTO> obtenerReservasPorEstado(EstadoReserva estado);

    // Gestión de estados
    ReservaResponseDTO confirmarReserva(Long reservaId, BigDecimal precio);
    ReservaResponseDTO cancelarReserva(Long reservaId, String motivo);
    ReservaResponseDTO marcarAsistencia(Long reservaId);
    ReservaResponseDTO marcarNoAsistencia(Long reservaId);

    // Pases gratuitos
    ReservaResponseDTO crearReservaConPaseGratuito(Long clienteId, Long eventoId, TipoEntrada tipoEntrada);

    // Consultas específicas
    List<ReservaResponseDTO> obtenerReservasConfirmadas(Long clienteId);
    List<ReservaResponseDTO> obtenerReservasPendientesVencidas(int horasVencimiento);
    List<ReservaResponseDTO> obtenerReservasQueExpiranPronto(int horasAntes);

    // Validaciones
    void validarReservaCancelable(Long reservaId);
    void validarReservaConfirmable(Long reservaId);
    boolean puedeCrearReserva(Long clienteId, Long eventoId, TipoEntrada tipoEntrada);

    // Estadísticas y reportes
    BigDecimal calcularIngresosPorEvento(Long eventoId);
    long contarReservasConfirmadasPorEventoYTipo(Long eventoId, TipoEntrada tipoEntrada);
    List<Object[]> obtenerEstadisticasPorFecha(LocalDateTime desde, LocalDateTime hasta);
}
