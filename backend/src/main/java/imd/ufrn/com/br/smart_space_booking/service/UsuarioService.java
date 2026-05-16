package imd.ufrn.com.br.smart_space_booking.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import imd.ufrn.com.br.smart_space_booking.dto.UsuarioLoginDTO;
import imd.ufrn.com.br.smart_space_booking.dto.UsuarioRequestDTO;
import imd.ufrn.com.br.smart_space_booking.dto.UsuarioResponseDTO;
import imd.ufrn.com.br.smart_space_booking.enums.UsuarioStatus;
import imd.ufrn.com.br.smart_space_booking.exception.AcessoNegadoException;
import imd.ufrn.com.br.smart_space_booking.exception.RegraNegocioException;
import imd.ufrn.com.br.smart_space_booking.exception.UsuarioNotFoundException;
import imd.ufrn.com.br.smart_space_booking.model.Usuario;
import imd.ufrn.com.br.smart_space_booking.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public UsuarioResponseDTO realizarAcesso(UsuarioLoginDTO loginDTO) {
        return usuarioRepository.findByEmail(loginDTO.email())
                .map(usuario -> convertToDTO(usuario))
                .orElseGet(() -> {
                    Usuario novoUsuario = new Usuario();
                    novoUsuario.setEmail(loginDTO.email());
                    novoUsuario.setNome(loginDTO.nome());
                    novoUsuario.setStatus(UsuarioStatus.ATIVO);

                    if (loginDTO.email().endsWith("@admin.com") || loginDTO.email().contains("@admin")) {
                        novoUsuario.setPerfil("ADMIN");
                    } else {
                        novoUsuario.setPerfil("USER");
                    }

                    Usuario usuarioSalvo = usuarioRepository.save(novoUsuario);
                    return convertToDTO(usuarioSalvo);
                });
    }

    public List<UsuarioResponseDTO> findAll() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public UsuarioResponseDTO findById(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuário não encontrado com o ID: " + id));
        return convertToDTO(usuario);
    }

    @Transactional
    public UsuarioResponseDTO criarUsuarioManual(UsuarioRequestDTO dto) {
        if (usuarioRepository.findByEmail(dto.email()).isPresent()) {
            throw new RegraNegocioException("Já existe um usuário cadastrado com o e-mail: " + dto.email());
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(dto.email());
        usuario.setNome(dto.nome());
        usuario.setPerfil(dto.perfil() != null ? dto.perfil() : "USER");
        usuario.setTrustScore(dto.trustScore() != null ? dto.trustScore() : 100);

        if (dto.status() != null) {
            usuario.setStatus(UsuarioStatus.valueOf(dto.status().toUpperCase()));
        } else {
            usuario.setStatus(UsuarioStatus.ATIVO);
        }

        return convertToDTO(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponseDTO atualizarUsuario(Long id, UsuarioRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuário não encontrado com o ID: " + id));

        if (!usuario.getEmail().equals(dto.email()) && usuarioRepository.findByEmail(dto.email()).isPresent()) {
            throw new RegraNegocioException("O e-mail " + dto.email() + " já está sendo usado por outro usuário.");
        }

        usuario.setEmail(dto.email());
        usuario.setNome(dto.nome());
        
        if (dto.perfil() != null) {
            usuario.setPerfil(dto.perfil());
        }
        if (dto.trustScore() != null) {
            usuario.setTrustScore(dto.trustScore());
        }
        if (dto.status() != null) {
            usuario.setStatus(UsuarioStatus.valueOf(dto.status().toUpperCase()));
        }

        return convertToDTO(usuarioRepository.save(usuario));
    }

    @Transactional
    public void banirUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuário não encontrado com o ID: " + id));
        
        usuario.setStatus(UsuarioStatus.BANIDO);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void suspenderUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuário não encontrado com o ID: " + id));
        
        usuario.setStatus(UsuarioStatus.SUSPENSO);
        usuarioRepository.save(usuario);
    }

    public void validarRole(Long usuarioId, String roleNecessaria) {
        if (roleNecessaria == null || roleNecessaria.isBlank()) {
            throw new IllegalArgumentException("Role necessária não pode ser nula ou vazia.");
        }
        if (usuarioId == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo.");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new AcessoNegadoException("Acesso negado: Usuário inválido."));
        System.out.println("ID recebido do Postman: " + usuarioId);
        System.out.println("Perfil encontrado no Banco: '" + usuario.getPerfil() + "'");
        System.out.println("Role exigida: '" + roleNecessaria + "'");
        if (!usuario.getPerfil().equalsIgnoreCase(roleNecessaria)) {
            throw new AcessoNegadoException("Acesso negado: usuário não possui a role necessária (" + roleNecessaria + ").");
        }
    }

    public void validarDono(Long usuarioRequisitanteId, Long donoDoRecursoId) {
        if (usuarioRequisitanteId == null || !usuarioRequisitanteId.equals(donoDoRecursoId)) {
            throw new AcessoNegadoException("Acesso negado: Você não é o proprietário deste recurso.");
        }
    }

    public void validarDonoOuAdmin(Long usuarioRequisitanteId, Long donoDoRecursoId) {
        if (usuarioRequisitanteId == null) {
            throw new IllegalArgumentException("ID do usuário requisitante não pode ser nulo.");
        }
        if (donoDoRecursoId == null) {
            throw new IllegalArgumentException("ID do dono do recurso não pode ser nulo.");
        }

        Usuario usuario = usuarioRepository.findById(usuarioRequisitanteId)
                .orElseThrow(() -> new AcessoNegadoException("Acesso negado: Usuário inválido."));

        boolean isAdmin = "ADMIN".equalsIgnoreCase(usuario.getPerfil());
        boolean isDono = usuarioRequisitanteId.equals(donoDoRecursoId);

        if (!isAdmin && !isDono) {
            throw new AcessoNegadoException("Acesso negado: Você não é o proprietário deste recurso nem um administrador.");
        }
    }

    private UsuarioResponseDTO convertToDTO(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getNome(),
                usuario.getTrustScore(),
                usuario.getPerfil(),
                usuario.getStatus() != null ? usuario.getStatus().name() : null
        );
    }
}