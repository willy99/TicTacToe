package com.flamingo.tictactoe.session.infrastructure.web;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flamingo.tictactoe.session.application.port.out.EngineGameState;
import com.flamingo.tictactoe.session.application.port.out.GameEngineClient;
import com.flamingo.tictactoe.session.domain.exception.GameEngineCommunicationException;
import com.flamingo.tictactoe.session.domain.model.SessionStatus;
import com.flamingo.tictactoe.session.domain.model.Symbol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Full-stack integration tests for the session service in isolation: real
 * Spring context, real controller/service/repository, with the outbound
 * {@link GameEngineClient} port replaced by a mock so no real HTTP call to
 * another process is needed. The cross-service HTTP contract is separately
 * verified in {@code EndToEndSimulationTest}.
 */
@SpringBootTest
@AutoConfigureMockMvc
// No artificial pause between simulated moves - tests only care about
// correctness, not pacing. Layered on top of application.yml rather than a
// separate test application.yml, which would shadow the whole file instead
// of overriding just this one property.
@TestPropertySource(properties = "simulation.move-delay-ms=0")
class SessionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GameEngineClient gameEngineClient;

    // simulate() normally hands the game-playing loop to this executor and
    // returns immediately - see SessionService. These tests care about the
    // eventual outcome, not the async hand-off itself, so the executor is
    // replaced with one that runs the task on the calling thread: the HTTP
    // response then already reflects the finished game, same as before
    // simulate() was made non-blocking.
    @MockBean
    private TaskExecutor simulationTaskExecutor;

    @BeforeEach
    void runSimulationsSynchronously() {
        doAnswer(invocation -> {
            invocation.getArgument(0, Runnable.class).run();
            return null;
        }).when(simulationTaskExecutor).execute(any());
    }

    @Test
    void createSessionInitializesTheGameEngineAndReturnsANewSession() throws Exception {
        when(gameEngineClient.initializeGame(anyString(), anyInt()))
                .thenReturn(new EngineGameState(SessionStatus.IN_PROGRESS, null));

        mockMvc.perform(post("/sessions"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sessionId", notNullValue()))
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.moves").isEmpty());
    }

    @Test
    void fetchingAnUnknownSessionReturns404() throws Exception {
        mockMvc.perform(get("/sessions/{sessionId}", "does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title", is("Session Not Found")));
    }

    @Test
    void simulateDrivesAFullGameToAWin() throws Exception {
        when(gameEngineClient.initializeGame(anyString(), anyInt()))
                .thenReturn(new EngineGameState(SessionStatus.IN_PROGRESS, null));
        when(gameEngineClient.submitMove(anyString(), any(Symbol.class), anyInt(), anyInt()))
                .thenReturn(new EngineGameState(SessionStatus.IN_PROGRESS, null))
                .thenReturn(new EngineGameState(SessionStatus.IN_PROGRESS, null))
                .thenReturn(new EngineGameState(SessionStatus.IN_PROGRESS, null))
                .thenReturn(new EngineGameState(SessionStatus.IN_PROGRESS, null))
                .thenReturn(new EngineGameState(SessionStatus.WIN, Symbol.X));

        String sessionId = createSession();

        mockMvc.perform(post("/sessions/{sessionId}/simulate", sessionId))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status", is("WIN")))
                .andExpect(jsonPath("$.winner", is("X")))
                .andExpect(jsonPath("$.moves.length()", is(5)));

        mockMvc.perform(get("/sessions/{sessionId}", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("WIN")));
    }

    @Test
    void simulatingAgainAfterCompletionReturns409() throws Exception {
        when(gameEngineClient.initializeGame(anyString(), anyInt()))
                .thenReturn(new EngineGameState(SessionStatus.IN_PROGRESS, null));
        when(gameEngineClient.submitMove(anyString(), any(Symbol.class), anyInt(), anyInt()))
                .thenReturn(new EngineGameState(SessionStatus.DRAW, null))
                .thenReturn(new EngineGameState(SessionStatus.DRAW, null))
                .thenReturn(new EngineGameState(SessionStatus.DRAW, null))
                .thenReturn(new EngineGameState(SessionStatus.DRAW, null))
                .thenReturn(new EngineGameState(SessionStatus.DRAW, null))
                .thenReturn(new EngineGameState(SessionStatus.DRAW, null))
                .thenReturn(new EngineGameState(SessionStatus.DRAW, null))
                .thenReturn(new EngineGameState(SessionStatus.DRAW, null))
                .thenReturn(new EngineGameState(SessionStatus.DRAW, null));

        String sessionId = createSession();

        mockMvc.perform(post("/sessions/{sessionId}/simulate", sessionId)).andExpect(status().isAccepted());

        mockMvc.perform(post("/sessions/{sessionId}/simulate", sessionId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title", is("Session Already Completed")));
    }

    @Test
    void whenTheGameEngineIsUnreachableTheSessionEndsUpFailed() throws Exception {
        // The failure happens on the simulation executor, not on this
        // request's own thread, so it can no longer surface as an HTTP error
        // response for this call - only as a FAILED status once observed,
        // here immediately since the executor is stubbed to run inline.
        when(gameEngineClient.initializeGame(anyString(), anyInt()))
                .thenReturn(new EngineGameState(SessionStatus.IN_PROGRESS, null));
        when(gameEngineClient.submitMove(anyString(), any(Symbol.class), anyInt(), anyInt()))
                .thenThrow(new GameEngineCommunicationException("Unable to reach the Game Engine Service", null));

        String sessionId = createSession();

        mockMvc.perform(post("/sessions/{sessionId}/simulate", sessionId))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status", is("FAILED")))
                .andExpect(jsonPath("$.failureReason", is("Unable to reach the Game Engine Service")));

        mockMvc.perform(get("/sessions/{sessionId}", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("FAILED")));
    }

    private String createSession() throws Exception {
        String body = mockMvc.perform(post("/sessions"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode json = objectMapper.readTree(body);
        return json.get("sessionId").asText();
    }
}
