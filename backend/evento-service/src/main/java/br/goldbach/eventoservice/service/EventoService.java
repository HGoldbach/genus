package br.goldbach.eventoservice.service;

import br.goldbach.eventoservice.dto.ClienteDTO;
import br.goldbach.eventoservice.dto.EventoDTO;
import br.goldbach.eventoservice.dto.ProfissionalDTO;
import br.goldbach.eventoservice.model.Evento;
import br.goldbach.eventoservice.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventoService {
    private final EventoRepository eventoRepository;
    private final WebClient.Builder webClientBuilder;
    private final ModelMapper modelMapper;


    public ResponseEntity<List<EventoDTO>> buscarTodos() {
        List<EventoDTO> eventos = eventoRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
        return ResponseEntity.ok().body(eventos);
    }

    public ResponseEntity<EventoDTO> buscarPorId(Long id) {
        Optional<Evento> evento = eventoRepository.findById(id);
        if (evento.isPresent()) {
            return ResponseEntity.ok().body(modelMapper.map(evento, EventoDTO.class));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    public ResponseEntity<EventoDTO> inserir(EventoDTO eventoDTO) {
        Evento evento = new Evento();
        List<Long> profissionaisId = buscarProfissionais(eventoDTO.getProfissional());
        evento.setDescricao(eventoDTO.getDescricao());
        evento.setClienteId(eventoDTO.getCliente().getId());
        evento.setProfissionalId(profissionaisId);
        eventoRepository.save(evento);
        return ResponseEntity.ok().body(eventoDTO);
    }

    private List<Long> buscarProfissionais(List<ProfissionalDTO> profissional) {
        return profissional.stream()
                .map(p -> webClientBuilder.build().get()
                        .uri("http://api-gateway/api/profissionais/especialidade/{especialidade}", p.getEspecialidade().getDescricao())
                        .retrieve()
                        .bodyToMono(ProfissionalDTO.class)
                        .block()).filter(Objects::nonNull)
                .map(ProfissionalDTO::getId).toList();
    }

    private EventoDTO mapToDto(Evento evento) {
        EventoDTO eventoDTO = new EventoDTO();
        eventoDTO.setId(evento.getId());
        eventoDTO.setDescricao(evento.getDescricao());

        List<ProfissionalDTO> profissionais = evento.getProfissionalId().stream()
                .map(p -> webClientBuilder.build().get()
                        .uri("http://api-gateway/api/profissionais/{id}", p)
                        .retrieve()
                        .bodyToMono(ProfissionalDTO.class)
                        .block()).toList();

        ClienteDTO cliente = webClientBuilder.build().get()
                .uri("http://api-gateway/api/clientes/{id}", evento.getClienteId())
                .retrieve()
                .bodyToMono(ClienteDTO.class)
                .block();

        eventoDTO.setProfissional(profissionais);
        eventoDTO.setCliente(cliente);
        return eventoDTO;
    }
}
