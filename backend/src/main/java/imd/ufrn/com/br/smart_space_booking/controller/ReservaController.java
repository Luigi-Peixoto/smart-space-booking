package imd.ufrn.com.br.smart_space_booking.controller;

import java.time.LocalDate;
import java.util.List;

import imd.ufrn.com.br.smart_space_booking.service.ReservaSalaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import imd.ufrn.com.br.smart_space_booking.framework.dto.HorarioOcupadoDTO;
import imd.ufrn.com.br.smart_space_booking.framework.dto.ReservaRequestDTO;
import imd.ufrn.com.br.smart_space_booking.framework.dto.ReservaResponseDTO;
import imd.ufrn.com.br.smart_space_booking.framework.service.UsuarioService;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/reservas")
public class ReservaController {

    private final ReservaSalaService reservaService;
    private final UsuarioService usuarioService;

    public ReservaController(ReservaSalaService reservaService, UsuarioService usuarioService) {
        this.reservaService = reservaService;
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<ReservaResponseDTO> create(@RequestBody ReservaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaService.create(dto));
    }

    @GetMapping("/ocupados")
    public ResponseEntity<List<HorarioOcupadoDTO>> getOcupados(
            @RequestParam Long salaId,
            @RequestParam String data) {

        LocalDate localDate = LocalDate.parse(data);
        return ResponseEntity.ok(reservaService.findOcupados(salaId, localDate));
    }

    @GetMapping
    public ResponseEntity<List<ReservaResponseDTO>> findAll() {
        return ResponseEntity.ok(reservaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id, 
            @RequestHeader(value = "X-Usuario-Id", required = true) Long userId) {
        usuarioService.validarRole(userId, "ADMIN");
        
        reservaService.delete(id);
        return ResponseEntity.noContent().build();
    }

   @PutMapping("/{id}/cancelar")
   public ResponseEntity<Void> cancelar(
            @PathVariable Long id,
            @RequestBody String motivo,
            @RequestHeader("X-Usuario-Id") Long usuarioLogadoId) {
        // usuarioService.validarDonoOuAdmin(usuarioLogadoId, id);
        reservaService.cancelarReserva(id, usuarioLogadoId, motivo);
        return ResponseEntity.noContent().build();
   }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<ReservaResponseDTO>> findByUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(reservaService.findByUsuario(usuarioId));
    }

}