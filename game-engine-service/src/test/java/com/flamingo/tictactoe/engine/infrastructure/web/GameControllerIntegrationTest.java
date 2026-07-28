package com.flamingo.tictactoe.engine.infrastructure.web;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Full-stack integration tests: real Spring context, real controller, real
 * in-memory repository, HTTP request/response cycle through MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
class GameControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void initializingAGameTwiceIsIdempotent() throws Exception {
        mockMvc.perform(put("/games/{gameId}", "idempotent-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));

        mockMvc.perform(put("/games/{gameId}", "idempotent-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));
    }

    @Test
    void fetchingAnUnknownGameReturns404() throws Exception {
        mockMvc.perform(get("/games/{gameId}", "does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title", is("Game Not Found")));
    }

    @Test
    void playsARealisticGameToACompleteWin() throws Exception {
        String gameId = "win-flow";
        mockMvc.perform(put("/games/{gameId}", gameId)).andExpect(status().isOk());

        performMove(gameId, "X", 0, 0).andExpect(status().isOk());
        performMove(gameId, "O", 1, 0).andExpect(status().isOk());
        performMove(gameId, "X", 0, 1).andExpect(status().isOk());
        performMove(gameId, "O", 1, 1).andExpect(status().isOk());
        performMove(gameId, "X", 0, 2)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("WIN")))
                .andExpect(jsonPath("$.winner", is("X")));

        mockMvc.perform(get("/games/{gameId}", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("WIN")));
    }

    @Test
    void rejectsAMoveOnAnOccupiedCell() throws Exception {
        String gameId = "occupied-flow";
        mockMvc.perform(put("/games/{gameId}", gameId)).andExpect(status().isOk());

        performMove(gameId, "X", 0, 0).andExpect(status().isOk());
        performMove(gameId, "O", 0, 0)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Invalid Move")));
    }

    @Test
    void rejectsAMoveAfterTheGameHasFinished() throws Exception {
        String gameId = "finished-flow";
        mockMvc.perform(put("/games/{gameId}", gameId)).andExpect(status().isOk());

        performMove(gameId, "X", 0, 0).andExpect(status().isOk());
        performMove(gameId, "O", 1, 0).andExpect(status().isOk());
        performMove(gameId, "X", 0, 1).andExpect(status().isOk());
        performMove(gameId, "O", 1, 1).andExpect(status().isOk());
        performMove(gameId, "X", 0, 2).andExpect(status().isOk()); // X wins

        performMove(gameId, "O", 2, 2)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title", is("Game Already Finished")));
    }

    @Test
    void rejectsAMalformedMoveRequest() throws Exception {
        String gameId = "validation-flow";
        mockMvc.perform(put("/games/{gameId}", gameId)).andExpect(status().isOk());

        mockMvc.perform(post("/games/{gameId}/move", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symbol\":\"Z\",\"row\":0,\"col\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Validation Error")));
    }

    @Test
    void movingOnANonExistentGameReturns404() throws Exception {
        performMove("never-created", "X", 0, 0)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title", is("Game Not Found")));
    }

    @Test
    void aFullBoardWithNoWinnerIsADraw() throws Exception {
        String gameId = "draw-flow";
        mockMvc.perform(put("/games/{gameId}", gameId)).andExpect(status().isOk());

        // X O X
        // X O O
        // O X X
        performMove(gameId, "X", 0, 0).andExpect(status().isOk());
        performMove(gameId, "O", 0, 1).andExpect(status().isOk());
        performMove(gameId, "X", 0, 2).andExpect(status().isOk());
        performMove(gameId, "O", 1, 1).andExpect(status().isOk());
        performMove(gameId, "X", 1, 0).andExpect(status().isOk());
        performMove(gameId, "O", 1, 2).andExpect(status().isOk());
        performMove(gameId, "X", 2, 1).andExpect(status().isOk());
        performMove(gameId, "O", 2, 0).andExpect(status().isOk());
        performMove(gameId, "X", 2, 2)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("DRAW")))
                .andExpect(jsonPath("$.winner", nullValue()));
    }

    private org.springframework.test.web.servlet.ResultActions performMove(
            String gameId, String symbol, int row, int col) throws Exception {
        String body = objectMapper.writeValueAsString(new MoveRequestBody(symbol, row, col));
        return mockMvc.perform(post("/games/{gameId}/move", gameId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private record MoveRequestBody(String symbol, int row, int col) {
    }
}
