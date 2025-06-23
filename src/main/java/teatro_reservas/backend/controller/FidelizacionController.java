package teatro_reservas.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import teatro_reservas.backend.dto.ClienteResponseDTO;
import teatro_reservas.backend.dto.ClienteResumenDTO;
import teatro_reservas.backend.service.FidelizacionService;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/fidelizacion")
@Validated
@Tag(name = "Fidelización", description = "Estadísticas y reportes del sistema de fidelización")
public class FidelizacionController {

    private final FidelizacionService fidelizacionService;

    public FidelizacionController(FidelizacionService fidelizacionService) {
        this.fidelizacionService = fidelizacionService;
    }

    // Consultas de fidelización
    @GetMapping("/asistencias-ano-actual/{clienteId}")
    @Operation(
            summary = "Asistencias en año actual",
            description = "Cuenta las asistencias del cliente en el año actual"
    )
    @ApiResponse(responseCode = "200", description = "Conteo de asistencias obtenido")
    public ResponseEntity<Integer> contarAsistenciasEnAnoActual(
            @PathVariable @Parameter(description = "ID del cliente") Long clienteId) {
        int asistencias = fidelizacionService.contarAsistenciasEnAnoActual(clienteId);
        return ResponseEntity.ok(asistencias);
    }

    @GetMapping("/pases-pendientes/{clienteId}")
    @Operation(
            summary = "Pases gratuitos disponibles",
            description = "Obtiene la cantidad de pases gratuitos disponibles que tiene el cliente"
    )
    @ApiResponse(responseCode = "200", description = "Cantidad de pases disponibles obtenida")
    public ResponseEntity<Integer> calcularPasesGratuitosPendientes(
            @PathVariable @Parameter(description = "ID del cliente") Long clienteId) {
        int pases = fidelizacionService.calcularPasesGratuitosPendientes(clienteId);
        return ResponseEntity.ok(pases);
    }

    @GetMapping("/clientes-elegibles")
    @Operation(
            summary = "Clientes elegibles para pase gratuito",
            description = "Lista los clientes que deberían tener pases gratuitos pero hay inconsistencias en el sistema"
    )
    @ApiResponse(responseCode = "200", description = "Lista de clientes elegibles obtenida")
    public ResponseEntity<List<ClienteResumenDTO>> obtenerClientesElegiblesParaPase() {
        List<ClienteResumenDTO> clientes = fidelizacionService.obtenerClientesElegiblesParaPase();
        return ResponseEntity.ok(clientes);
    }

    // Estadísticas de fidelización
    @GetMapping("/estadisticas")
    @Operation(
            summary = "Estadísticas generales de fidelización",
            description = """
                Obtiene estadísticas completas del programa de fidelización incluyendo:
                - Total de clientes
                - Clientes frecuentes (5+ eventos)
                - Clientes con pases disponibles
                - Total de pases otorgados y usados
                - Promedio de eventos por cliente
                - Porcentaje de fidelización
                """
    )
    @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas exitosamente")
    public ResponseEntity<Map<String, Long>> obtenerEstadisticasFidelizacion() {
        Map<String, Long> estadisticas = fidelizacionService.obtenerEstadisticasFidelizacion();
        return ResponseEntity.ok(estadisticas);
    }

    @GetMapping("/ranking-clientes-frecuentes")
    @Operation(
            summary = "Ranking de clientes frecuentes",
            description = "Obtiene los top 10 clientes con mayor número de eventos asistidos"
    )
    @ApiResponse(responseCode = "200", description = "Ranking obtenido exitosamente")
    public ResponseEntity<List<ClienteResponseDTO>> obtenerRankingClientesFrecuentes() {
        List<ClienteResponseDTO> ranking = fidelizacionService.obtenerRankingClientesFrecuentes();
        return ResponseEntity.ok(ranking);
    }

