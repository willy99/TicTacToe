// Reconstructs the board grid from a session's move history, since the Game
// Session Service reports moves rather than a pre-rendered board. Board size
// is configurable server-side (see game-session-service's game.board.size),
// so it's a parameter here rather than a hardcoded 3.

export const DEFAULT_BOARD_SIZE = 3

export function buildBoardFromMoves(moves, boardSize = DEFAULT_BOARD_SIZE) {
  const board = Array.from({ length: boardSize }, () => Array(boardSize).fill(null))
  for (const move of moves) {
    board[move.row][move.col] = move.symbol
  }
  return board
}
