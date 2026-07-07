package imd.ufrn.com.br.smart_space_booking.framework.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import imd.ufrn.com.br.smart_space_booking.framework.dto.EventoDisponivelDTO;
import imd.ufrn.com.br.smart_space_booking.framework.dto.NivelExigenciaDisponivelDTO;
import imd.ufrn.com.br.smart_space_booking.framework.dto.RegraTrustScoreRequestDTO;
import imd.ufrn.com.br.smart_space_booking.framework.dto.RegraTrustScoreResponseDTO;
import imd.ufrn.com.br.smart_space_booking.framework.dto.RestricaoDisponivelDTO;
import imd.ufrn.com.br.smart_space_booking.framework.enums.CategoriaRegraTrustScore;
import imd.ufrn.com.br.smart_space_booking.framework.service.RegraTrustScoreService;
import imd.ufrn.com.br.smart_space_booking.framework.service.UsuarioService;


@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
@RestController
@RequestMapping("/regras-trust-score")
public class RegraTrustScoreController {

    private final RegraTrustScoreService regraService;
    private final UsuarioService usuarioService;

    public RegraTrustScoreController(RegraTrustScoreService regraService, UsuarioService usuarioService) {
        this.regraService = regraService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<RegraTrustScoreResponseDTO>> listarRegras(
            @RequestParam CategoriaRegraTrustScore categoria,
            @RequestHeader(value = "X-Usuario-Id", required = true) Long userId) {
        usuarioService.validarRole(userId, "ADMIN");
        return ResponseEntity.ok(regraService.listarPorCategoria(categoria));
    }

    @GetMapping("/restricoes-disponiveis")
    public ResponseEntity<List<RestricaoDisponivelDTO>> listarRestricoesDisponiveis(
            @RequestParam(required = false) String tipoRecurso,
            @RequestHeader(value = "X-Usuario-Id", required = true) Long userId) {
        usuarioService.validarRole(userId, "ADMIN");
        return ResponseEntity.ok(regraService.listarRestricoesDisponiveis(tipoRecurso));
    }

    @GetMapping("/eventos-disponiveis")
    public ResponseEntity<List<EventoDisponivelDTO>> listarEventosDisponiveis(
            @RequestParam(required = false) String tipoRecurso,
            @RequestHeader(value = "X-Usuario-Id", required = true) Long userId) {
        usuarioService.validarRole(userId, "ADMIN");
        return ResponseEntity.ok(regraService.listarEventosDisponiveis(tipoRecurso));
    }

    @GetMapping("/niveis-exigencia-disponiveis")
    public ResponseEntity<List<NivelExigenciaDisponivelDTO>> listarNiveisExigenciaDisponiveis(
            @RequestParam String tipoRecurso,
            @RequestHeader(value = "X-Usuario-Id", required = true) Long userId) {
        usuarioService.validarRole(userId, "ADMIN");
        return ResponseEntity.ok(regraService.listarNiveisExigenciaDisponiveis(tipoRecurso));
    }

    @PostMapping
    public ResponseEntity<RegraTrustScoreResponseDTO> criarRegra(
            @RequestBody RegraTrustScoreRequestDTO dto,
            @RequestHeader(value = "X-Usuario-Id", required = true) Long userId) {
        usuarioService.validarRole(userId, "ADMIN");
        return ResponseEntity.status(HttpStatus.CREATED).body(regraService.criar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegraTrustScoreResponseDTO> atualizarRegra(
            @PathVariable Long id,
            @RequestBody RegraTrustScoreRequestDTO dto,
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
