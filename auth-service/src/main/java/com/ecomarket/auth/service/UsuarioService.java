package com.ecomarket.auth.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecomarket.auth.dto.UsuarioResponse;
import com.ecomarket.auth.exception.RecursoNoEncontradoException;
import com.ecomarket.auth.model.Usuario;
import com.ecomarket.auth.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarUsuarios() {
        log.info("Listando usuarios registrados");
        List<UsuarioResponse> usuarios = usuarioRepository.findAll()
                .stream()
                .map(this::mapearUsuarioResponse)
                .toList();
        log.info("Listado de usuarios completado. Total={}", usuarios.size());
        return usuarios;
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerUsuarioPorId(Integer id) {
        log.info("Buscando usuario con id {}", id);
        return usuarioRepository.findById(id)
                .map(this::mapearUsuarioResponse)
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado con id {}", id);
                    return new RecursoNoEncontradoException("Usuario no encontrado");
                });
    }

    @Transactional
    public void eliminarUsuario(Integer id) {
        log.info("Iniciando eliminacion de usuario con id {}", id);
        if (!usuarioRepository.existsById(id)) {
            log.warn("Eliminacion rechazada: usuario {} no existe", id);
            throw new RecursoNoEncontradoException("Usuario no encontrado");
        }

        usuarioRepository.deleteById(id);
        log.info("Usuario eliminado con id {}", id);
    }

    public UsuarioResponse mapearUsuarioResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol()
        );
    }
}
