package com.flamingo.tictactoe.session.infrastructure.client;

import com.flamingo.tictactoe.session.application.port.out.EngineGameState;
import com.flamingo.tictactoe.session.application.port.out.GameEngineClient;
import com.flamingo.tictactoe.session.domain.exception.GameEngineCommunicationException;
import com.flamingo.tictactoe.session.domain.model.SessionStatus;
import com.flamingo.tictactoe.session.domain.model.Symbol;
import com.flamingo.tictactoe.session.infrastructure.client.dto.EngineGameResponseDto;
import com.flamingo.tictactoe.session.infrastructure.client.dto.EngineMoveRequestDto;
import java.util.function.Supplier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Talks to the Game Engine over HTTP using Spring's RestClient. Any
 * network or server error gets turned into one
 * GameEngineCommunicationException, so the rest of the code doesn't need
 * to know or care that this is HTTP underneath.
 */
@Component
public class GameEngineHttpClient implements GameEngineClient {

    private final RestClient restClient;

    public GameEngineHttpClient(RestClient gameEngineRestClient) {
        this.restClient = gameEngineRestClient;
    }

    @Override
    public EngineGameState initializeGame(String gameId, int boardSize) {
        return call(() -> restClient.put()
                .uri("/games/{gameId}?boardSize={boardSize}", gameId, boardSize)
                .retrieve()
                .body(EngineGameResponseDto.class));
    }

    @Override
    public EngineGameState submitMove(String gameId, Symbol symbol, int row, int col) {
        EngineMoveRequestDto requestBody = new EngineMoveRequestDto(symbol.name(), row, col);
        return call(() -> restClient.post()
                .uri("/games/{gameId}/move", gameId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(EngineGameResponseDto.class));
    }

    private EngineGameState call(Supplier<EngineGameResponseDto> httpCall) {
        try {
            EngineGameResponseDto response = httpCall.get();
            if (response == null) {
                throw new GameEngineCommunicationException("Game Engine returned an empty response", null);
            }
            return toEngineGameState(response);
        } catch (RestClientResponseException ex) {
            throw new GameEngineCommunicationException(
                    "Game Engine responded with status %s: %s"
                            .formatted(ex.getStatusCode(), ex.getResponseBodyAsString()),
                    ex);
        } catch (ResourceAccessException ex) {
            throw new GameEngineCommunicationException("Unable to reach the Game Engine Service", ex);
        } catch (IllegalArgumentException ex) {
            // Symbol.valueOf/SessionStatus.valueOf in toEngineGameState() throws
            // this if the response doesn't match either enum's values - e.g. the
            // two services got out of sync. Treated the same as any other
            // communication failure instead of showing up as a confusing 500.
            throw new GameEngineCommunicationException(
                    "Game Engine returned an unrecognized response: " + ex.getMessage(), ex);
        }
    }

    private EngineGameState toEngineGameState(EngineGameResponseDto response) {
        SessionStatus status = SessionStatus.valueOf(response.status());
        Symbol winner = response.winner() == null ? null : Symbol.valueOf(response.winner());
        return new EngineGameState(status, winner);
    }
}
