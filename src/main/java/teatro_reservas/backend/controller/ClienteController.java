package teatro_reservas.backend.controller;

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
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> crearCliente(@Valid @RequestBody ClienteRequestDTO clienteDTO) {
        ClienteResponseDTO cliente = clienteService.crearCliente(clienteDTO);
        return new ResponseEntity<>(cliente, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> obtenerCliente(@PathVariable Long id) {
        ClienteResponseDTO cliente = clienteService.obtenerClientePorId(id);
        return ResponseEntity.ok(cliente);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> actualizarCliente(
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequestDTO clienteDTO) {
        ClienteResponseDTO cliente = clienteService.actualizarCliente(id, clienteDTO);
        return ResponseEntity.ok(cliente);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long id) {
        clienteService.eliminarCliente(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> obtenerTodosLosClientes() {
        List<ClienteResponseDTO> clientes = clienteService.obtenerTodosLosClientes();
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/activos")
    public ResponseEntity<List<ClienteResumenDTO>> obtenerClientesActivos() {
        List<ClienteResumenDTO> clientes = clienteService.obtenerClientesActivos();
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ClienteResponseDTO> obtenerClientePorEmail(@PathVariable String email) {
        Optional<ClienteResponseDTO> cliente = clienteService.obtenerClientePorEmail(email);
        return cliente.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ClienteResumenDTO>> buscarClientesPorNombre(
            @RequestParam String termino) {
        List<ClienteResumenDTO> clientes = clienteService.buscarClientesPorNombre(termino);
        return ResponseEntity.ok(clientes);
    }

    // Fidelización
    @GetMapping("/frecuentes")
    public ResponseEntity<List<ClienteResumenDTO>> obtenerClientesFrecuentes() {
        List<ClienteResumenDTO> clientes = clienteService.obtenerClientesFrecuentes();
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/con-pases-gratuitos")
    public ResponseEntity<List<ClienteResumenDTO>> obtenerClientesConPasesGratuitos() {
        List<ClienteResumenDTO> clientes = clienteService.obtenerClientesConPasesGratuitos();
        return ResponseEntity.ok(clientes);
    }

    @PostMapping("/{id}/procesar-asistencia")
    public ResponseEntity<Void> procesarAsistenciaEvento(@PathVariable Long id) {
        clienteService.procesarAsistenciaEvento(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/puede-usar-pase-gratuito")
    public ResponseEntity<Boolean> puedeUsarPaseGratuito(@PathVariable Long id) {
        boolean puede = clienteService.puedeUsarPaseGratuito(id);
        return ResponseEntity.ok(puede);
    }

    @PostMapping("/{id}/usar-pase-gratuito")
    public ResponseEntity<Void> usarPaseGratuito(@PathVariable Long id) {
        clienteService.usarPaseGratuito(id);
        return ResponseEntity.ok().build();
    }

    // Estadísticas
    @GetMapping("/top-por-asistencia")
    public ResponseEntity<Page<ClienteResponseDTO>> obtenerTopClientesPorAsistencia(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ClienteResponseDTO> clientes = clienteService.obtenerTopClientesPorAsistencia(page, size);
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/registrados-en-periodo")
    public ResponseEntity<List<ClienteResponseDTO>> obtenerClientesRegistradosEnPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        List<ClienteResponseDTO> clientes = clienteService.obtenerClientesRegistradosEnPeriodo(desde, hasta);
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/existe-email/{email}")
    public ResponseEntity<Boolean> existeClientePorEmail(@PathVariable String email) {
        boolean existe = clienteService.existeClientePorEmail(email);
        return ResponseEntity.ok(existe);
    }

    @GetMapping("/{id}/validar-activo")
    public ResponseEntity<Void> validarClienteActivo(@PathVariable Long id) {
        clienteService.validarClienteActivo(id);
        return ResponseEntity.ok().build();
    }
}