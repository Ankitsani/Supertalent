package com.internal.tasktracker;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Search query with status filter respects status and excludes archived tasks")
    void searchTasks_filtersCorrectlyAndExcludesArchived() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .param("q", "api")
                        .param("status", "OPEN")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.items[*].status", everyItem(is("OPEN"))))
                .andExpect(jsonPath("$.items[*].archived", everyItem(is(false))))
                .andExpect(jsonPath("$.items[*].id", not(hasItem(2)))); // Task 2 is IN_PROGRESS
    }

    @Test
    @DisplayName("Invalid status parameter returns HTTP 400 Bad Request")
    void searchTasks_invalidStatus_returns400() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .param("status", "INVALID")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Invalid status: INVALID")));
    }

    @Test
    @DisplayName("Non-positive page and pageSize clamp to safe bounds without 500 error")
    void searchTasks_boundsClamping_handlesNegativeAndZero() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .param("page", "0")
                        .param("pageSize", "-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page", is(1)))
                .andExpect(jsonPath("$.pageSize", is(10)));
    }

    @Test
    @DisplayName("Excessive pageSize clamps to maximum of 100")
    void searchTasks_boundsClamping_handlesExcessivePageSize() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .param("page", "1")
                        .param("pageSize", "500")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page", is(1)))
                .andExpect(jsonPath("$.pageSize", is(100)));
    }
}
