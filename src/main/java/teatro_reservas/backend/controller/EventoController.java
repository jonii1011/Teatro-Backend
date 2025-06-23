package teatro_reservas.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import teatro_reservas.backend.dto.EventoRequestDTO;
import teatro_reservas.backend.dto.EventoResponseDTO;
import teatro_reservas.backend.dto.EventoResumenDTO;
import teatro_reservas.backend.entity.enums.TipoEntrada;
import teatro_reservas.backend.entity.enums.TipoEvento;
import teatro_reservas.backend.service.EventoService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/eventos")
@Validated
@Tag(name = "Eventos", description = "Gestión de eventos del teatro")
public class EventoController {

    private final EventoService eventoService;

    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    // CRUD básico
    @PostMapping
    @Operation(
            summary = "Crear nuevo evento",
            description = "Crea un nuevo evento con configuración de precios y capacidades por tipo de entrada. Los tipos de entrada deben ser compatibles con el tipo de evento."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Evento creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o tipos de entrada incompatibles")
    })
    public ResponseEntity<EventoResponseDTO> crearEvento(
            @Valid @RequestBody @Parameter(description = "Configuración del evento") EventoRequestDTO eventoDTO) {
        EventoResponseDTO evento = eventoService.crearEvento(eventoDTO);
        return new ResponseEntity<>(evento, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener evento por ID",
            description = "Recupera la información completa de un evento específico"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento encontrado"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado")
    })
    public ResponseEntity<EventoResponseDTO> obtenerEvento(
            @PathVariable @Parameter(description = "ID del evento", example = "1") Long id) {
        EventoResponseDTO evento = eventoService.obtenerEventoPorId(id);
        return ResponseEntity.ok(evento);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar evento",
            description = "Actualiza los datos de un evento existente. No se puede actualizar un evento que ya pasó."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "No se puede actualizar un evento que ya pasó"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado")
    })
    public ResponseEntity<EventoResponseDTO> actualizarEvento(
            @PathVariable @Parameter(description = "ID del evento a actualizar") Long id,
            @Valid @RequestBody @Parameter(description = "Nuevos datos del evento") EventoRequestDTO eventoDTO) {
        EventoResponseDTO evento = eventoService.actualizarEvento(id, eventoDTO);
        return ResponseEntity.ok(evento);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar evento",
            description = "Realiza un borrado lógico del evento. No se puede eliminar si tiene reservas confirmadas."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Evento eliminado exitosamente"),
            @ApiResponse(responseCode = "400", description = "No se puede eliminar evento con reservas confirmadas"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado")
    })
    public ResponseEntity<Void> eliminarEvento(
            @PathVariable @Parameter(description = "ID del evento a eliminar") Long id) {
        eventoService.eliminarEvento(id);
        return ResponseEntity.noContent().build();
    }

    // Consultas básicas
    @GetMapping
    @Operation(
            summary = "Obtener todos los eventos",
            description = "Lista todos los eventos registrados en el sistema"
    )
    @ApiResponse(responseCode = "200", description = "Lista de eventos obtenida exitosamente")
    public ResponseEntity<List<EventoResponseDTO>> obtenerTodosLosEventos() {
        List<EventoResponseDTO> eventos = eventoService.obtenerTodosLosEventos();
        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/vigentes")
    @Operation(
            summary = "Obtener eventos vigentes",
            description = "Lista solo los eventos activos con fecha futura"
    )
    @ApiResponse(responseCode = "200", description = "Lista de eventos vigentes obtenida")
    public ResponseEntity<List<EventoResumenDTO>> obtenerEventosVigentes() {
        List<EventoResumenDTO> eventos = eventoService.obtenerEventosVigentes();
        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/tipo/{tipoEvento}")
    @Operation(
            summary = "Obtener eventos por tipo",
            description = "Lista eventos filtrados por tipo específico"
    )
    @ApiResponse(responseCode = "200", description = "Lista de eventos del tipo especificado")
    public ResponseEntity<List<EventoResumenDTO>> obtenerEventosPorTipo(
            @PathVariable @Parameter(description = "Tipo de evento", example = "OBRA_TEATRO") TipoEvento tipoEvento) {
        List<EventoResumenDTO> eventos = eventoService.obtenerEventosPorTipo(tipoEvento);
        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar eventos por nombre",
            description = "Busca eventos que contengan el término especificado en el nombre"
    )
    @ApiResponse(responseCode = "200", description = "Búsqueda realizada exitosamente")
    public ResponseEntity<List<EventoResumenDTO>> buscarEventosPorNombre(
            @RequestParam @Parameter(description = "Término de búsqueda", example = "Romeo") String nombre) {
        List<EventoResumenDTO> eventos = eventoService.buscarEventosPorNombre(nombre);
        return ResponseEntity.ok(eventos);
    }

    // Disponibilidad
    @GetMapping("/con-disponibilidad")
    @Operation(
            summary = "Eventos con disponibilidad",
            description = "Lista eventos que tienen al menos una entrada disponible"
    )
    @ApiResponse(responseCode = "200", description = "Lista de eventos con disponibilidad")
    public ResponseEntity<List<EventoResumenDTO>> obtenerEventosConDisponibilidad() {
        List<EventoResumenDTO> eventos = eventoService.obtenerEventosConDisponibilidad();
        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/{id}/disponibilidad/{tipoEntrada}")
    @Operation(
            summary = "Verificar disponibilidad específica",
            description = "Verifica si hay disponibilidad para un tipo específico de entrada en un evento"
    )
    @ApiResponse(responseCode = "200", description = "Verificación realizada")
    public ResponseEntity<Boolean> tieneDisponibilidad(
            @PathVariable @Parameter(description = "ID del evento") Long id,
            @PathVariable @Parameter(description = "Tipo de entrada", example = "GENERAL") TipoEntrada tipoEntrada) {
        boolean disponible = eventoService.tieneDisponibilidad(id, tipoEntrada);
        return ResponseEntity.ok(disponible);
    }

    @GetMapping("/{id}/capacidad-disponible/{tipoEntrada}")
    @Operation(
            summary = "Obtener capacidad disponible",
            description = "Obtiene la cantidad exacta de entradas disponibles para un tipo específico"
    )
    @ApiResponse(responseCode = "200", description = "Capacidad disponible obtenida")
    public ResponseEntity<Long> obtenerCapacidadDisponible(
            @PathVariable @Parameter(description = "ID del evento") Long id,
            @PathVariable @Parameter(description = "Tipo de entrada") TipoEntrada tipoEntrada) {
        long capacidad = eventoService.obtenerCapacidadDisponible(id, tipoEntrada);
        return ResponseEntity.ok(capacidad);
    }

    @GetMapping("/{id}/disponibilidad-por-tipo")
    @Operation(
            summary = "Disponibilidad por todos los tipos",
            description = "Obtiene la disponibilidad de todos los tipos de entrada del evento"
    )
    @ApiResponse(responseCode = "200", description = "Mapa de disponibilidad obtenido")
    public ResponseEntity<Map<TipoEntrada, Long>> obtenerDisponibilidadPorTipo(
            @PathVariable @Parameter(description = "ID del evento") Long id) {
        Map<TipoEntrada, Long> disponibilidad = eventoService.obtenerDisponibilidadPorTipo(id);
        return ResponseEntity.ok(disponibilidad);
    }

    @GetMapping("/proximos")
    @Operation(
            summary = "Eventos próximos",
            description = "Obtiene eventos que ocurrirán en los próximos días especificados"
    )
    @ApiResponse(responseCode = "200", description = "Lista de eventos próximos")
    public ResponseEntity<List<EventoResumenDTO>> obtenerEventosProximos(
            @RequestParam(defaultValue = "7") @Parameter(description = "Días hacia adelante", example = "7") int dias) {
        List<EventoResumenDTO> eventos = eventoService.obtenerEventosProximos(dias);
        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/en-rango")
    @Operation(
            summary = "Eventos en rango de fechas",
            description = "Obtiene eventos entre dos fechas específicas"
    )
    @ApiResponse(responseCode = "200", description = "Lista de eventos en el rango")
    public ResponseEntity<List<EventoResumenDTO>> obtenerEventosEnRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Parameter(description = "Fecha desde", example = "2024-01-01T00:00:00") LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Parameter(description = "Fecha hasta", example = "2024-12-31T23:59:59") LocalDateTime hasta) {
        List<EventoResumenDTO> eventos = eventoService.obtenerEventosEnRango(desde, hasta);
        return ResponseEntity.ok(eventos);
    }

    // Precios
    @GetMapping("/{id}/precio/{tipoEntrada}")
    @Operation(
            summary = "Obtener precio de entrada",
            description = "Obtiene el precio de un tipo específico de entrada para un evento"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Precio obtenido"),
            @ApiResponse(responseCode = "400", description = "Tipo de entrada no disponible para este evento")
    })
    public ResponseEntity<BigDecimal> obtenerPrecioEntrada(
            @PathVariable @Parameter(description = "ID del evento") Long id,
            @PathVariable @Parameter(description = "Tipo de entrada") TipoEntrada tipoEntrada) {
        BigDecimal precio = eventoService.obtenerPrecioEntrada(id, tipoEntrada);
        return ResponseEntity.ok(precio);
    }

    @GetMapping("/{id}/precios")
    @Operation(
            summary = "Obtener todos los precios",
            description = "Obtiene todos los precios configurados para un evento"
    )
    @ApiResponse(responseCode = "200", description = "Mapa de precios obtenido")
    public ResponseEntity<Map<TipoEntrada, BigDecimal>> obtenerTodosLosPrecios(
            @PathVariable @Parameter(description = "ID del evento") Long id) {
        Map<TipoEntrada, BigDecimal> precios = eventoService.obtenerTodosLosPrecios(id);
        return ResponseEntity.ok(precios);
    }

    // Validaciones
    @GetMapping("/{id}/validar-vigente")
    @Operation(
            summary = "Validar evento vigente",
            description = "Valida que el evento esté activo y con fecha futura"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento está vigente"),
            @ApiResponse(responseCode = "400", description = "Evento no está vigente")
    })
    public ResponseEntity<Void> validarEventoVigente(
            @PathVariable @Parameter(description = "ID del evento") Long id) {
        eventoService.validarEventoVigente(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/validar-compatibilidad/{tipoEntrada}")
    @Operation(
            summary = "Validar compatibilidad de entrada",
            description = "Valida que el tipo de entrada sea compatible con el tipo de evento"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tipo de entrada es compatible"),
            @ApiResponse(responseCode = "400", description = "Tipo de entrada no válido para este evento")
    })
    public ResponseEntity<Void> validarCompatibilidadTipoEntrada(
            @PathVariable @Parameter(description = "ID del evento") Long id,
            @PathVariable @Parameter(description = "Tipo de entrada") TipoEntrada tipoEntrada) {
        eventoService.validarCompatibilidadTipoEntrada(id, tipoEntrada);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/validar-disponibilidad/{tipoEntrada}")
    @Operation(
            summary = "Validar disponibilidad",
            description = "Valida que haya disponibilidad para el tipo de entrada especificado"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hay disponibilidad"),
            @ApiResponse(responseCode = "400", description = "No hay disponibilidad")
    })
    public ResponseEntity<Void> validarDisponibilidad(
            @PathVariable @Parameter(description = "ID del evento") Long id,
            @PathVariable @Parameter(description = "Tipo de entrada") TipoEntrada tipoEntrada) {
        eventoService.validarDisponibilidad(id, tipoEntrada);
        return ResponseEntity.ok().build();
    }

    // Estadísticas
    @GetMapping("/mas-populares")
    @Operation(
            summary = "Eventos más populares",
            description = "Obtiene los eventos más populares (con más reservas) en un período específico"
    )
    @ApiResponse(responseCode = "200", description = "Lista de eventos populares obtenida")
    public ResponseEntity<List<EventoResumenDTO>> obtenerEventosMasPopulares(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Parameter(description = "Fecha desde") LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Parameter(description = "Fecha hasta") LocalDateTime hasta) {
        List<EventoResumenDTO> eventos = eventoService.obtenerEventosMasPopulares(desde, hasta);
        return ResponseEntity.ok(eventos);
    }

    // Endpoints útiles para el frontend
    @GetMapping("/tipos-evento")
    @Operation(
            summary = "Obtener tipos de evento",
            description = "Lista todos los tipos de evento disponibles en el sistema"
    )
    @ApiResponse(responseCode = "200", description = "Lista de tipos de evento")
    public ResponseEntity<TipoEvento[]> obtenerTiposEvento() {
        return ResponseEntity.ok(TipoEvento.values());
    }

    @GetMapping("/tipos-entrada")
    @Operation(
            summary = "Obtener tipos de entrada",
            description = "Lista todos los tipos de entrada disponibles en el sistema"
    )
    @ApiResponse(responseCode = "200", description = "Lista de tipos de entrada")
    public ResponseEntity<TipoEntrada[]> obtenerTiposEntrada() {
        return ResponseEntity.ok(TipoEntrada.values());
    }

    @GetMapping("/{id}/info-completa")
    @Operation(
            summary = "Información completa del evento",
            description = "Obtiene toda la información del evento incluyendo disponibilidad y precios en un solo endpoint"
    )
    @ApiResponse(responseCode = "200", description = "Información completa obtenida")
    public ResponseEntity<Map<String, Object>> obtenerInfoCompleta(
            @PathVariable @Parameter(description = "ID del evento") Long id) {
        EventoResponseDTO evento = eventoService.obtenerEventoPorId(id);
        Map<TipoEntrada, Long> disponibilidad = eventoService.obtenerDisponibilidadPorTipo(id);
        Map<TipoEntrada, BigDecimal> precios = eventoService.obtenerTodosLosPrecios(id);

        Map<String, Object> info = new HashMap<>();
        info.put("evento", evento);
        info.put("disponibilidad", disponibilidad);
        info.put("precios", precios);

        return ResponseEntity.ok(info);
    }
}
