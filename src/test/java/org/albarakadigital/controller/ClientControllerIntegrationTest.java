package org.albarakadigital.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ClientControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "CLIENT")
    void createOperation_DepositBelowThreshold_Succeeds() throws Exception {
        mockMvc.perform(post("/api/client/operations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"DEPOSIT\", \"amount\": 5000}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void createOperation_WithdrawalInsufficientBalance_Fails() throws Exception {
        mockMvc.perform(post("/api/client/operations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"WITHDRAWAL\", \"amount\": 30000}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void getOperations_ReturnsList() throws Exception {
        mockMvc.perform(get("/api/client/operations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}