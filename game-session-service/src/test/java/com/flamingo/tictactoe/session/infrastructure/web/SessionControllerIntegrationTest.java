package com.flamingo.tictactoe.session.infrastructure.web;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flamingo.tictactoe.session.application.port.out.EngineGameState;
import com.flamingo.tictactoe.session.application.port.out.GameEngineClient;
import com.flamingo.tictactoe.session.domain.model.SessionStatus;
import com.flamingo.tictactoe.session.domain.model.Symbol;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
class SessionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GameEngineClient gameEngineClient;

    @Test
    void createSessionInitializesTheGameEngineAndReturnsANewSession() throws Exception {
        when(gameEngineClient.initializeGame(anyString()))
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
        when(gameEngineClient.initializeGame(anyString()))
                .thenReturn(new EngineGameState(SessionStatus.IN_PROGRESS, null));
        when(gameEngineClient.submitMove(anyString(), any(Symbol.class), anyInt(), anyInt()))
                .thenReturn(new EngineGameState(SessionStatus.IN_PROGRESS, null))
                .thenReturn(new EngineGameState(SessionStatus.IN_PROGRESS, null))
                .thenReturn(new EngineGameState(SessionStatus.IN_PROGRESS, null))
                .thenReturn(new EngineGameState(SessionStatus.IN_PROGRESS, null))
                .thenReturn(new EngineGameState(SessionStatus.WIN, Symbol.X));

        String sessionId = createSession();

        mockMvc.perform(post("/sessions/{sessionId}/simulate", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("WIN")))
                .andExpect(jsonPath("$.winner", is("X")))
                .andExpect(jsonPath("$.moves.length()", is(5)));

        mockMvc.perform(get("/sessions/{sessionId}", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("WIN")));
    }

    @Test
    void simulatingAgainAfterCompletionReturns409() throws Exception {
        when(gameEngineClient.initializeGame(anyString()))
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

        mockMvc.perform(post("/sessions/{sessionId}/simulate", sessionId)).andExpect(status().isOk());

        mockMvc.perform(post("/sessions/{sessionId}/simulate", sessionId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title", is("Session Already Completed")));
    }

    @Test
    void whenTheGameEngineIsUnreachableTheSessionServiceReturns502() throws Exception {
        when(gameEngineClient.initializeGame(anyString()))
                .thenReturn(new EngineGameState(SessionStatus.IN_PROGRESS, null));
        when(gameEngineClient.submitMove(anyString(), any(Symbol.class), anyInt(), anyInt()))
                .thenThrow(new com.flamingo.tictactoe.session.domain.exception.GameEngineCommunicationException(
                        "Unable to reach the Game Engine Service", null));

        String sessionId = createSession();

        mockMvc.perform(post("/sessions/{sessionId}/simulate", sessionId))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.title", is("Game Engine Communication Error")));
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
