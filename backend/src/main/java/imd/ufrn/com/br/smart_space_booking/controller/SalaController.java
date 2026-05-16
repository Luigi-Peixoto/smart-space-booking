package imd.ufrn.com.br.smart_space_booking.controller;


import java.util.List;

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
import imd.ufrn.com.br.smart_space_booking.service.SalaService;
import imd.ufrn.com.br.smart_space_booking.service.UsuarioService;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/salas")
public class SalaController {

    private final SalaService salaService;
    private final UsuarioService usuarioService;

    public SalaController(SalaService salaService, UsuarioService usuarioService) {
        this.salaService = salaService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<SalaResponseDTO>> listarTodas() {
        List<SalaResponseDTO> salas = salaService.listarTodas();
        return ResponseEntity.ok(salas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalaResponseDTO> buscarPorId(@PathVariable Long id) {
        return salaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SalaResponseDTO> criar(@RequestBody Sala sala) {
        SalaResponseDTO response = salaService.salvar(sala);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(
            @PathVariable Long id, 
            @RequestHeader(value = "X-Usuario-Id", required = true) Long userId) {
        usuarioService.validarDonoOuAdmin(userId, userId);
        boolean excluido = salaService.deletar(id);

        if (excluido) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<SalaResponseDTO> atualizar(@PathVariable Long id, @RequestBody Sala sala) {
        SalaResponseDTO response = salaService.atualizar(id, sala);
        return ResponseEntity.ok(response);
    }

}