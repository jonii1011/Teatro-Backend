package teatro_reservas.backend.service;

import teatro_reservas.backend.dto.ClienteResponseDTO;
import teatro_reservas.backend.dto.ClienteResumenDTO;

import java.util.List;
import java.util.Map;

public interface FidelizacionService {

    // Consultas de fidelización
    int contarAsistenciasEnAnoActual(Long clienteId);
    int calcularPasesGratuitosPendientes(Long clienteId);
    List<ClienteResumenDTO> obtenerClientesElegiblesParaPase();

    // Estadísticas de fidelización
    Map<String, Long> obtenerEstadisticasFidelizacion();
    List<ClienteResponseDTO> obtenerRankingClientesFrecuentes();

    // Estadísticas detalladas
    Map<String, Object> obtenerEstadisticasDetalladasCliente(Long clienteId);

    // Métodos administrativos
    void actualizarSistemaFidelizacion();
    Map<String, Object> validarIntegridadSistema();
}
