package ai.lexmind.analysis.client;

import ai.lexmind.analysis.client.AiContracts.AgentResultsPayload;
import ai.lexmind.analysis.client.AiContracts.ChatAnswer;
import ai.lexmind.analysis.client.AiContracts.ChatRequest;
import ai.lexmind.analysis.client.AiContracts.RunAgentsRequest;
import ai.lexmind.common.config.LexMindProperties;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Calls the FastAPI AI service. Authenticated with the internal service token; bounded by a
 * read timeout (the agent graph is long-running but capped). On error, callers mark the run
 * FAILED (LLD §7 resilience).
 */
@Component
public class AiServiceClient {

    private final RestClient restClient;

    public AiServiceClient(LexMindProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        int timeoutMs = (int) Duration.ofSeconds(props.ai().timeoutSeconds()).toMillis();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(timeoutMs);
        this.restClient = RestClient.builder()
                .baseUrl(props.ai().baseUrl())
                .requestFactory(factory)
                .defaultHeader("X-Internal-Token", props.ai().serviceToken())
                .build();
    }

    /** Run the LangGraph agent pipeline for a case (synchronous pull model for MVP). */
    public AgentResultsPayload runAgents(RunAgentsRequest request) {
        return restClient.post()
                .uri("/analyze")
                .body(request)
                .retrieve()
                .body(AgentResultsPayload.class);
    }

    /** Grounded RAG answer for a case-scoped question. */
    public ChatAnswer chat(ChatRequest request) {
        return restClient.post()
                .uri("/chat")
                .body(request)
                .retrieve()
                .body(ChatAnswer.class);
    }
}
