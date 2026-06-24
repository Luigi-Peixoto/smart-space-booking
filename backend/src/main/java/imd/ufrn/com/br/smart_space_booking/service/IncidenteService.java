package imd.ufrn.com.br.smart_space_booking.service;

import imd.ufrn.com.br.smart_space_booking.dto.IncidenteRequestDTO;
import imd.ufrn.com.br.smart_space_booking.enums.IncidenteStatus;
import imd.ufrn.com.br.smart_space_booking.enums.StatusRecurso;
import imd.ufrn.com.br.smart_space_booking.exception.RegraNegocioException;
import imd.ufrn.com.br.smart_space_booking.model.Incidente;
import imd.ufrn.com.br.smart_space_booking.repository.IncidenteRepository;
import imd.ufrn.com.br.smart_space_booking.repository.SalaRepository;
import imd.ufrn.com.br.smart_space_booking.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

@Service
public class IncidenteService {

    private final IncidenteRepository incidenteRepository;
    private final SalaRepository salaRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    public IncidenteService(IncidenteRepository incidenteRepository, SalaRepository salaRepository, UsuarioRepository usuarioRepository, UsuarioService usuarioService) {
        this.incidenteRepository = incidenteRepository;
        this.salaRepository = salaRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
    }

    public void reportarProblema(IncidenteRequestDTO dto, Long usuarioId) {
        var sala = salaRepository.findById(dto.salaId())
                .orElseThrow(() -> new RuntimeException("Sala não encontrada."));
        var usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        Incidente incidente = new Incidente();
        incidente.setSala(sala);
        incidente.setUsuario(usuario);
        incidente.setDescricao(dto.descricao());
        
        incidenteRepository.save(incidente);
    }

    @Transactional
    public void aprovarIncidente(Long incidenteId, Long adminId) {
        usuarioService.validarRole(adminId, "ADMIN");

        Incidente incidente = incidenteRepository.findById(incidenteId)
                .orElseThrow(() -> new RuntimeException("Incidente não encontrado."));

        if (incidente.getStatus() != IncidenteStatus.ABERTO) {
            throw new RegraNegocioException("Apenas incidentes abertos podem ser aprovados.");
        }

        incidente.setStatus(IncidenteStatus.EM_ANDAMENTO);
        incidenteRepository.save(incidente);
    
        var sala = incidente.getSala();
        sala.setStatus(StatusRecurso.MANUTENCAO);
        salaRepository.save(sala);
        
        // Aqui no futuro poderia ter uma lógica para cancelar as reservas daquela sala
    }

    @Transactional
    public void rejeitarIncidente(Long incidenteId, Long adminId) {
        usuarioService.validarRole(adminId, "ADMIN");

        Incidente incidente = incidenteRepository.findById(incidenteId)
                .orElseThrow(() -> new RuntimeException("Incidente não encontrado."));

        if (incidente.getStatus() != IncidenteStatus.ABERTO) {
            throw new RegraNegocioException("Apenas incidentes abertos podem ser rejeitados.");
        }

        incidente.setStatus(IncidenteStatus.RESOLVIDO);
        incidenteRepository.save(incidente);
    }
}