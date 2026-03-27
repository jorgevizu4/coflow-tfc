package com.example.backend_v2.controller;

import com.example.backend_v2.model.entity.Empresa;
import com.example.backend_v2.service.EmpresaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;

    @GetMapping("/{id}")
    public ResponseEntity<Empresa> findById(@PathVariable Long id) {
        return empresaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public Empresa update(@PathVariable Long id, @RequestBody Empresa empresa) {
        empresa.setId(id);
        return empresaService.save(empresa);
    }
}
