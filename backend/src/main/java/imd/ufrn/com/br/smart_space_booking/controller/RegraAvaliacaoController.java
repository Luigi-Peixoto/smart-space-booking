package imd.ufrn.com.br.smart_space_booking.controller;

import imd.ufrn.com.br.smart_space_booking.dto.RegraAvaliacaoRequestDTO;
import imd.ufrn.com.br.smart_space_booking.dto.RegraAvaliacaoResponseDTO;
import imd.ufrn.com.br.smart_space_booking.service.RegraAvaliacaoService;
import imd.ufrn.com.br.smart_space_booking.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

//todo: ver se perfil admin

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/regras")
public class RegraAvaliacaoController {

    private final RegraAvaliacaoService regraService;
    private final UsuarioService usuarioService;

    public RegraAvaliacaoController(RegraAvaliacaoService regraService, UsuarioService usuarioService) {
        this.regraService = regraService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<RegraAvaliacaoResponseDTO>> listarRegras(
            @RequestHeader(value = "X-Usuario-Id", required = true) Long userId) {
        usuarioService.validarRole(userId, "ADMIN");
        List<RegraAvaliacaoResponseDTO> lista = regraService.listarTodas();
        return ResponseEntity.ok(lista);
    }

    /* ex:
     * Body: {
     *   "nome": "Limpeza geral",
     *   "descricao": "Verifique se superfícies estão livres de sujeira.",
     *   "limiBonus": 8, "deltaBonus": 15,
     *   "limiPenalidade": 5, "deltaPenalidade": -15
     * }
     */
    @PostMapping
    public ResponseEntity<RegraAvaliacaoResponseDTO> criarRegra(
            @RequestBody RegraAvaliacaoRequestDTO dto,
            @RequestHeader(value = "X-Usuario-Id", required = true) Long userId) {
        usuarioService.validarRole(userId, "ADMIN");
        return ResponseEntity.status(HttpStatus.CREATED).body(regraService.criar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegraAvaliacaoResponseDTO> atualizarRegra(
            @PathVariable Long id,
            @RequestBody RegraAvaliacaoRequestDTO dto,
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