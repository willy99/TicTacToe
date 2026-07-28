package com.flamingo.tictactoe.session.domain.model;

/**
 * The two marks a player can place on the board. Deliberately its own type
 * (not shared with the Game Engine Service) so the two services remain
 * independently deployable bounded contexts with their own contracts.
 */
public enum Symbol {
    X,
    O
}