    @GetMapping("/estadisticas-cliente/{clienteId}")
    @Operation(
            summary = "Estadísticas detalladas del cliente",
            description = """
                Obtiene estadísticas completas de un cliente específico incluyendo:
                - Eventos asistidos y pases disponibles
                - Eventos para próximo pase gratuito
                - Asistencias en el año actual
                - Tiempo como cliente
                """
    )
    @ApiResponse(responseCode = "200", description = "Estadísticas del cliente obtenidas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasDetalladasCliente(
            @PathVariable @Parameter(description = "ID del cliente") Long clienteId) {
        Map<String, Object> estadisticas = fidelizacionService.obtenerEstadisticasDetalladasCliente(clienteId);
        return ResponseEntity.ok(estadisticas);
    }

    // Reportes avanzados
    @GetMapping("/reporte-mensual")
    @Operation(
            summary = "Reporte mensual de fidelización",
            description = "Genera un reporte mensual con estadísticas del programa de fidelización. Si no se especifican parámetros, usa el mes y año actual."
    )
    @ApiResponse(responseCode = "200", description = "Reporte mensual generado exitosamente")
    public ResponseEntity<Map<String, Object>> obtenerReporteMensual(
            @RequestParam(required = false) @Parameter(description = "Año del reporte", example = "2024") Integer ano,
            @RequestParam(required = false) @Parameter(description = "Mes del reporte (1-12)", example = "12") Integer mes) {

        // Si no se especifica, usar mes y año actual
        LocalDateTime ahora = LocalDateTime.now();
        int anoActual = ano != null ? ano : ahora.getYear();
        int mesActual = mes != null ? mes : ahora.getMonthValue();

        Map<String, Object> reporte = new HashMap<>();
        reporte.put("ano", anoActual);
        reporte.put("mes", mesActual);

        // Estadísticas del período
        Map<String, Long> estadisticas = fidelizacionService.obtenerEstadisticasFidelizacion();
        reporte.put("estadisticas", estadisticas);

        // Ranking de clientes frecuentes
        List<ClienteResponseDTO> ranking = fidelizacionService.obtenerRankingClientesFrecuentes();
        reporte.put("totalClientesFrecuentes", ranking.size());
        reporte.put("topClientesFrecuentes", ranking);

        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/exportar")
    @Operation(
            summary = "Exportar datos de fidelización",
            description = """
                Prepara una exportación con todos los datos del programa de fidelización incluyendo:
                - Estadísticas generales
                - Ranking de clientes frecuentes
                - Lista de clientes elegibles para pases
                
                Nota: En una implementación real, esto generaría un archivo Excel o CSV para descarga.
                """
    )
    @ApiResponse(responseCode = "200", description = "Datos de exportación preparados exitosamente")
    public ResponseEntity<Map<String, Object>> exportarDatosFidelizacion() {
        Map<String, Object> exportacion = new HashMap<>();

        // En una implementación real, esto generaría un archivo Excel o CSV
        exportacion.put("mensaje", "Exportación preparada");
        exportacion.put("estadisticas", fidelizacionService.obtenerEstadisticasFidelizacion());
        exportacion.put("ranking", fidelizacionService.obtenerRankingClientesFrecuentes());
        exportacion.put("elegibles", fidelizacionService.obtenerClientesElegiblesParaPase());
        exportacion.put("fechaExportacion", LocalDateTime.now());

        return ResponseEntity.ok(exportacion);
    }

    // Endpoints administrativos
    @PostMapping("/actualizar-sistema")
    @Operation(
            summary = "Actualizar sistema de fidelización",
            description = "Sincroniza y corrige inconsistencias en el sistema de fidelización. Útil para correcciones masivas."
    )
    @ApiResponse(responseCode = "200", description = "Sistema actualizado exitosamente")
    public ResponseEntity<Map<String, Object>> actualizarSistemaFidelizacion() {
        fidelizacionService.actualizarSistemaFidelizacion();

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("mensaje", "Sistema de fidelización actualizado correctamente");
        resultado.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/validar-integridad")
    @Operation(
            summary = "Validar integridad del sistema",
            description = "Verifica que no haya inconsistencias en el sistema de fidelización y genera un reporte de auditoría"
    )
    @ApiResponse(responseCode = "200", description = "Validación completada")
    public ResponseEntity<Map<String, Object>> validarIntegridadSistema() {
        Map<String, Object> reporte = fidelizacionService.validarIntegridadSistema();
        return ResponseEntity.ok(reporte);
    }
}