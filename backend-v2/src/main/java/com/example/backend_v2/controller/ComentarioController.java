package com.example.backend_v2.controller;

import com.example.backend_v2.dto.ApiResponse;
import com.example.backend_v2.dto.ComentarioCrearRequest;
import com.example.backend_v2.dto.ComentarioDTO;
import com.example.backend_v2.service.ComentarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comentarios")
@RequiredArgsConstructor
public class ComentarioController {

    private final ComentarioService comentarioService;

    @GetMapping("/tarea/{tareaId}")
    public ResponseEntity<ApiResponse<List<ComentarioDTO>>> findByTareaId(@PathVariable Long tareaId) {
        List<ComentarioDTO> lista = comentarioService.findByTareaId(tareaId);
        return ResponseEntity.ok(ApiResponse.<List<ComentarioDTO>>builder()
                .success(true).message("OK").data(lista).build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ComentarioDTO>> crear(
            @RequestBody ComentarioCrearRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        ComentarioDTO dto = comentarioService.crear(req, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.<ComentarioDTO>builder()
                .success(true).message("Comentario creado").data(dto).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        comentarioService.deleteById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true).message("Comentario eliminado").data(null).build());
    }
}
