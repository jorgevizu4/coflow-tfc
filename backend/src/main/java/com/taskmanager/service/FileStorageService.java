package com.taskmanager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Interfaz para servicio de almacenamiento de archivos.
 */
public interface FileStorageService {

    /**
     * Almacena un archivo y devuelve la URL/path donde se guardó.
     */
    String almacenarArchivo(MultipartFile archivo, Long empresaId, Long tareaId) throws IOException;

    /**
     * Elimina un archivo.
     */
    void eliminarArchivo(String urlArchivo) throws IOException;

    /**
     * Verifica si un archivo existe.
     */
    boolean existeArchivo(String urlArchivo);
}

/**
 * Implementación local de FileStorageService.
 * Almacena archivos en el sistema de archivos local.
 * Para MVP - en producción usar S3/Azure Blob/MinIO.
 */
@Service
class LocalFileStorageService implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageService.class);

    private final Path uploadDir;

    public LocalFileStorageService(@Value("${app.file-storage.upload-dir:./uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        crearDirectorioSiNoExiste(this.uploadDir);
    }

    @Override
    public String almacenarArchivo(MultipartFile archivo, Long empresaId, Long tareaId) throws IOException {
        if (archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        // Crear estructura de directorios: uploads/{empresaId}/{tareaId}/
        Path directorioTarea = uploadDir.resolve(String.valueOf(empresaId))
                                        .resolve(String.valueOf(tareaId));
        crearDirectorioSiNoExiste(directorioTarea);

        // Generar nombre único para evitar colisiones
        String nombreOriginal = archivo.getOriginalFilename();
        String extension = obtenerExtension(nombreOriginal);
        String nombreUnico = UUID.randomUUID().toString() + extension;

        // Guardar archivo
        Path rutaArchivo = directorioTarea.resolve(nombreUnico);
        Files.copy(archivo.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);

        // Devolver path relativo
        String rutaRelativa = uploadDir.relativize(rutaArchivo).toString().replace("\\", "/");
        log.info("Archivo almacenado: {} -> {}", nombreOriginal, rutaRelativa);

        return rutaRelativa;
    }

    @Override
    public void eliminarArchivo(String urlArchivo) throws IOException {
        Path rutaArchivo = uploadDir.resolve(urlArchivo);
        if (Files.exists(rutaArchivo)) {
            Files.delete(rutaArchivo);
            log.info("Archivo eliminado: {}", urlArchivo);
        }
    }

    @Override
    public boolean existeArchivo(String urlArchivo) {
        Path rutaArchivo = uploadDir.resolve(urlArchivo);
        return Files.exists(rutaArchivo);
    }

    private void crearDirectorioSiNoExiste(Path directorio) {
        try {
            if (!Files.exists(directorio)) {
                Files.createDirectories(directorio);
                log.debug("Directorio creado: {}", directorio);
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio: " + directorio, e);
        }
    }

    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) {
            return "";
        }
        return nombreArchivo.substring(nombreArchivo.lastIndexOf("."));
    }
}
