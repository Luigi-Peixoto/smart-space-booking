package imd.ufrn.com.br.smart_space_booking.instancia_equipamento.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import imd.ufrn.com.br.smart_space_booking.instancia_equipamento.dto.EquipamentoResponseDTO;
import imd.ufrn.com.br.smart_space_booking.instancia_equipamento.model.Equipamento;
import imd.ufrn.com.br.smart_space_booking.instancia_equipamento.service.EquipamentoService;
import imd.ufrn.com.br.smart_space_booking.framework.service.UsuarioService;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/equipamentos")
public class EquipamentoController {

    private final EquipamentoService equipamentoService;
    private final UsuarioService usuarioService;

    public EquipamentoController(EquipamentoService equipamentoService, UsuarioService usuarioService) {
        this.equipamentoService = equipamentoService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<EquipamentoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(equipamentoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipamentoResponseDTO> buscarPorId(@PathVariable Long id) {
        return equipamentoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EquipamentoResponseDTO> criar(
            @RequestBody Equipamento equipamento,
            @RequestHeader(value = "X-Usuario-Id", required = true) Long userId) {
        usuarioService.validarRole(userId, "ADMIN");
        return ResponseEntity.status(HttpStatus.CREATED).body(equipamentoService.salvar(equipamento));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(
            @PathVariable Long id,
            @RequestHeader(value = "X-Usuario-Id", required = true) Long userId) {
        usuarioService.validarRole(userId, "ADMIN");
        return equipamentoService.deletar(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipamentoResponseDTO> atualizar(
            @PathVariable Long id,
            @RequestBody Equipamento equipamento,
            @RequestHeader(value = "X-Usuario-Id", required = true) Long userId) {
        usuarioService.validarRole(userId, "ADMIN");
        return ResponseEntity.ok(equipamentoService.atualizar(id, equipamento));
    }
}