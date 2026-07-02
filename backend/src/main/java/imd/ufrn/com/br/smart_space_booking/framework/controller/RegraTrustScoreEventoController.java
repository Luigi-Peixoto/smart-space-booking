package imd.ufrn.com.br.smart_space_booking.framework.controller;

import imd.ufrn.com.br.smart_space_booking.framework.dto.RegraTrustScoreEventoRequestDTO;
import imd.ufrn.com.br.smart_space_booking.framework.dto.RegraTrustScoreEventoResponseDTO;
import imd.ufrn.com.br.smart_space_booking.framework.service.RegraTrustScoreEventoService;
import imd.ufrn.com.br.smart_space_booking.framework.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/regras-trust-score-evento")
public class RegraTrustScoreEventoController {

    private final RegraTrustScoreEventoService regraService;
    private final UsuarioService usuarioService;

    public RegraTrustScoreEventoController(RegraTrustScoreEventoService regraService, UsuarioService usuarioService) {
        this.regraService = regraService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<RegraTrustScoreEventoResponseDTO>> listarRegras(
            @RequestHeader(value = "X-Usuario-Id", required = true) Long userId) {
        usuarioService.validarRole(userId, "ADMIN");
        return ResponseEntity.ok(regraService.listarTodas());
    }

    /* ex:
     * Body: {
     *   "evento": "CANCELAMENTO_TARDIO",
     *   "delta": -15,
     *   "parametro": 2,
     *   "descricao": "Penalidade por cancelar com menos de 2 horas de antecedência."
     * }
     */
    @PostMapping
    public ResponseEntity<RegraTrustScoreEventoResponseDTO> criarRegra(
            @RequestBody RegraTrustScoreEventoRequestDTO dto,
            @RequestHeader(value = "X-Usuario-Id", required = true) Long userId) {
        usuarioService.validarRole(userId, "ADMIN");
        return ResponseEntity.status(HttpStatus.CREATED).body(regraService.criar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegraTrustScoreEventoResponseDTO> atualizarRegra(
            @PathVariable Long id,
            @RequestBody RegraTrustScoreEventoRequestDTO dto,
            @RequestHeader(value = "X-Usuario-Id", required = true) Long userId) {
        usuarioService.validarRole(userId, "ADMIN");
        return ResponseEntity.ok(regraService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarRegra(
            @PathVariable Long id,
            @RequestHeader(value = "X-Usuario-Id", required = true) Long userId) {
        usuarioService.validarRole(userId, "ADMIN");
        regraService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
