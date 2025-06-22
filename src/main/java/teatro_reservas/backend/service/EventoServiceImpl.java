package teatro_reservas.backend.service;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Service;
import teatro_reservas.backend.dto.*;
import teatro_reservas.backend.entity.Evento;
import teatro_reservas.backend.entity.enums.TipoEntrada;
import teatro_reservas.backend.entity.enums.TipoEvento;
import teatro_reservas.backend.exception.BusinessException;
import teatro_reservas.backend.exception.ResourceNotFoundException;
import teatro_reservas.backend.repository.EventoRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventoServiceImpl implements EventoService {

    private final EventoRepository eventoRepository;
    private final ModelMapper modelMapper = new ModelMapper();

    public EventoServiceImpl(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
        configurarModelMapper();
    }

    private void configurarModelMapper() {
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);

        modelMapper.createTypeMap(Evento.class, EventoResponseDTO.class)
                .addMapping(src -> src.estaVigente(), EventoResponseDTO::setEstaVigente)
                .addMapping(src -> src.getReservasActivas(), EventoResponseDTO::setTotalReservasActivas);
    }

    // CRUD básico
    @Override
    public EventoResponseDTO crearEvento(EventoRequestDTO eventoDTO) {
        // Validar compatibilidad de tipos de entrada con tipo de evento
        validarTiposEntradaCompatibles(eventoDTO.getTipoEvento(), eventoDTO.getConfiguracionEntradas().keySet());

        Evento evento = mapToEventoEntity(eventoDTO);
        Evento eventoGuardado = eventoRepository.save(evento);
        return mapToEventoResponseDTO(eventoGuardado);
    }

    @Override
    public EventoResponseDTO obtenerEventoPorId(Long id) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", "id", id));
        return mapToEventoResponseDTO(evento);
    }

    @Override
    public EventoResponseDTO actualizarEvento(Long id, EventoRequestDTO eventoDTO) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", "id", id));

        // Validar que no se actualice un evento que ya pasó
        if (evento.getFechaHora().isBefore(LocalDateTime.now())) {
            throw new BusinessException("No se puede actualizar un evento que ya pasó");
        }

        // Validar compatibilidad
        validarTiposEntradaCompatibles(eventoDTO.getTipoEvento(), eventoDTO.getConfiguracionEntradas().keySet());

        // Actualizar campos
        evento.setNombre(eventoDTO.getNombre());
        evento.setDescripcion(eventoDTO.getDescripcion());
        evento.setFechaHora(eventoDTO.getFechaHora());
        evento.setTipoEvento(eventoDTO.getTipoEvento());
        evento.setCapacidadTotal(eventoDTO.getCapacidadTotal());

        // Actualizar configuraciones de entrada
        actualizarConfiguracionesEntrada(evento, eventoDTO.getConfiguracionEntradas());

        Evento eventoActualizado = eventoRepository.save(evento);
        return mapToEventoResponseDTO(eventoActualizado);
    }

    @Override
    public void eliminarEvento(Long id) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", "id", id));

        // Verificar que no tenga reservas confirmadas
        if (evento.getReservasActivas() > 0) {
            throw new BusinessException("No se puede eliminar un evento con reservas confirmadas");
        }

        // Soft delete
        evento.setActivo(false);
        eventoRepository.save(evento);
    }

    // Consultas básicas
    @Override
    public List<EventoResponseDTO> obtenerTodosLosEventos() {
        List<Evento> eventos = eventoRepository.findAll();
        return mapToEventoResponseDTOList(eventos);
    }

    @Override
    public List<EventoResumenDTO> obtenerEventosVigentes() {
        List<Evento> eventos = eventoRepository.findEventosVigentes(LocalDateTime.now());
        return mapToEventoResumenDTOList(eventos);
    }

    @Override
    public List<EventoResumenDTO> obtenerEventosPorTipo(TipoEvento tipoEvento) {
        List<Evento> eventos = eventoRepository.findByTipoEventoAndActivoTrue(tipoEvento);
        return mapToEventoResumenDTOList(eventos);
    }

    @Override
    public List<EventoResumenDTO> buscarEventosPorNombre(String nombre) {
        List<Evento> eventos = eventoRepository.findByNombreContainingIgnoreCase(nombre);
        return mapToEventoResumenDTOList(eventos);
    }

    // Disponibilidad
    @Override
    public List<EventoResumenDTO> obtenerEventosConDisponibilidad() {
        List<Evento> eventos = eventoRepository.findEventosConDisponibilidad(LocalDateTime.now());
        return mapToEventoResumenDTOList(eventos);
    }

    @Override
    public boolean tieneDisponibilidad(Long eventoId, TipoEntrada tipoEntrada) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", "id", eventoId));
        return evento.tieneDisponibilidad(tipoEntrada);
    }

    @Override
    public long obtenerCapacidadDisponible(Long eventoId, TipoEntrada tipoEntrada) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", "id", eventoId));
        return evento.getCapacidadDisponible(tipoEntrada);
    }

    @Override
    public Map<TipoEntrada, Long> obtenerDisponibilidadPorTipo(Long eventoId) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", "id", eventoId));

        Map<TipoEntrada, Long> disponibilidad = new HashMap<>();
        for (TipoEntrada tipo : evento.getPrecios().keySet()) {
            disponibilidad.put(tipo, evento.getCapacidadDisponible(tipo));
        }
        return disponibilidad;
    }

    @Override
    public List<EventoResumenDTO> obtenerEventosProximos(int dias) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime limite = ahora.plusDays(dias);
        List<Evento> eventos = eventoRepository.findEventosProximos(ahora, limite);
        return mapToEventoResumenDTOList(eventos);
    }

    @Override
    public List<EventoResumenDTO> obtenerEventosEnRango(LocalDateTime desde, LocalDateTime hasta) {
        List<Evento> eventos = eventoRepository.findEventosByFechaBetween(desde, hasta);
        return mapToEventoResumenDTOList(eventos);
    }

    // Precios
    @Override
    public BigDecimal obtenerPrecioEntrada(Long eventoId, TipoEntrada tipoEntrada) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", "id", eventoId));

        BigDecimal precio = evento.getPrecios().get(tipoEntrada);
        if (precio == null) {
            throw new BusinessException("Tipo de entrada no disponible para este evento");
        }
        return precio;
    }

    @Override
    public Map<TipoEntrada, BigDecimal> obtenerTodosLosPrecios(Long eventoId) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", "id", eventoId));
        return new HashMap<>(evento.getPrecios());
    }

    // Validaciones
    @Override
    public void validarEventoVigente(Long eventoId) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", "id", eventoId));

        if (!evento.estaVigente()) {
            throw new BusinessException("El evento no está vigente");
        }
    }

    @Override
    public void validarCompatibilidadTipoEntrada(Long eventoId, TipoEntrada tipoEntrada) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", "id", eventoId));

        if (!evento.getPrecios().containsKey(tipoEntrada)) {
            throw new BusinessException("Tipo de entrada no válido para este evento");
        }
    }

    @Override
    public void validarDisponibilidad(Long eventoId, TipoEntrada tipoEntrada) {
        if (!tieneDisponibilidad(eventoId, tipoEntrada)) {
            throw new BusinessException("No hay disponibilidad para este tipo de entrada");
        }
    }

    @Override
    public List<EventoResumenDTO> obtenerEventosMasPopulares(LocalDateTime desde, LocalDateTime hasta) {
        List<Object[]> resultados = eventoRepository.findEventosMasPopulares(desde, hasta);
        return resultados.stream()
                .map(result -> {
                    Evento evento = (Evento) result[0];
                    return mapToEventoResumenDTO(evento);
                })
                .collect(Collectors.toList());
    }

    // Métodos helper privados
    private Evento mapToEventoEntity(EventoRequestDTO dto) {
        Evento evento = modelMapper.map(dto, Evento.class);

        // Mapear configuraciones manualmente
        Map<TipoEntrada, BigDecimal> precios = new HashMap<>();
        Map<TipoEntrada, Integer> capacidades = new HashMap<>();

        dto.getConfiguracionEntradas().forEach((tipo, config) -> {
            precios.put(tipo, config.getPrecio());
            capacidades.put(tipo, config.getCapacidad());
        });

        evento.setPrecios(precios);
        evento.setCapacidades(capacidades);

        return evento;
    }

    private EventoResponseDTO mapToEventoResponseDTO(Evento evento) {
        EventoResponseDTO dto = modelMapper.map(evento, EventoResponseDTO.class);

        // Calcular disponibilidad por tipo
        Map<TipoEntrada, Long> disponibilidad = new HashMap<>();
        for (TipoEntrada tipo : evento.getPrecios().keySet()) {
            disponibilidad.put(tipo, evento.getCapacidadDisponible(tipo));
        }
        dto.setDisponibilidadPorTipo(disponibilidad);

        // Establecer los tipos de entrada
        dto.setTiposEntrada(evento.getPrecios().keySet());

        return dto;
    }

    private EventoResumenDTO mapToEventoResumenDTO(Evento evento) {
        EventoResumenDTO dto = modelMapper.map(evento, EventoResumenDTO.class);

        // Calcular precio mínimo
        BigDecimal precioMinimo = evento.getPrecios().values().stream()
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        dto.setPrecioDesde(precioMinimo);

        // Capacidad disponible total
        long capacidadDisponible = evento.getCapacidadTotal() - evento.getReservasActivas();
        dto.setCapacidadDisponible(capacidadDisponible);

        return dto;
    }

    private List<EventoResponseDTO> mapToEventoResponseDTOList(List<Evento> eventos) {
        return eventos.stream()
                .map(this::mapToEventoResponseDTO)
                .collect(Collectors.toList());
    }

    private List<EventoResumenDTO> mapToEventoResumenDTOList(List<Evento> eventos) {
        return eventos.stream()
                .map(this::mapToEventoResumenDTO)
                .collect(Collectors.toList());
    }

    private void validarTiposEntradaCompatibles(TipoEvento tipoEvento, Set<TipoEntrada> tiposEntrada) {
        for (TipoEntrada tipo : tiposEntrada) {
            if (!esCompatible(tipoEvento, tipo)) {
                throw new BusinessException("Tipo de entrada " + tipo + " no es compatible con " + tipoEvento);
            }
        }
    }

    private boolean esCompatible(TipoEvento tipoEvento, TipoEntrada tipoEntrada) {
        return switch (tipoEvento) {
            case OBRA_TEATRO -> tipoEntrada == TipoEntrada.GENERAL || tipoEntrada == TipoEntrada.VIP;
            case RECITAL -> tipoEntrada == TipoEntrada.CAMPO || tipoEntrada == TipoEntrada.PLATEA || tipoEntrada == TipoEntrada.PALCO;
            case CHARLA_CONFERENCIA -> tipoEntrada == TipoEntrada.CON_MEET_GREET || tipoEntrada == TipoEntrada.SIN_MEET_GREET;
        };
    }

    private void actualizarConfiguracionesEntrada(Evento evento, Map<TipoEntrada, ConfiguracionEntradaDTO> configuraciones) {
        Map<TipoEntrada, BigDecimal> precios = new HashMap<>();
        Map<TipoEntrada, Integer> capacidades = new HashMap<>();

        configuraciones.forEach((tipo, config) -> {
            precios.put(tipo, config.getPrecio());
            capacidades.put(tipo, config.getCapacidad());
        });

        evento.setPrecios(precios);
        evento.setCapacidades(capacidades);
    }
}
