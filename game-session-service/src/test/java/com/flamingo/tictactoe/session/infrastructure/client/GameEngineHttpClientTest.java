package com.flamingo.tictactoe.session.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.flamingo.tictactoe.session.application.port.out.EngineGameState;
import com.flamingo.tictactoe.session.domain.exception.GameEngineCommunicationException;
import com.flamingo.tictactoe.session.domain.model.SessionStatus;
import com.flamingo.tictactoe.session.domain.model.Symbol;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/**
 * Verifies the HTTP adapter in isolation: request shape sent to the Game
 * Engine and response/error translation, without a real network call.
 */
class GameEngineHttpClientTest {

    @Test
    void initializeGameSendsAPutAndParsesTheResponse() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://game-engine");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GameEngineHttpClient client = new GameEngineHttpClient(builder.build());

        server.expect(requestTo("http://game-engine/games/g1"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess(
                        "{\"gameId\":\"g1\",\"board\":[[null,null,null],[null,null,null],[null,null,null]],\"status\":\"IN_PROGRESS\",\"winner\":null}",
                        MediaType.APPLICATION_JSON));

        EngineGameState state = client.initializeGame("g1");

        assertThat(state.status()).isEqualTo(SessionStatus.IN_PROGRESS);
        assertThat(state.winner()).isNull();
        server.verify();
    }

    @Test
    void submitMoveSendsAPostWithTheMoveBodyAndParsesAWin() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://game-engine");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GameEngineHttpClient client = new GameEngineHttpClient(builder.build());

        server.expect(requestTo("http://game-engine/games/g1/move"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        "{\"gameId\":\"g1\",\"board\":[[\"X\",null,null],[null,null,null],[null,null,null]],\"status\":\"WIN\",\"winner\":\"X\"}",
                        MediaType.APPLICATION_JSON));

        EngineGameState state = client.submitMove("g1", Symbol.X, 0, 0);

        assertThat(state.status()).isEqualTo(SessionStatus.WIN);
        assertThat(state.winner()).isEqualTo(Symbol.X);
        server.verify();
    }

    @Test
    void translatesAServerErrorIntoACommunicationException() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://game-engine");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GameEngineHttpClient client = new GameEngineHttpClient(builder.build());

        server.expect(requestTo("http://game-engine/games/g1/move"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.submitMove("g1", Symbol.X, 0, 0))
                .isInstanceOf(GameEngineCommunicationException.class);
    }
}
