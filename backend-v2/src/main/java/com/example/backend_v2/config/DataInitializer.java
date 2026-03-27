package com.example.backend_v2.config;

import com.example.backend_v2.model.entity.Empresa;
import com.example.backend_v2.model.entity.Usuario;
import com.example.backend_v2.model.enums.RolUsuario;
import com.example.backend_v2.repository.EmpresaRepository;
import com.example.backend_v2.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.findByEmail("admin@coflow.com") != null) {
            return; // Ya existe, no duplicar
        }

        Empresa empresa = new Empresa();
        empresa.setNombre("CoFlow Demo");
        empresa.setFechaCreacion(LocalDate.now());
        empresa = empresaRepository.save(empresa);

        Usuario usuario = new Usuario();
        usuario.setNombre("Admin");
        usuario.setApellidos("CoFlow");
        usuario.setEmail("admin@coflow.com");
        usuario.setPassword(passwordEncoder.encode("Admin1234"));
        usuario.setRol(RolUsuario.ADMIN);
        usuario.setFechaCreacion(LocalDate.now());
        usuario.setEmpresa(empresa);
        usuarioRepository.save(usuario);

        System.out.println("=== Usuario de ejemplo creado ===");
        System.out.println("Email:    admin@coflow.com");
        System.out.println("Password: Admin1234");
        System.out.println("Empresa:  CoFlow Demo");
        System.out.println("Rol:      ADMIN");
    }
}
