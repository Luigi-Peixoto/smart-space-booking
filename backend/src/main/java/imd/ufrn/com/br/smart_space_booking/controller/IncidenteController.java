package imd.ufrn.com.br.smart_space_booking.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import imd.ufrn.com.br.smart_space_booking.dto.IncidenteRequestDTO;
import imd.ufrn.com.br.smart_space_booking.dto.IncidenteResponseDTO;
import imd.ufrn.com.br.smart_space_booking.service.IncidenteService;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/incidentes")
public class IncidenteController {

    private final IncidenteService incidenteService;

    // ← incidenteRepository e usuarioService removidos do controller
    public IncidenteController(IncidenteService incidenteService) {
        this.incidenteService = incidenteService;
    }

    @PostMapping
    public ResponseEntity<Void> reportar(
            @RequestBody IncidenteRequestDTO dto,
            @RequestHeader("X-Usuario-Id") Long usuarioId) {
        incidenteService.reportarProblema(dto, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{id}/aprovar")
    public ResponseEntity<Void> aprovar(
            @PathVariable Long id,
            @RequestHeader("X-Usuario-Id") Long adminId) {
        incidenteService.aprovarIncidente(id, adminId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/rejeitar")
    public ResponseEntity<Void> rejeitar(
            @PathVariable Long id,
            @RequestHeader("X-Usuario-Id") Long adminId) {
        incidenteService.rejeitarIncidente(id, adminId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pendentes")
    public ResponseEntity<List<IncidenteResponseDTO>> listarPendentes(
            @RequestHeader("X-Usuario-Id") Long adminId) {
        return ResponseEntity.ok(incidenteService.listarPendentes(adminId));  // validação dentro do service
    }
}