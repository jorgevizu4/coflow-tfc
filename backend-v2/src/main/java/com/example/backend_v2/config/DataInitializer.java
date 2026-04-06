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
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    record EmpresaSeed(String nombre, String adminNombre, String adminApellidos, String adminEmail, String adminPassword) {}

    @Override
    public void run(String... args) {
        if (empresaRepository.count() > 0) {
            return; // Ya existen datos, no duplicar
        }

        List<EmpresaSeed> seeds = List.of(
            new EmpresaSeed("TechNova Solutions",   "Carlos",  "García López",   "admin@technova.com",  "Admin1234"),
            new EmpresaSeed("Creativa Studio",      "Laura",   "Martínez Ruiz",  "admin@creativa.com",  "Admin1234"),
            new EmpresaSeed("DataBridge Corp",      "Marcos",  "Fernández Gil",  "admin@databridge.com","Admin1234")
        );

        for (EmpresaSeed seed : seeds) {
            Empresa empresa = new Empresa();
            empresa.setNombre(seed.nombre());
            empresa.setFechaCreacion(LocalDate.now());
            empresa = empresaRepository.save(empresa);

            Usuario admin = new Usuario();
            admin.setNombre(seed.adminNombre());
            admin.setApellidos(seed.adminApellidos());
            admin.setEmail(seed.adminEmail());
            admin.setPassword(passwordEncoder.encode(seed.adminPassword()));
            admin.setRol(RolUsuario.ADMIN);
            admin.setFechaCreacion(LocalDate.now());
            admin.setEmpresa(empresa);
            usuarioRepository.save(admin);

            System.out.println("=== Empresa creada: " + seed.nombre() + " ===");
            System.out.println("  Admin email:    " + seed.adminEmail());
            System.out.println("  Admin password: " + seed.adminPassword());
        }
    }
}
