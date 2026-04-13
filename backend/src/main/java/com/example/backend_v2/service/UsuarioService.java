package com.example.backend_v2.service;

import com.example.backend_v2.model.entity.Usuario;
import com.example.backend_v2.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final AuthService authService;

    public List<Usuario> findAll() {
        return usuarioRepository.findByEmpresaId(authService.getEmpresaIdActual());
    }

    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id)
                .filter(u -> u.getEmpresa().getId() == authService.getEmpresaIdActual());
    }

    public Usuario save(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public void deleteById(Long id) {
        usuarioRepository.deleteById(id);
    }
}
