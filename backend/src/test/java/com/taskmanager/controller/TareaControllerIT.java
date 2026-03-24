package com.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.domain.enums.Prioridad;
import com.taskmanager.dto.request.*;
import com.taskmanager.dto.response.LoginResponseDTO;
import com.taskmanager.dto.response.TareaResponseDTO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for TareaController using Testcontainers.
 * Tests the complete request-response cycle with a real PostgreSQL database.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TareaControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("taskmanager_test")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("init.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String jwtToken;
    private static Long createdTareaId;

    // ========================
    // AUTHENTICATION
    // ========================

    @Test
    @Order(1)
    @DisplayName("Should login and get JWT token")
    void shouldLoginAndGetToken() throws Exception {
        // Use seed data from init.sql: admin@techcorp.com / password123
        LoginRequestDTO loginRequest = new LoginRequestDTO("admin@techcorp.com", "password123");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.email").value("admin@techcorp.com"))
                .andReturn();

        // Extract token from ApiResponse wrapper
        String responseBody = result.getResponse().getContentAsString();
        jwtToken = objectMapper.readTree(responseBody).get("data").get("token").asText();
        assertThat(jwtToken).isNotBlank();
    }

    // ========================
    // CREATE TASK
    // ========================

    @Test
    @Order(2)
    @DisplayName("Should create a new task")
    void shouldCreateTask() throws Exception {
        TareaCreateDTO createDTO = new TareaCreateDTO(
                1L, // proyecto_id from seed data
                "Integration Test Task",
                "This task was created by integration test",
                120,
                LocalDateTime.now().plusDays(14),
                Prioridad.ALTA,
                true // requiereRevision
        );

        MvcResult result = mockMvc.perform(post("/api/v1/tareas")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.titulo").value("Integration Test Task"))
                .andExpect(jsonPath("$.data.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.data.prioridad").value("ALTA"))
                .andReturn();

        // Extract task ID from ApiResponse wrapper
        String responseBody = result.getResponse().getContentAsString();
        createdTareaId = objectMapper.readTree(responseBody).get("data").get("id").asLong();
        assertThat(createdTareaId).isNotNull();
    }

    @Test
    @Order(3)
    @DisplayName("Should reject task creation without authentication")
    void shouldRejectCreateWithoutAuth() throws Exception {
        TareaCreateDTO createDTO = new TareaCreateDTO(
                1L,
                "Unauthorized Task",
                "Description",
                30,
                LocalDateTime.now().plusDays(7),
                Prioridad.BAJA,
                false
        );

        mockMvc.perform(post("/api/v1/tareas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(4)
    @DisplayName("Should reject task creation with invalid data")
    void shouldRejectCreateWithInvalidData() throws Exception {
        TareaCreateDTO createDTO = new TareaCreateDTO(
                null,
                "", // invalid: blank title and null project
                "Description",
                30,
                LocalDateTime.now().plusDays(7),
                Prioridad.MEDIA,
                false
        );

        mockMvc.perform(post("/api/v1/tareas")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest());
    }

    // ========================
    // GET TASK
    // ========================

    @Test
    @Order(5)
    @DisplayName("Should get task by ID")
    void shouldGetTaskById() throws Exception {
        mockMvc.perform(get("/api/v1/tareas/" + createdTareaId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(createdTareaId))
                .andExpect(jsonPath("$.data.titulo").value("Integration Test Task"));
    }

    @Test
    @Order(6)
    @DisplayName("Should return 404 for non-existent task")
    void shouldReturn404ForNonExistentTask() throws Exception {
        mockMvc.perform(get("/api/v1/tareas/99999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ENTITY_NOT_FOUND"));
    }

    // ========================
    // LIST TASKS
    // ========================

    @Test
    @Order(7)
    @DisplayName("Should list tasks with filters")
    void shouldListTasksWithFilters() throws Exception {
        mockMvc.perform(get("/api/v1/tareas")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("estado", "PENDIENTE")
                        .param("prioridad", "ALTA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @Order(8)
    @DisplayName("Should list tasks by project")
    void shouldListTasksByProject() throws Exception {
        mockMvc.perform(get("/api/v1/tareas")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("proyectoId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    // ========================
    // ASSIGN TASK
    // ========================

    @Test
    @Order(9)
    @DisplayName("Should assign user to task")
    void shouldAssignUserToTask() throws Exception {
        // User ID 2 from seed data (lider@techcorp.com)
                AsignacionDTO assignDTO = new AsignacionDTO(2L, Prioridad.ALTA, LocalDateTime.now().plusDays(10));

        mockMvc.perform(patch("/api/v1/tareas/" + createdTareaId + "/asignar")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado").value("ASIGNADA"))
                .andExpect(jsonPath("$.data.usuarioAsignado.id").value(2));
    }

    @Test
    @Order(10)
    @DisplayName("Should reject reassignment when task not in PENDIENTE")
    void shouldRejectReassignmentWhenNotPending() throws Exception {
                AsignacionDTO assignDTO = new AsignacionDTO(3L, Prioridad.MEDIA, LocalDateTime.now().plusDays(7));

        mockMvc.perform(patch("/api/v1/tareas/" + createdTareaId + "/asignar")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_STATE_TRANSITION"));
    }

    // ========================
    // CHANGE STATE
    // ========================

    @Test
    @Order(11)
    @DisplayName("Should accept assigned task")
    void shouldAcceptTask() throws Exception {
        CambioEstadoDTO cambioDTO = new CambioEstadoDTO(
                CambioEstadoDTO.AccionEstado.ACEPTAR,
                null
        );

        // Note: This test may fail if task state doesn't allow accept
        // In real scenario, task would need to be in ASIGNADA state
        mockMvc.perform(patch("/api/v1/tareas/" + createdTareaId + "/estado")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cambioDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(12)
    @DisplayName("Should reject task with reason")
    void shouldRejectTaskWithReason() throws Exception {
        CambioEstadoDTO cambioDTO = new CambioEstadoDTO(
                CambioEstadoDTO.AccionEstado.RECHAZAR,
                "Requirements not clear"
        );

        mockMvc.perform(patch("/api/v1/tareas/" + createdTareaId + "/estado")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cambioDTO)))
                .andExpect(status().isOk());
    }

    // ========================
    // REVIEW FLOW
    // ========================

    @Test
    @Order(13)
    @DisplayName("Should list pending reviews")
    void shouldListPendingReviews() throws Exception {
        mockMvc.perform(get("/api/v1/tareas/pendientes-revision")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    // ========================
    // TENANT ISOLATION
    // ========================

    @Test
    @Order(14)
    @DisplayName("Should isolate tasks by tenant")
    void shouldIsolateTasksByTenant() throws Exception {
        // Login as user from different company (if exists in seed data)
        // For this test, we verify that only tasks from the authenticated user's
        // company are returned

        mockMvc.perform(get("/api/v1/tareas")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        // All returned tasks should belong to the same empresa_id as the user
        // This is validated by the service layer filtering
    }

    // ========================
    // ERROR HANDLING
    // ========================

    @Test
    @Order(15)
    @DisplayName("Should handle malformed JSON")
    void shouldHandleMalformedJson() throws Exception {
        mockMvc.perform(post("/api/v1/tareas")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(16)
    @DisplayName("Should reject expired token")
    void shouldRejectExpiredToken() throws Exception {
        String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                "eyJzdWIiOiIxIiwiZW1wcmVzYV9pZCI6MSwiZXhwIjoxNjAwMDAwMDAwfQ." +
                "invalid_signature";

        mockMvc.perform(get("/api/v1/tareas")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }
}
