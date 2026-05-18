package imd.ufrn.com.br.smart_space_booking.controller;

import imd.ufrn.com.br.smart_space_booking.dto.IncidenteRequestDTO;
import imd.ufrn.com.br.smart_space_booking.enums.IncidenteStatus;
import imd.ufrn.com.br.smart_space_booking.model.Incidente;
import imd.ufrn.com.br.smart_space_booking.service.IncidenteService;
import imd.ufrn.com.br.smart_space_booking.service.UsuarioService;
import imd.ufrn.com.br.smart_space_booking.repository.IncidenteRepository;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/incidentes")
public class IncidenteController {

    private final IncidenteService incidenteService;
    private final UsuarioService usuarioService;
    private final IncidenteRepository incidenteRepository;

    public IncidenteController(IncidenteService incidenteService, UsuarioService usuarioService, IncidenteRepository incidenteRepository) {
        this.incidenteService = incidenteService;
        this.usuarioService = usuarioService;
        this.incidenteRepository = incidenteRepository;
    }

    @PostMapping
    public ResponseEntity<Void> reportar(
            @RequestBody IncidenteRequestDTO dto,
            @RequestHeader(value = "X-Usuario-Id", required = true) Long usuarioId) {
        incidenteService.reportarProblema(dto, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{id}/aprovar")
    public ResponseEntity<Void> aprovar(
            @PathVariable Long id,
            @RequestHeader(value = "X-Usuario-Id", required = true) Long adminId) {
        
        incidenteService.aprovarIncidente(id, adminId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/rejeitar")
    public ResponseEntity<Void> rejeitar(
            @PathVariable Long id,
            @RequestHeader(value = "X-Usuario-Id", required = true) Long adminId) {
        
        incidenteService.rejeitarIncidente(id, adminId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/pendentes")
    public ResponseEntity<List<Incidente>> listarPendentes(
            @RequestHeader(value = "X-Usuario-Id", required = true) Long adminId) {
        
        usuarioService.validarRole(adminId, "ADMIN");
        List<Incidente> lista = incidenteRepository.findByStatus(IncidenteStatus.ABERTO);
        return ResponseEntity.ok(lista);
    }
}