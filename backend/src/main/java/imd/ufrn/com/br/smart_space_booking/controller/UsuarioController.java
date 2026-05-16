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

import imd.ufrn.com.br.smart_space_booking.dto.UsuarioLoginDTO;
import imd.ufrn.com.br.smart_space_booking.dto.UsuarioRequestDTO;
import imd.ufrn.com.br.smart_space_booking.dto.UsuarioResponseDTO;
import imd.ufrn.com.br.smart_space_booking.service.UsuarioService;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/acesso")
    public ResponseEntity<UsuarioResponseDTO> acessar(@RequestBody UsuarioLoginDTO loginDTO) {
        UsuarioResponseDTO response = usuarioService.realizarAcesso(loginDTO);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos() {
        return ResponseEntity.ok(usuarioService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.findById(id));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criar(
            @RequestBody UsuarioRequestDTO dto,
            @RequestHeader(value = "X-Usuario-Id", required = true) Long userId) {
        usuarioService.validarRole(userId, "ADMIN");
        UsuarioResponseDTO response = usuarioService.criarUsuarioManual(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> atualizar(@PathVariable Long id, @RequestBody UsuarioRequestDTO dto) {
        UsuarioResponseDTO response = usuarioService.atualizarUsuario(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/banir")
    public ResponseEntity<Void> banir(
            @PathVariable Long id, 
            @RequestHeader(value = "X-Usuario-Id", required = true) Long userId) {
        usuarioService.validarRole(userId, "ADMIN");
        usuarioService.banirUsuario(id);
        
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/suspender")
    public ResponseEntity<Void> suspender(
            @PathVariable Long id, 
            @RequestHeader(value = "X-Usuario-Id", required = true) Long userId) {
        usuarioService.validarRole(userId, "ADMIN");
        usuarioService.suspenderUsuario(id);
        
        return ResponseEntity.noContent().build();
    }
}