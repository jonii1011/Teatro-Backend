package teatro_reservas.backend.service;

import teatro_reservas.backend.dto.ClienteResponseDTO;
import teatro_reservas.backend.dto.ClienteResumenDTO;

import java.util.List;
import java.util.Map;

public interface FidelizacionService {

    // Procesamiento de fidelización
    void procesarAsistenciaParaFidelizacion(Long clienteId);
    boolean cumpleRequisitosParaPaseGratuito(Long clienteId);
    void otorgarPaseGratuito(Long clienteId);

    // Consultas de fidelización
    int contarAsistenciasEnAnoActual(Long clienteId);
    int calcularPasesGratuitosPendientes(Long clienteId);
    List<ClienteResumenDTO> obtenerClientesElegiblesParaPase();

    // Estadísticas de fidelización
    Map<String, Long> obtenerEstadisticasFidelizacion();
    List<ClienteResponseDTO> obtenerRankingClientesFrecuentes();
}
