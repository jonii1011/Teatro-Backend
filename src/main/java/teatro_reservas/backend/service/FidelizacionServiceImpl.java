package teatro_reservas.backend.service;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import teatro_reservas.backend.dto.ClienteResponseDTO;
import teatro_reservas.backend.dto.ClienteResumenDTO;
import teatro_reservas.backend.entity.Cliente;
import teatro_reservas.backend.entity.Reserva;
import teatro_reservas.backend.exception.ResourceNotFoundException;
import teatro_reservas.backend.repository.ClienteRepository;
import teatro_reservas.backend.repository.ReservaRepository;

import java.awt.print.Pageable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class FidelizacionServiceImpl implements FidelizacionService {

    private final ClienteRepository clienteRepository;
    private final ReservaRepository reservaRepository;
    private final ModelMapper modelMapper = new ModelMapper();

    public FidelizacionServiceImpl(ClienteRepository clienteRepository,
                                   ReservaRepository reservaRepository) {
        this.clienteRepository = clienteRepository;
        this.reservaRepository = reservaRepository;
        configurarModelMapper();
    }

    private void configurarModelMapper() {
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);

        modelMapper.createTypeMap(Cliente.class, ClienteResponseDTO.class)
                .addMapping(src -> src.esClienteFrecuente(), ClienteResponseDTO::setEsClienteFrecuente)
                .addMapping(src -> src.getReservasActivas(), ClienteResponseDTO::setReservasActivas);
    }

    // Procesamiento de fidelización
    @Override
    public void procesarAsistenciaParaFidelizacion(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", clienteId));

        // Incrementar eventos asistidos
        cliente.procesarAsistenciaEvento();

        // Verificar si merece pase gratuito
        if (cumpleRequisitosParaPaseGratuito(clienteId)) {
            otorgarPaseGratuito(clienteId);
        }

        clienteRepository.save(cliente);
    }

    @Override
    public boolean cumpleRequisitosParaPaseGratuito(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", clienteId));

        // Cada 5 eventos asistidos merece un pase gratuito
        return cliente.getEventosAsistidos() > 0 && cliente.getEventosAsistidos() % 5 == 0;
    }

    @Override
    public void otorgarPaseGratuito(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", clienteId));

        cliente.setPasesGratuitos(cliente.getPasesGratuitos() + 1);
        clienteRepository.save(cliente);
    }

    // Consultas de fidelización
    @Override
    public int contarAsistenciasEnAnoActual(Long clienteId) {
        LocalDateTime inicioAno = LocalDateTime.of(LocalDateTime.now().getYear(), 1, 1, 0, 0);
        List<Reserva> asistencias = reservaRepository.findAsistenciasPorClienteEnAno(clienteId, inicioAno);
        return asistencias.size();
    }

    @Override
    public int calcularPasesGratuitosPendientes(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", clienteId));

        return cliente.getPasesGratuitos();
    }

    @Override
    public List<ClienteResumenDTO> obtenerClientesElegiblesParaPase() {
        // Clientes que tienen múltiplos de 5 eventos asistidos pero no han recibido todos sus pases
        List<Cliente> todosClientes = clienteRepository.findAll();

        List<Cliente> elegibles = todosClientes.stream()
                .filter(cliente -> {
                    int eventosAsistidos = cliente.getEventosAsistidos();
                    int pasesQueDebeTener = eventosAsistidos / 5;
                    return cliente.getPasesGratuitos() < pasesQueDebeTener;
                })
                .collect(Collectors.toList());

        return mapToClienteResumenDTOList(elegibles);
    }

    // Estadísticas de fidelización
    @Override
    public Map<String, Long> obtenerEstadisticasFidelizacion() {
        Map<String, Long> estadisticas = new HashMap<>();

        // Total de clientes
        long totalClientes = clienteRepository.count();
        estadisticas.put("totalClientes", totalClientes);

        // Clientes frecuentes (5+ eventos)
        List<Cliente> clientesFrecuentes = clienteRepository.findClientesFrecuentes();
        estadisticas.put("clientesFrecuentes", (long) clientesFrecuentes.size());

        // Clientes con pases gratuitos disponibles
        List<Cliente> clientesConPases = clienteRepository.findClientesConPasesGratuitos();
        estadisticas.put("clientesConPasesDisponibles", (long) clientesConPases.size());

        // Total de pases gratuitos otorgados
        long totalPasesOtorgados = clienteRepository.findAll().stream()
                .mapToLong(Cliente::getPasesGratuitos)
                .sum();
        estadisticas.put("totalPasesOtorgados", totalPasesOtorgados);

        // Total de pases gratuitos usados (calculado)
        long totalPasesUsados = calcularTotalPasesUsados();
        estadisticas.put("totalPasesUsados", totalPasesUsados);

        // Promedio de eventos por cliente
        double promedioEventos = clienteRepository.findAll().stream()
                .mapToInt(Cliente::getEventosAsistidos)
                .average()
                .orElse(0.0);
        estadisticas.put("promedioEventosPorCliente", Math.round(promedioEventos));

        // Porcentaje de fidelización
        double porcentajeFidelizacion = totalClientes > 0 ?
                (double) clientesFrecuentes.size() / totalClientes * 100 : 0.0;
        estadisticas.put("porcentajeFidelizacion", Math.round(porcentajeFidelizacion));

        return estadisticas;
    }

    @Override
    public List<ClienteResponseDTO> obtenerRankingClientesFrecuentes() {
        // Obtener los top 10 clientes por eventos asistidos
        Pageable topTen = (Pageable) PageRequest.of(0, 10);
        Page<Cliente> topClientes = clienteRepository.findTopClientesByEventosAsistidos(topTen);

        return topClientes.getContent().stream()
                .map(cliente -> modelMapper.map(cliente, ClienteResponseDTO.class))
                .collect(Collectors.toList());
    }

    // Métodos adicionales útiles
    public void actualizarSistemaFidelizacion() {
        // Método para sincronizar el sistema de fidelización
        List<Cliente> todosClientes = clienteRepository.findAll();

        for (Cliente cliente : todosClientes) {
            int eventosAsistidos = cliente.getEventosAsistidos();
            int pasesQueDebeTener = eventosAsistidos / 5;

            if (cliente.getPasesGratuitos() < pasesQueDebeTener) {
                int pasesParaOtorgar = pasesQueDebeTener - cliente.getPasesGratuitos();
                cliente.setPasesGratuitos(cliente.getPasesGratuitos() + pasesParaOtorgar);
                clienteRepository.save(cliente);
            }
        }
    }

    public Map<String, Object> obtenerEstadisticasDetalladasCliente(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", clienteId));

        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("eventosAsistidos", cliente.getEventosAsistidos());
        estadisticas.put("pasesGratuitosDisponibles", cliente.getPasesGratuitos());
        estadisticas.put("esClienteFrecuente", cliente.esClienteFrecuente());

        // Calcular eventos hasta próximo pase gratuito
        int eventosParaProximoPase = 5 - (cliente.getEventosAsistidos() % 5);
        if (eventosParaProximoPase == 5) eventosParaProximoPase = 0; // Ya cumplió el ciclo
        estadisticas.put("eventosParaProximoPase", eventosParaProximoPase);

        // Asistencias en el año actual
        int asistenciasEsteAno = contarAsistenciasEnAnoActual(clienteId);
        estadisticas.put("asistenciasEsteAno", asistenciasEsteAno);

        // Fecha de registro
        estadisticas.put("fechaRegistro", cliente.getFechaRegistro());

        // Calcular meses como cliente
        long mesesComoCliente = ChronoUnit.MONTHS.between(
                cliente.getFechaRegistro().toLocalDate(),
                LocalDate.now()
        );
        estadisticas.put("mesesComoCliente", mesesComoCliente);

        return estadisticas;
    }

    private long calcularTotalPasesUsados() {
        // Contar reservas que fueron hechas con pase gratuito
        return reservaRepository.findAll().stream()
                .filter(Reserva::getEsPaseGratuito)
                .count();
    }

    private List<ClienteResumenDTO> mapToClienteResumenDTOList(List<Cliente> clientes) {
        return clientes.stream()
                .map(cliente -> modelMapper.map(cliente, ClienteResumenDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public void procesarFidelizacionEnLote() {
        List<Cliente> clientesElegibles = obtenerClientesElegiblesParaPase().stream()
                .map(dto -> clienteRepository.findById(dto.getId()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        for (Cliente cliente : clientesElegibles) {
            procesarAsistenciaParaFidelizacion(cliente.getId());
        }
    }

    // Validar integridad del sistema de fidelización
    public Map<String, Object> validarIntegridadSistema() {
        Map<String, Object> reporte = new HashMap<>();
        List<String> inconsistencias = new ArrayList<>();

        List<Cliente> todosClientes = clienteRepository.findAll();

        for (Cliente cliente : todosClientes) {
            int eventosAsistidos = cliente.getEventosAsistidos();
            int pasesActuales = cliente.getPasesGratuitos();
            int pasesQueDebeTener = eventosAsistidos / 5;

            // Calcular pases usados por el cliente
            long pasesUsados = reservaRepository.findAll().stream()
                    .filter(r -> r.getCliente().getId().equals(cliente.getId()))
                    .filter(Reserva::getEsPaseGratuito)
                    .count();

            int pasesTotalesQueDebeHaber = pasesQueDebeTener + (int) pasesUsados;

            if (pasesActuales + pasesUsados < pasesQueDebeTener) {
                inconsistencias.add(String.format(
                        "Cliente %s (%s): Debe tener %d pases pero solo tiene %d disponibles + %d usados",
                        cliente.getNombre() + " " + cliente.getApellido(),
                        cliente.getId(),
                        pasesQueDebeTener,
                        pasesActuales,
                        pasesUsados
                ));
            }
        }

        reporte.put("inconsistenciasEncontradas", inconsistencias.size());
        reporte.put("detalleInconsistencias", inconsistencias);
        reporte.put("sistemaIntegro", inconsistencias.isEmpty());

        return reporte;
    }
}
