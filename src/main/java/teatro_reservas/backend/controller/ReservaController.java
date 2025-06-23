package teatro_reservas.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import teatro_reservas.backend.dto.ReservaRequestDTO;
import teatro_reservas.backend.dto.ReservaResponseDTO;
import teatro_reservas.backend.dto.ReservaResumenDTO;
import teatro_reservas.backend.entity.enums.EstadoReserva;
import teatro_reservas.backend.entity.enums.TipoEntrada;
import teatro_reservas.backend.service.ReservaService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/reservas")
@Validated
@Tag(name = "Reservas", description = "Gestión completa del ciclo de vida de reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping
    @Operation(
            summary = "Crear nueva reserva",
            description = "Crea una nueva reserva para un cliente y evento específico. Valida disponibilidad y compatibilidad del tipo de entrada."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reserva creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos, sin disponibilidad o tipo de entrada incompatible"),
            @ApiResponse(responseCode = "404", description = "Cliente o evento no encontrado")
    })
    public ResponseEntity<ReservaResponseDTO> crearReserva(
            @Valid @RequestBody @Parameter(description = "Datos de la reserva a crear") ReservaRequestDTO reservaDTO) {
        ReservaResponseDTO reserva = reservaService.crearReserva(reservaDTO);
        return new ResponseEntity<>(reserva, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener reserva por ID",
            description = "Recupera la información completa de una reserva específica"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva encontrada"),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada")
    })
    public ResponseEntity<ReservaResponseDTO> obtenerReserva(
            @PathVariable @Parameter(description = "ID de la reserva", example = "1") Long id) {
        ReservaResponseDTO reserva = reservaService.obtenerReservaPorId(id);
        return ResponseEntity.ok(reserva);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar reserva",
            description = "Elimina una reserva del sistema. Solo se pueden eliminar reservas que pueden ser canceladas."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Reserva eliminada exitosamente"),
            @ApiResponse(responseCode = "400", description = "La reserva no puede ser eliminada"),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada")
    })
    public ResponseEntity<Void> eliminarReserva(
            @PathVariable @Parameter(description = "ID de la reserva a eliminar") Long id) {
        reservaService.eliminarReserva(id);
        return ResponseEntity.noContent().build();
    }

    // Búsquedas
    @GetMapping
    @Operation(
            summary = "Obtener todas las reservas",
            description = "Lista todas las reservas registradas en el sistema"
    )
    @ApiResponse(responseCode = "200", description = "Lista de reservas obtenida exitosamente")
    public ResponseEntity<List<ReservaResponseDTO>> obtenerTodasLasReservas() {
        List<ReservaResponseDTO> reservas = reservaService.obtenerTodasLasReservas();
        return ResponseEntity.ok(reservas);
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(
            summary = "Reservas por cliente",
            description = "Obtiene todas las reservas de un cliente específico, ordenadas por fecha"
    )
    @ApiResponse(responseCode = "200", description = "Lista de reservas del cliente obtenida")
    public ResponseEntity<List<ReservaResumenDTO>> obtenerReservasPorCliente(
            @PathVariable @Parameter(description = "ID del cliente") Long clienteId) {
        List<ReservaResumenDTO> reservas = reservaService.obtenerReservasPorCliente(clienteId);
        return ResponseEntity.ok(reservas);
    }

    @GetMapping("/evento/{eventoId}")
    @Operation(
            summary = "Reservas por evento",
            description = "Obtiene todas las reservas de un evento específico"
    )
    @ApiResponse(responseCode = "200", description = "Lista de reservas del evento obtenida")
    public ResponseEntity<List<ReservaResumenDTO>> obtenerReservasPorEvento(
            @PathVariable @Parameter(description = "ID del evento") Long eventoId) {
        List<ReservaResumenDTO> reservas = reservaService.obtenerReservasPorEvento(eventoId);
        return ResponseEntity.ok(reservas);
    }

    @GetMapping("/codigo/{codigoReserva}")
    @Operation(
            summary = "Buscar reserva por código",
            description = "Busca una reserva específica por su código único generado automáticamente"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva encontrada"),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada")
    })
    public ResponseEntity<ReservaResponseDTO> obtenerReservaPorCodigo(
            @PathVariable @Parameter(description = "Código de la reserva", example = "RES-ABC12345") String codigoReserva) {
        Optional<ReservaResponseDTO> reserva = reservaService.obtenerReservaPorCodigo(codigoReserva);
        return reserva.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/estado/{estado}")
    @Operation(
            summary = "Reservas por estado",
            description = "Lista todas las reservas que tienen un estado específico"
    )
    @ApiResponse(responseCode = "200", description = "Lista de reservas con el estado especificado")
    public ResponseEntity<List<ReservaResponseDTO>> obtenerReservasPorEstado(
            @PathVariable @Parameter(description = "Estado de la reserva", example = "CONFIRMADA") EstadoReserva estado) {
        List<ReservaResponseDTO> reservas = reservaService.obtenerReservasPorEstado(estado);
        return ResponseEntity.ok(reservas);
    }

    // Gestión de estados
    @PutMapping("/{id}/confirmar")
    @Operation(
            summary = "Confirmar reserva",
            description = "Confirma una reserva pendiente registrando el pago. Solo se pueden confirmar reservas en estado PENDIENTE."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva confirmada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Solo se pueden confirmar reservas pendientes"),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada")
    })
    public ResponseEntity<ReservaResponseDTO> confirmarReserva(@PathVariable Long id) {
        ReservaResponseDTO reserva = reservaService.confirmarReserva(id);
        return ResponseEntity.ok(reserva);
    }

    @PutMapping("/{id}/cancelar")
    @Operation(
            summary = "Cancelar reserva",
            description = "Cancela una reserva existente. Si era un pase gratuito, se devuelve al cliente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva cancelada exitosamente"),
            @ApiResponse(responseCode = "400", description = "La reserva no puede ser cancelada"),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada")
    })
    public ResponseEntity<ReservaResponseDTO> cancelarReserva(
            @PathVariable @Parameter(description = "ID de la reserva") Long id,
            @RequestParam @Size(max = 500) @Parameter(description = "Motivo de la cancelación", example = "Cliente no puede asistir") String motivo) {
        ReservaResponseDTO reserva = reservaService.cancelarReserva(id, motivo);
        return ResponseEntity.ok(reserva);
    }

    // Pases gratuitos
    @PostMapping("/con-pase-gratuito")
    @Operation(
            summary = "Crear reserva con pase gratuito",
            description = "Crea una reserva utilizando un pase gratuito del cliente. El cliente debe tener pases disponibles."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reserva con pase gratuito creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Cliente no tiene pases gratuitos disponibles"),
            @ApiResponse(responseCode = "404", description = "Cliente o evento no encontrado")
    })
    public ResponseEntity<ReservaResponseDTO> crearReservaConPaseGratuito(
            @RequestParam @Parameter(description = "ID del cliente") Long clienteId,
            @RequestParam @Parameter(description = "ID del evento") Long eventoId,
            @RequestParam @Parameter(description = "Tipo de entrada") TipoEntrada tipoEntrada) {
        ReservaResponseDTO reserva = reservaService.crearReservaConPaseGratuito(clienteId, eventoId, tipoEntrada);
        return new ResponseEntity<>(reserva, HttpStatus.CREATED);
    }

    // Consultas específicas
    @GetMapping("/cliente/{clienteId}/confirmadas")
    @Operation(
            summary = "Reservas confirmadas del cliente",
            description = "Obtiene solo las reservas confirmadas de un cliente específico"
    )
    @ApiResponse(responseCode = "200", description = "Lista de reservas confirmadas obtenida")
    public ResponseEntity<List<ReservaResponseDTO>> obtenerReservasConfirmadas(
            @PathVariable @Parameter(description = "ID del cliente") Long clienteId) {
        List<ReservaResponseDTO> reservas = reservaService.obtenerReservasConfirmadas(clienteId);
        return ResponseEntity.ok(reservas);
    }

    @GetMapping("/pendientes-vencidas")
    @Operation(
            summary = "Reservas pendientes vencidas",
            description = "Obtiene reservas que están pendientes de pago más allá del tiempo límite especificado"
    )
    @ApiResponse(responseCode = "200", description = "Lista de reservas vencidas obtenida")
    public ResponseEntity<List<ReservaResponseDTO>> obtenerReservasPendientesVencidas(
            @RequestParam(defaultValue = "24") @Parameter(description = "Horas de vencimiento", example = "24") int horasVencimiento) {
        List<ReservaResponseDTO> reservas = reservaService.obtenerReservasPendientesVencidas(horasVencimiento);
        return ResponseEntity.ok(reservas);
    }

    @GetMapping("/expiran-pronto")
    @Operation(
            summary = "Reservas que expiran pronto",
            description = "Obtiene reservas cuyos eventos ocurrirán pronto (útil para recordatorios)"
    )
    @ApiResponse(responseCode = "200", description = "Lista de reservas próximas a expirar")
    public ResponseEntity<List<ReservaResponseDTO>> obtenerReservasQueExpiranPronto(
            @RequestParam(defaultValue = "24") @Parameter(description = "Horas antes del evento", example = "24") int horasAntes) {
        List<ReservaResponseDTO> reservas = reservaService.obtenerReservasQueExpiranPronto(horasAntes);
        return ResponseEntity.ok(reservas);
    }

    // Validaciones
    @GetMapping("/{id}/validar-cancelable")
    @Operation(
            summary = "Validar si reserva es cancelable",
            description = "Valida si una reserva puede ser cancelada según su estado actual"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "La reserva puede ser cancelada"),
            @ApiResponse(responseCode = "400", description = "La reserva no puede ser cancelada")
    })
    public ResponseEntity<Void> validarReservaCancelable(
            @PathVariable @Parameter(description = "ID de la reserva") Long id) {
        reservaService.validarReservaCancelable(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/validar-confirmable")
    @Operation(
            summary = "Validar si reserva es confirmable",
            description = "Valida si una reserva puede ser confirmada (debe estar en estado PENDIENTE)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "La reserva puede ser confirmada"),
            @ApiResponse(responseCode = "400", description = "Solo se pueden confirmar reservas pendientes")
    })
    public ResponseEntity<Void> validarReservaConfirmable(
            @PathVariable @Parameter(description = "ID de la reserva") Long id) {
        reservaService.validarReservaConfirmable(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/puede-crear")
    @Operation(
            summary = "Verificar si se puede crear reserva",
            description = "Verifica si es posible crear una reserva para un cliente, evento y tipo de entrada específicos"
    )
    @ApiResponse(responseCode = "200", description = "Verificación realizada")
    public ResponseEntity<Boolean> puedeCrearReserva(
            @RequestParam @Parameter(description = "ID del cliente") Long clienteId,
            @RequestParam @Parameter(description = "ID del evento") Long eventoId,
            @RequestParam @Parameter(description = "Tipo de entrada") TipoEntrada tipoEntrada) {
        boolean puede = reservaService.puedeCrearReserva(clienteId, eventoId, tipoEntrada);
        return ResponseEntity.ok(puede);
    }

    // Estadísticas y reportes
    @GetMapping("/evento/{eventoId}/ingresos")
    @Operation(
            summary = "Calcular ingresos por evento",
            description = "Calcula el total de ingresos generados por un evento específico (solo reservas confirmadas)"
    )
    @ApiResponse(responseCode = "200", description = "Ingresos calculados exitosamente")
    public ResponseEntity<BigDecimal> calcularIngresosPorEvento(
            @PathVariable @Parameter(description = "ID del evento") Long eventoId) {
        BigDecimal ingresos = reservaService.calcularIngresosPorEvento(eventoId);
        return ResponseEntity.ok(ingresos);
    }

    @GetMapping("/evento/{eventoId}/contar/{tipoEntrada}")
    @Operation(
            summary = "Contar reservas por tipo",
            description = "Cuenta las reservas confirmadas de un evento específico para un tipo de entrada"
    )
    @ApiResponse(responseCode = "200", description = "Conteo realizado exitosamente")
    public ResponseEntity<Long> contarReservasConfirmadasPorEventoYTipo(
            @PathVariable @Parameter(description = "ID del evento") Long eventoId,
            @PathVariable @Parameter(description = "Tipo de entrada") TipoEntrada tipoEntrada) {
        long count = reservaService.contarReservasConfirmadasPorEventoYTipo(eventoId, tipoEntrada);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/estadisticas-por-fecha")
    @Operation(
            summary = "Estadísticas por rango de fechas",
            description = "Obtiene estadísticas de reservas agrupadas por fecha en un período específico"
    )
    @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas exitosamente")
    public ResponseEntity<List<Object[]>> obtenerEstadisticasPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Parameter(description = "Fecha desde") LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Parameter(description = "Fecha hasta") LocalDateTime hasta) {
        List<Object[]> estadisticas = reservaService.obtenerEstadisticasPorFecha(desde, hasta);
        return ResponseEntity.ok(estadisticas);
    }

    // Endpoints útiles para el frontend
    @GetMapping("/estados")
    @Operation(
            summary = "Obtener estados de reserva",
            description = "Lista todos los estados posibles de una reserva"
    )
    @ApiResponse(responseCode = "200", description = "Lista de estados obtenida")
    public ResponseEntity<EstadoReserva[]> obtenerEstadosReserva() {
        return ResponseEntity.ok(EstadoReserva.values());
    }

    @GetMapping("/{id}/resumen")
    @Operation(
            summary = "Resumen de reserva",
            description = "Obtiene un resumen con la información más importante de una reserva"
    )
    @ApiResponse(responseCode = "200", description = "Resumen obtenido exitosamente")
    public ResponseEntity<Map<String, Object>> obtenerResumenReserva(
            @PathVariable @Parameter(description = "ID de la reserva") Long id) {
        ReservaResponseDTO reserva = reservaService.obtenerReservaPorId(id);

        Map<String, Object> resumen = new HashMap<>();
        resumen.put("codigoReserva", reserva.getCodigoReserva());
        resumen.put("nombreCliente", reserva.getCliente().getNombre() + " " + reserva.getCliente().getApellido());
        resumen.put("nombreEvento", reserva.getEvento().getNombre());
        resumen.put("fechaEvento", reserva.getEvento().getFechaHora());
        resumen.put("tipoEntrada", reserva.getTipoEntrada());
        resumen.put("estado", reserva.getEstado());
        resumen.put("precioPagado", reserva.getPrecioPagado());
        resumen.put("esPaseGratuito", reserva.getEsPaseGratuito());
        resumen.put("puedeSerCancelada", reserva.getPuedeSerCancelada());

        return ResponseEntity.ok(resumen);
    }

    @PostMapping("/crear-y-confirmar")
    @Operation(
            summary = "Crear y confirmar reserva",
            description = "Crea una reserva y la confirma inmediatamente en una sola operación (excepto pases gratuitos)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva creada y confirmada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Error en creación o confirmación")
    })
    public ResponseEntity<ReservaResponseDTO> crearYConfirmarReserva(
            @Valid @RequestBody @Parameter(description = "Datos de la reserva") ReservaRequestDTO reservaDTO) {

        // Crear la reserva
        ReservaResponseDTO reserva = reservaService.crearReserva(reservaDTO);

        // Si no es pase gratuito, confirmarla inmediatamente
        if (!reserva.getEsPaseGratuito()) {
            reserva = reservaService.confirmarReserva(reserva.getId());
        }

        return ResponseEntity.ok(reserva);
    }

    @GetMapping("/{id}/historial")
    @Operation(
            summary = "Historial de reserva",
            description = "Obtiene el historial completo de cambios de estado de una reserva"
    )
    @ApiResponse(responseCode = "200", description = "Historial obtenido exitosamente")
    public ResponseEntity<Map<String, Object>> obtenerHistorialReserva(
            @PathVariable @Parameter(description = "ID de la reserva") Long id) {
        ReservaResponseDTO reserva = reservaService.obtenerReservaPorId(id);

        Map<String, Object> historial = new HashMap<>();
        historial.put("fechaReserva", reserva.getFechaReserva());
        historial.put("fechaConfirmacion", reserva.getFechaConfirmacion());
        historial.put("fechaCancelacion", reserva.getFechaCancelacion());
        historial.put("motivoCancelacion", reserva.getMotivoCancelacion());
        historial.put("estadoActual", reserva.getEstado());
        historial.put("cambiosDeEstado", obtenerCambiosDeEstado(reserva));

        return ResponseEntity.ok(historial);
    }

    @Operation(hidden = true)
    private List<Map<String, Object>> obtenerCambiosDeEstado(ReservaResponseDTO reserva) {
        List<Map<String, Object>> cambios = new ArrayList<>();

        // Reserva creada
        Map<String, Object> creacion = new HashMap<>();
        creacion.put("estado", "CREADA");
        creacion.put("fecha", reserva.getFechaReserva());
        creacion.put("descripcion", "Reserva creada");
        cambios.add(creacion);

        // Si fue confirmada
        if (reserva.getFechaConfirmacion() != null) {
            Map<String, Object> confirmacion = new HashMap<>();
            confirmacion.put("estado", "CONFIRMADA");
            confirmacion.put("fecha", reserva.getFechaConfirmacion());
            confirmacion.put("descripcion", "Reserva confirmada - Precio: $" + reserva.getPrecioPagado());
            cambios.add(confirmacion);
        }

        // Si fue cancelada
        if (reserva.getFechaCancelacion() != null) {
            Map<String, Object> cancelacion = new HashMap<>();
            cancelacion.put("estado", "CANCELADA");
            cancelacion.put("fecha", reserva.getFechaCancelacion());
            cancelacion.put("descripcion", "Reserva cancelada - Motivo: " + reserva.getMotivoCancelacion());
            cambios.add(cancelacion);
        }

        return cambios;
    }
}
