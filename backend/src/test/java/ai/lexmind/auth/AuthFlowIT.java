package ai.lexmind.auth;

import ai.lexmind.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthFlowIT extends AbstractIntegrationTest {

    @Test
    void registerThenAccessMe() throws Exception {
        var register = Map.of(
                "email", "adv@test.com", "password", "password123",
                "fullName", "Adv Test", "role", "ADVOCATE");

        String body = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.user.role").value("ADVOCATE"))
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(body).path("data").path("accessToken").asText();
        assertThat(token).isNotBlank();

        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("adv@test.com"));
    }

    @Test
    void loginWithWrongPasswordIsUnauthorized() throws Exception {
        var register = Map.of(
                "email", "wrong@test.com", "password", "password123",
                "fullName", "Wrong Pwd", "role", "LAW_STUDENT");
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        var login = Map.of("email", "wrong@test.com", "password", "incorrect");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
    }
}
