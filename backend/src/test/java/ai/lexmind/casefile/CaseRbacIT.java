package ai.lexmind.casefile;

import ai.lexmind.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CaseRbacIT extends AbstractIntegrationTest {

    private String registerAndGetToken(String email) throws Exception {
        var register = Map.of(
                "email", email, "password", "password123",
                "fullName", "User " + email, "role", "ADVOCATE");
        String body = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").path("accessToken").asText();
    }

    private String createCase(String token, String title) throws Exception {
        String body = mockMvc.perform(post("/api/v1/cases")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", title))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").path("id").asText();
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/api/v1/cases"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usersCannotReadEachOthersCases() throws Exception {
        String tokenA = registerAndGetToken("owner@rbac.com");
        String caseId = createCase(tokenA, "Owner's matter");

        // Owner can read it.
        mockMvc.perform(get("/api/v1/cases/" + caseId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Owner's matter"));

        // A different user must not (404, existence hidden).
        String tokenB = registerAndGetToken("intruder@rbac.com");
        mockMvc.perform(get("/api/v1/cases/" + caseId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }
}
