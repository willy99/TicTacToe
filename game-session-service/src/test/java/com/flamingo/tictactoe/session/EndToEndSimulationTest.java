package com.flamingo.tictactoe.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import com.flamingo.tictactoe.engine.GameEngineApplication;
import com.flamingo.tictactoe.session.infrastructure.web.dto.SessionResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.context.ServerPortInfoApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

/**
 * Genuine cross-service end-to-end test. Unlike {@code SessionControllerIntegrationTest},
 * which mocks the Game Engine, this test boots a real, separate Game Engine
 * Service instance on a random port and points the Game Session Service (also
 * under test on a random port) at it, then drives a full automated game
 * across real HTTP calls between the two - exactly what happens in production.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "simulation.move-delay-ms=0")
class EndToEndSimulationTest {

    // simulate() normally hands off to this executor and returns immediately;
    // replaced here so the assertions below can observe the finished game
    // straight from the HTTP response, as they did before simulate() was
    // made non-blocking.
    @MockBean
    private TaskExecutor simulationTaskExecutor;

    @BeforeEach
    void runSimulationsSynchronously() {
        doAnswer(invocation -> {
            invocation.getArgument(0, Runnable.class).run();
            return null;
        }).when(simulationTaskExecutor).execute(any());
    }

    private static ConfigurableApplicationContext engineContext;

    @DynamicPropertySource
    static void gameEngineBaseUrl(DynamicPropertyRegistry registry) {
        engineContext = new SpringApplicationBuilder(GameEngineApplication.class)
                .initializers(new ServerPortInfoApplicationContextInitializer())
                .properties("server.port=0", "spring.main.banner-mode=off")
                .run();
        String port = engineContext.getEnvironment().getProperty("local.server.port");
        registry.add("game-engine.base-url", () -> "http://localhost:" + port);
    }

    @AfterAll
    static void stopEngine() {
        if (engineContext != null) {
            engineContext.close();
        }
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void playsAFullAutomatedGameAcrossBothRealServices() {
        ResponseEntity<SessionResponse> created =
                restTemplate.postForEntity("/sessions", null, SessionResponse.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String sessionId = created.getBody().sessionId();

        ResponseEntity<SessionResponse> simulated = restTemplate.postForEntity(
                "/sessions/{sessionId}/simulate", null, SessionResponse.class, sessionId);

        assertThat(simulated.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        SessionResponse result = simulated.getBody();
        assertThat(result).isNotNull();
        assertThat(result.status()).isIn("WIN", "DRAW");
        assertThat(result.moves()).isNotEmpty();
        assertThat(result.moves()).hasSizeLessThanOrEqualTo(9);

        ResponseEntity<SessionResponse> fetched = restTemplate.getForEntity(
                "/sessions/{sessionId}", SessionResponse.class, sessionId);

        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fetched.getBody().status()).isEqualTo(result.status());
        assertThat(fetched.getBody().moves()).isEqualTo(result.moves());
    }
}
