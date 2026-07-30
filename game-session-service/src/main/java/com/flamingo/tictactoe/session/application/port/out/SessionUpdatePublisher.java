package com.flamingo.tictactoe.session.application.port.out;

import com.flamingo.tictactoe.session.domain.model.SessionSnapshot;

/**
 * Sends out a session's latest state whenever it changes, so a UI watching
 * that session finds out right away instead of having to poll for it.
 */
public interface SessionUpdatePublisher {

    void publish(SessionSnapshot snapshot);
}
