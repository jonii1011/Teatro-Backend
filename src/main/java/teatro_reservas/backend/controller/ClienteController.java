package teatro_reservas.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import teatro_reservas.backend.dto.ClienteRequestDTO;
import teatro_reservas.backend.dto.ClienteResponseDTO;
import teatro_reservas.backend.dto.ClienteResumenDTO;
import teatro_reservas.backend.service.ClienteService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/clientes")
@Validated
@Tag(name = "Clientes", description = "Gestión de clientes del teatro")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    @Operation(
            summary = "Crear nuevo cliente",
            description = "Registra un nuevo cliente en el sistema. El email debe ser único."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Email ya existe")
    })
    public ResponseEntity<ClienteResponseDTO> crearCliente(
            @Valid @RequestBody @Parameter(description = "Datos del cliente a crear") ClienteRequestDTO clienteDTO) {
        ClienteResponseDTO cliente = clienteService.crearCliente(clienteDTO);
        return new ResponseEntity<>(cliente, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener cliente por ID",
            description = "Recupera la información completa de un cliente específico"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<ClienteResponseDTO> obtenerCliente(
            @PathVariable @Parameter(description = "ID del cliente", example = "1") Long id) {
        ClienteResponseDTO cliente = clienteService.obtenerClientePorId(id);
        return ResponseEntity.ok(cliente);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar cliente",
            description = "Actualiza los datos de un cliente existente. El email debe seguir siendo único."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
            @ApiResponse(responseCode = "409", description = "Email ya existe para otro cliente")
    })
    public ResponseEntity<ClienteResponseDTO> actualizarCliente(
            @PathVariable @Parameter(description = "ID del cliente a actualizar") Long id,
            @Valid @RequestBody @Parameter(description = "Nuevos datos del cliente") ClienteRequestDTO clienteDTO) {
        ClienteResponseDTO cliente = clienteService.actualizarCliente(id, clienteDTO);
        return ResponseEntity.ok(cliente);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar cliente",
            description = "Realiza un borrado lógico del cliente (soft delete). El cliente queda inactivo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cliente eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<Void> eliminarCliente(
            @PathVariable @Parameter(description = "ID del cliente a eliminar") Long id) {
        clienteService.eliminarCliente(id);
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/{id}/activar")
    @Operation(
            summary = "Activar cliente",
            description = "Activa un cliente que previamente había sido desactivado en el sistema"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cliente activado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
            @ApiResponse(responseCode = "400", description = "El cliente ya está activo")
    })
    public ResponseEntity<Void> activarCliente(
            @PathVariable @Parameter(description = "ID del cliente a activar") Long id) {
        clienteService.activarCliente(id);
        return ResponseEntity.noContent().build();
    }



    @GetMapping
    @Operation(
            summary = "Obtener todos los clientes",
            description = "Lista todos los clientes registrados en el sistema"
    )
    @ApiResponse(responseCode = "200", description = "Lista de clientes obtenida exitosamente")
    public ResponseEntity<List<ClienteResponseDTO>> obtenerTodosLosClientes() {
        List<ClienteResponseDTO> clientes = clienteService.obtenerTodosLosClientes();
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/activos")
    @Operation(
            summary = "Obtener clientes activos",
            description = "Lista solo los clientes que están activos en el sistema"
    )
    @ApiResponse(responseCode = "200", description = "Lista de clientes activos obtenida")
    public ResponseEntity<List<ClienteResumenDTO>> obtenerClientesActivos() {
        List<ClienteResumenDTO> clientes = clienteService.obtenerClientesActivos();
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/email/{email}")
    @Operation(
            summary = "Buscar cliente por email",
            description = "Busca un cliente específico por su dirección de email"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<ClienteResponseDTO> obtenerClientePorEmail(
            @PathVariable @Parameter(description = "Email del cliente", example = "juan@email.com") String email) {
        Optional<ClienteResponseDTO> cliente = clienteService.obtenerClientePorEmail(email);
        return cliente.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar clientes por nombre",
            description = "Busca clientes que contengan el término especificado en nombre o apellido"
    )
    @ApiResponse(responseCode = "200", description = "Búsqueda realizada exitosamente")
    public ResponseEntity<List<ClienteResumenDTO>> buscarClientesPorNombre(
            @RequestParam @Parameter(description = "Término de búsqueda", example = "Juan") String termino) {
        List<ClienteResumenDTO> clientes = clienteService.buscarClientesPorNombre(termino);
        return ResponseEntity.ok(clientes);
    }

    // Fidelización
    @GetMapping("/frecuentes")
    @Operation(
            summary = "Obtener clientes frecuentes",
            description = "Lista todos los clientes que han asistido a 5 o más eventos"
    )
    @ApiResponse(responseCode = "200", description = "Lista de clientes frecuentes obtenida")
    public ResponseEntity<List<ClienteResumenDTO>> obtenerClientesFrecuentes() {
        List<ClienteResumenDTO> clientes = clienteService.obtenerClientesFrecuentes();
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/con-pases-gratuitos")
    @Operation(
            summary = "Clientes con pases gratuitos",
            description = "Lista clientes que tienen pases gratuitos disponibles para usar"
    )
    @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    public ResponseEntity<List<ClienteResumenDTO>> obtenerClientesConPasesGratuitos() {
        List<ClienteResumenDTO> clientes = clienteService.obtenerClientesConPasesGratuitos();
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/{id}/puede-usar-pase-gratuito")
    @Operation(
            summary = "Verificar pases gratuitos disponibles",
            description = "Verifica si el cliente tiene pases gratuitos disponibles para usar"
    )
    @ApiResponse(responseCode = "200", description = "Verificación realizada")
    public ResponseEntity<Boolean> puedeUsarPaseGratuito(
            @PathVariable @Parameter(description = "ID del cliente") Long id) {
        boolean puede = clienteService.puedeUsarPaseGratuito(id);
        return ResponseEntity.ok(puede);
    }

    @PostMapping("/{id}/usar-pase-gratuito")
    @Operation(
            summary = "Usar pase gratuito",
            description = "Consume un pase gratuito del cliente. Solo se puede usar si tiene pases disponibles."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pase gratuito usado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Cliente no tiene pases gratuitos disponibles"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<Void> usarPaseGratuito(
            @PathVariable @Parameter(description = "ID del cliente") Long id) {
        clienteService.usarPaseGratuito(id);
        return ResponseEntity.ok().build();
    }

    // Estadísticas
    @GetMapping("/top-por-asistencia")
    @Operation(
            summary = "Top clientes por asistencia",
            description = "Obtiene los clientes con mayor número de eventos asistidos, con paginación"
    )
    @ApiResponse(responseCode = "200", description = "Ranking obtenido exitosamente")
    public ResponseEntity<Page<ClienteResponseDTO>> obtenerTopClientesPorAsistencia(
            @RequestParam(defaultValue = "0") @Parameter(description = "Número de página (base 0)", example = "0") int page,
            @RequestParam(defaultValue = "10") @Parameter(description = "Tamaño de página", example = "10") int size) {
        Page<ClienteResponseDTO> clientes = clienteService.obtenerTopClientesPorAsistencia(page, size);
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/registrados-en-periodo")
    @Operation(
            summary = "Clientes registrados en período",
            description = "Obtiene los clientes registrados entre dos fechas específicas"
    )
    @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    public ResponseEntity<List<ClienteResponseDTO>> obtenerClientesRegistradosEnPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Parameter(description = "Fecha desde (ISO DateTime)", example = "2024-01-01T00:00:00") LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Parameter(description = "Fecha hasta (ISO DateTime)", example = "2024-12-31T23:59:59") LocalDateTime hasta) {
        List<ClienteResponseDTO> clientes = clienteService.obtenerClientesRegistradosEnPeriodo(desde, hasta);
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/existe-email/{email}")
    @Operation(
            summary = "Verificar existencia de email",
            description = "Verifica si ya existe un cliente registrado con el email especificado"
    )
    @ApiResponse(responseCode = "200", description = "Verificación realizada")
    public ResponseEntity<Boolean> existeClientePorEmail(
            @PathVariable @Parameter(description = "Email a verificar") String email) {
        boolean existe = clienteService.existeClientePorEmail(email);
        return ResponseEntity.ok(existe);
    }

    @GetMapping("/{id}/validar-activo")
    @Operation(
            summary = "Validar cliente activo",
            description = "Valida que el cliente esté activo en el sistema"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente está activo"),
            @ApiResponse(responseCode = "400", description = "Cliente está inactivo"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<Void> validarClienteActivo(
            @PathVariable @Parameter(description = "ID del cliente") Long id) {
        clienteService.validarClienteActivo(id);
        return ResponseEntity.ok().build();
    }
}