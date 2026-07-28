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
 * {@link GameEngineClient} adapter that talks to the Game Engine Service over
 * HTTP using Spring's {@link RestClient}. Any transport or remote error is
 * translated into a single domain-level {@link GameEngineCommunicationException}
 * so the application layer never has to know about HTTP.
 */
@Component
public class GameEngineHttpClient implements GameEngineClient {

    private final RestClient restClient;

    public GameEngineHttpClient(RestClient gameEngineRestClient) {
        this.restClient = gameEngineRestClient;
    }

    @Override
    public EngineGameState initializeGame(String gameId) {
        return call(() -> restClient.put()
                .uri("/games/{gameId}", gameId)
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
        }
    }

    private EngineGameState toEngineGameState(EngineGameResponseDto response) {
        SessionStatus status = SessionStatus.valueOf(response.status());
        Symbol winner = response.winner() == null ? null : Symbol.valueOf(response.winner());
        return new EngineGameState(status, winner);
    }
}
