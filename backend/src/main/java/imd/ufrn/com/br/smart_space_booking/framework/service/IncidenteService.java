package imd.ufrn.com.br.smart_space_booking.framework.service;

import java.util.List;

import org.springframework.stereotype.Service;

import imd.ufrn.com.br.smart_space_booking.framework.dto.IncidenteRequestDTO;
import imd.ufrn.com.br.smart_space_booking.framework.dto.IncidenteResponseDTO;
import imd.ufrn.com.br.smart_space_booking.framework.enums.IncidenteStatus;
import imd.ufrn.com.br.smart_space_booking.framework.enums.StatusRecurso;
import imd.ufrn.com.br.smart_space_booking.framework.exception.RegraNegocioException;
import imd.ufrn.com.br.smart_space_booking.framework.model.Incidente;
import imd.ufrn.com.br.smart_space_booking.framework.repository.IncidenteRepository;
import imd.ufrn.com.br.smart_space_booking.framework.repository.RecursoRepository;
import imd.ufrn.com.br.smart_space_booking.framework.repository.UsuarioRepository;
import jakarta.transaction.Transactional;

@Service
public class IncidenteService {

    private final IncidenteRepository incidenteRepository;
    private final RecursoRepository recursoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    public IncidenteService(IncidenteRepository incidenteRepository,
                            RecursoRepository recursoRepository,
                            UsuarioRepository usuarioRepository,
                            UsuarioService usuarioService) {
        this.incidenteRepository = incidenteRepository;
        this.recursoRepository = recursoRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
    }

    public void reportarProblema(IncidenteRequestDTO dto, Long usuarioId) {
        var recurso = recursoRepository.findById(dto.recursoId())
                .orElseThrow(() -> new RuntimeException("Recurso não encontrado."));
        var usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        Incidente incidente = new Incidente();
        incidente.setRecurso(recurso);
        incidente.setUsuario(usuario);
        incidente.setDescricao(dto.descricao());
        incidenteRepository.save(incidente);
    }

    @Transactional
    public void aprovarIncidente(Long incidenteId, Long adminId) {
        usuarioService.validarRole(adminId, "ADMIN");

        Incidente incidente = incidenteRepository.findById(incidenteId)
                .orElseThrow(() -> new RuntimeException("Incidente não encontrado."));

        if (incidente.getStatus() != IncidenteStatus.ABERTO)
            throw new RegraNegocioException("Apenas incidentes abertos podem ser aprovados.");

        incidente.setStatus(IncidenteStatus.EM_ANDAMENTO);
        incidenteRepository.save(incidente);

        var recurso = incidente.getRecurso();
        recurso.setStatus(StatusRecurso.MANUTENCAO);
        recursoRepository.save(recurso);
    }

    @Transactional
    public void rejeitarIncidente(Long incidenteId, Long adminId) {
        usuarioService.validarRole(adminId, "ADMIN");

        Incidente incidente = incidenteRepository.findById(incidenteId)
                .orElseThrow(() -> new RuntimeException("Incidente não encontrado."));

        if (incidente.getStatus() != IncidenteStatus.ABERTO)
            throw new RegraNegocioException("Apenas incidentes abertos podem ser rejeitados.");

        incidente.setStatus(IncidenteStatus.RESOLVIDO);
        incidenteRepository.save(incidente);
    }

    public List<IncidenteResponseDTO> listarPendentes(Long adminId) {
        usuarioService.validarRole(adminId, "ADMIN");
        return incidenteRepository.findByStatus(IncidenteStatus.ABERTO)
                .stream()
                .map(IncidenteResponseDTO::fromEntity)
                .toList();
    }
}