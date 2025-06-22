package teatro_reservas.backend.service;

import org.springframework.data.domain.Page;
import teatro_reservas.backend.dto.ClienteRequestDTO;
import teatro_reservas.backend.dto.ClienteResponseDTO;
import teatro_reservas.backend.dto.ClienteResumenDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ClienteService {

    ClienteResponseDTO crearCliente(ClienteRequestDTO clienteDTO);
    ClienteResponseDTO obtenerClientePorId(Long id);
    ClienteResponseDTO actualizarCliente(Long id, ClienteRequestDTO clienteDTO);
    void eliminarCliente(Long id); // soft delete

    List<ClienteResponseDTO> obtenerTodosLosClientes();
    List<ClienteResumenDTO> obtenerClientesActivos();
    Optional<ClienteResponseDTO> obtenerClientePorEmail(String email);
    List<ClienteResumenDTO> buscarClientesPorNombre(String termino);

    List<ClienteResumenDTO> obtenerClientesFrecuentes();
    List<ClienteResumenDTO> obtenerClientesConPasesGratuitos();
    void procesarAsistenciaEvento(Long clienteId);
    boolean puedeUsarPaseGratuito(Long clienteId);
    void usarPaseGratuito(Long clienteId);

    Page<ClienteResponseDTO> obtenerTopClientesPorAsistencia(int page, int size);
    List<ClienteResponseDTO> obtenerClientesRegistradosEnPeriodo(LocalDateTime desde, LocalDateTime hasta);

    boolean existeClientePorEmail(String email);
    void validarClienteActivo(Long clienteId);
}
