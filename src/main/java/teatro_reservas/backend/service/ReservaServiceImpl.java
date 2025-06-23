package teatro_reservas.backend.service;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Service;
import teatro_reservas.backend.dto.*;
import teatro_reservas.backend.entity.Cliente;
import teatro_reservas.backend.entity.Evento;
import teatro_reservas.backend.entity.Reserva;
import teatro_reservas.backend.entity.enums.EstadoReserva;
import teatro_reservas.backend.entity.enums.TipoEntrada;
import teatro_reservas.backend.exception.BusinessException;
import teatro_reservas.backend.exception.ResourceNotFoundException;
import teatro_reservas.backend.repository.ClienteRepository;
import teatro_reservas.backend.repository.EventoRepository;
import teatro_reservas.backend.repository.ReservaRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository reservaRepository;
    private final ClienteRepository clienteRepository;
    private final EventoRepository eventoRepository;
    private final ModelMapper modelMapper = new ModelMapper();

    public ReservaServiceImpl(ReservaRepository reservaRepository,
                              ClienteRepository clienteRepository,
                              EventoRepository eventoRepository) {
        this.reservaRepository = reservaRepository;
        this.clienteRepository = clienteRepository;
        this.eventoRepository = eventoRepository;
        configurarModelMapper();
    }

    private void configurarModelMapper() {
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);

        modelMapper.createTypeMap(Reserva.class, ReservaResponseDTO.class)
                .addMapping(src -> src.puedeSerCancelada(), ReservaResponseDTO::setPuedeSerCancelada)
                .addMapping(src -> src.estaVigente(), ReservaResponseDTO::setEstaVigente);
    }

    @Override
    public ReservaResponseDTO crearReserva(ReservaRequestDTO reservaDTO) {
        // Validaciones previas
        Cliente cliente = clienteRepository.findById(reservaDTO.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", reservaDTO.getClienteId()));

        Evento evento = eventoRepository.findById(reservaDTO.getEventoId())
                .orElseThrow(() -> new ResourceNotFoundException("Evento", "id", reservaDTO.getEventoId()));

        // Validar que el cliente esté activo
        if (!cliente.getActivo()) {
            throw new BusinessException("El cliente está inactivo");
        }

        // Validar que el evento esté vigente
        if (!evento.estaVigente()) {
            throw new BusinessException("El evento no está vigente");
        }

        // Validar compatibilidad tipo entrada - tipo evento
        if (!evento.getPrecios().containsKey(reservaDTO.getTipoEntrada())) {
            throw new BusinessException("Tipo de entrada no válido para este evento");
        }

        // Validar disponibilidad
        if (!evento.tieneDisponibilidad(reservaDTO.getTipoEntrada())) {
            throw new BusinessException("No hay disponibilidad para este tipo de entrada");
        }

        // Crear reserva
        Reserva reserva = new Reserva();
        reserva.setCliente(cliente);
        reserva.setEvento(evento);
        reserva.setTipoEntrada(reservaDTO.getTipoEntrada());

        // Manejar pase gratuito
        if (reservaDTO.getUsarPaseGratuito()) {
            // VALIDAR PRIMERO QUE TENGA PASES
            if (!cliente.tienePasesGratuitos()) {
                throw new BusinessException("El cliente no tiene pases gratuitos disponibles");
            }

            // Si llega aquí, sí tiene pases
            reserva.setEsPaseGratuito(true);
            reserva.setEstado(EstadoReserva.CONFIRMADA);
            reserva.setFechaConfirmacion(LocalDateTime.now());
            reserva.setPrecioPagado(BigDecimal.ZERO);

            // Usar el pase gratuito
            cliente.usarPaseGratuito();
            clienteRepository.save(cliente);
        } else {
            // Reserva normal (sin pase gratuito)
            reserva.setEsPaseGratuito(false);
            reserva.setEstado(EstadoReserva.PENDIENTE);
        }

        Reserva reservaGuardada = reservaRepository.save(reserva);
        return mapToReservaResponseDTO(reservaGuardada);
    }

    @Override
    public ReservaResponseDTO obtenerReservaPorId(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", "id", id));
        return mapToReservaResponseDTO(reserva);
    }

    @Override
    public void eliminarReserva(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", "id", id));

        if (!reserva.puedeSerCancelada()) {
            throw new BusinessException("La reserva no puede ser eliminada en su estado actual");
        }

        reservaRepository.delete(reserva);
    }

    // Búsquedas
    @Override
    public List<ReservaResponseDTO> obtenerTodasLasReservas() {
        List<Reserva> reservas = reservaRepository.findAll();
        return mapToReservaResponseDTOList(reservas);
    }

    @Override
    public List<ReservaResumenDTO> obtenerReservasPorCliente(Long clienteId) {
        List<Reserva> reservas = reservaRepository.findByClienteIdOrderByFechaReservaDesc(clienteId);
        return mapToReservaResumenDTOList(reservas);
    }

    @Override
    public List<ReservaResumenDTO> obtenerReservasPorEvento(Long eventoId) {
        List<Reserva> reservas = reservaRepository.findByEventoIdOrderByFechaReservaDesc(eventoId);
        return mapToReservaResumenDTOList(reservas);
    }

    @Override
    public Optional<ReservaResponseDTO> obtenerReservaPorCodigo(String codigoReserva) {
        Optional<Reserva> reserva = reservaRepository.findByCodigoReserva(codigoReserva);
        return reserva.map(this::mapToReservaResponseDTO);
    }

    @Override
    public List<ReservaResponseDTO> obtenerReservasPorEstado(EstadoReserva estado) {
        List<Reserva> reservas = reservaRepository.findByEstado(estado);
        return mapToReservaResponseDTOList(reservas);
    }

    // Gestión de estados
    @Override
    public ReservaResponseDTO confirmarReserva(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", "id", reservaId));

        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new BusinessException("Solo se pueden confirmar reservas pendientes");
        }

        if (!reserva.estaVigente()) {
            throw new BusinessException("La reserva no está vigente");
        }

        // CALCULAR PRECIO AUTOMÁTICAMENTE
        BigDecimal precioCorrect = reserva.getEvento().getPrecios().get(reserva.getTipoEntrada());
        if (precioCorrect == null) {
            throw new BusinessException("No se puede determinar el precio para este tipo de entrada");
        }

        // USAR EL PRECIO CORRECTO, NO EL PARÁMETRO
        reserva.confirmar(precioCorrect);

        // Procesar fidelización
        Cliente cliente = reserva.getCliente();
        cliente.procesarAsistenciaEvento();
        clienteRepository.save(cliente);

        Reserva reservaActualizada = reservaRepository.save(reserva);
        return mapToReservaResponseDTO(reservaActualizada);
    }

    @Override
    public ReservaResponseDTO cancelarReserva(Long reservaId, String motivo) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", "id", reservaId));

        if (!reserva.puedeSerCancelada()) {
            throw new BusinessException("La reserva no puede ser cancelada en su estado actual");
        }

        // Si era un pase gratuito, devolver el pase al cliente
        if (reserva.getEsPaseGratuito()) {
            Cliente cliente = reserva.getCliente();
            cliente.setPasesGratuitos(cliente.getPasesGratuitos() + 1);
            clienteRepository.save(cliente);
        }

        reserva.cancelar(motivo);
        Reserva reservaActualizada = reservaRepository.save(reserva);
        return mapToReservaResponseDTO(reservaActualizada);
    }

    // Pases gratuitos
    @Override
    public ReservaResponseDTO crearReservaConPaseGratuito(Long clienteId, Long eventoId, TipoEntrada tipoEntrada) {
        ReservaRequestDTO reservaDTO = new ReservaRequestDTO();
        reservaDTO.setClienteId(clienteId);
        reservaDTO.setEventoId(eventoId);
        reservaDTO.setTipoEntrada(tipoEntrada);
        reservaDTO.setUsarPaseGratuito(true);

        return crearReserva(reservaDTO);
    }

    // Consultas específicas
    @Override
    public List<ReservaResponseDTO> obtenerReservasConfirmadas(Long clienteId) {
        List<Reserva> reservas = reservaRepository.findReservasConfirmadasByCliente(clienteId);
        return mapToReservaResponseDTOList(reservas);
    }

    @Override
    public List<ReservaResponseDTO> obtenerReservasPendientesVencidas(int horasVencimiento) {
        LocalDateTime limite = LocalDateTime.now().minusHours(horasVencimiento);
        List<Reserva> reservas = reservaRepository.findReservasPendientesVencidas(limite);
        return mapToReservaResponseDTOList(reservas);
    }

    @Override
    public List<ReservaResponseDTO> obtenerReservasQueExpiranPronto(int horasAntes) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime limite = ahora.plusHours(horasAntes);
        List<Reserva> reservas = reservaRepository.findReservasQueExpiranPronto(ahora, limite);
        return mapToReservaResponseDTOList(reservas);
    }

    // Validaciones
    @Override
    public void validarReservaCancelable(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", "id", reservaId));

        if (!reserva.puedeSerCancelada()) {
            throw new BusinessException("La reserva no puede ser cancelada");
        }
    }

    @Override
    public void validarReservaConfirmable(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", "id", reservaId));

        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new BusinessException("Solo se pueden confirmar reservas pendientes");
        }
    }

    @Override
    public boolean puedeCrearReserva(Long clienteId, Long eventoId, TipoEntrada tipoEntrada) {
        try {
            Cliente cliente = clienteRepository.findById(clienteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", clienteId));

            Evento evento = eventoRepository.findById(eventoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Evento", "id", eventoId));

            return cliente.getActivo() &&
                    evento.estaVigente() &&
                    evento.getPrecios().containsKey(tipoEntrada) &&
                    evento.tieneDisponibilidad(tipoEntrada);
        } catch (Exception e) {
            return false;
        }
    }

    // Estadísticas y reportes
    @Override
    public BigDecimal calcularIngresosPorEvento(Long eventoId) {
        BigDecimal ingresos = reservaRepository.calcularIngresosPorEvento(eventoId);
        return ingresos != null ? ingresos : BigDecimal.ZERO;
    }

    @Override
    public long contarReservasConfirmadasPorEventoYTipo(Long eventoId, TipoEntrada tipoEntrada) {
        return reservaRepository.countReservasConfirmadasByEventoAndTipo(eventoId, tipoEntrada);
    }

    @Override
    public List<Object[]> obtenerEstadisticasPorFecha(LocalDateTime desde, LocalDateTime hasta) {
        return reservaRepository.getEstadisticasPorFecha(desde, hasta);
    }


    // Métodos helper privados
    private ReservaResponseDTO mapToReservaResponseDTO(Reserva reserva) {
        ReservaResponseDTO dto = modelMapper.map(reserva, ReservaResponseDTO.class);

        // Mapear entidades relacionadas
        dto.setCliente(modelMapper.map(reserva.getCliente(), ClienteResumenDTO.class));
        dto.setEvento(mapToEventoResumenDTO(reserva.getEvento()));

        return dto;
    }

    private EventoResumenDTO mapToEventoResumenDTO(Evento evento) {
        EventoResumenDTO dto = modelMapper.map(evento, EventoResumenDTO.class);

        // Calcular precio mínimo
        BigDecimal precioMinimo = evento.getPrecios().values().stream()
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        dto.setPrecioDesde(precioMinimo);

        return dto;
    }

    private ReservaResumenDTO mapToReservaResumenDTO(Reserva reserva) {
        ReservaResumenDTO dto = modelMapper.map(reserva, ReservaResumenDTO.class);
        dto.setNombreEvento(reserva.getEvento().getNombre());
        dto.setFechaEvento(reserva.getEvento().getFechaHora());
        return dto;
    }

    private List<ReservaResponseDTO> mapToReservaResponseDTOList(List<Reserva> reservas) {
        return reservas.stream()
                .map(this::mapToReservaResponseDTO)
                .collect(Collectors.toList());
    }

    private List<ReservaResumenDTO> mapToReservaResumenDTOList(List<Reserva> reservas) {
        return reservas.stream()
                .map(this::mapToReservaResumenDTO)
                .collect(Collectors.toList());
    }
}
