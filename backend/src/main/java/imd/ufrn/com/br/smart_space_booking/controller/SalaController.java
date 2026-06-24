package imd.ufrn.com.br.smart_space_booking.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
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
import org.springframework.web.bind.annotation.RestController;

import imd.ufrn.com.br.smart_space_booking.dto.SalaResponseDTO;
import imd.ufrn.com.br.smart_space_booking.model.Sala;
import imd.ufrn.com.br.smart_space_booking.service.RecursoService;
import imd.ufrn.com.br.smart_space_booking.service.UsuarioService;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/salas")
public class SalaController {

    private final RecursoService<Sala, SalaResponseDTO> recursoService;
    private final UsuarioService usuarioService;

    public SalaController(
            @Qualifier("salaService") RecursoService<Sala, SalaResponseDTO> recursoService,
            UsuarioService usuarioService) {
        this.recursoService = recursoService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<SalaResponseDTO>> listarTodas() {
        return ResponseEntity.ok(recursoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalaResponseDTO> buscarPorId(@PathVariable Long id) {
        return recursoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SalaResponseDTO> criar(
            @RequestBody Sala sala,
            @RequestHeader(value = "X-Usuario-Id", required = true) Long userId) {
        usuarioService.validarRole(userId, "ADMIN");
        return ResponseEntity.status(HttpStatus.CREATED).body(recursoService.salvar(sala));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(
            @PathVariable Long id,
            @RequestHeader(value = "X-Usuario-Id", required = true) Long userId) {
        usuarioService.validarRole(userId, "ADMIN");
        return recursoService.deletar(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<SalaResponseDTO> atualizar(
            @PathVariable Long id,
            @RequestBody Sala sala,
            @RequestHeader(value = "X-Usuario-Id", required = true) Long userId) {
        usuarioService.validarRole(userId, "ADMIN");
        return ResponseEntity.ok(recursoService.atualizar(id, sala));
    }
}