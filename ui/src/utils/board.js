// Reconstructs the 3x3 board grid from a session's move history, since the
// Game Session Service reports moves rather than a pre-rendered board.

export const BOARD_SIZE = 3

export function buildBoardFromMoves(moves) {
  const board = Array.from({ length: BOARD_SIZE }, () => Array(BOARD_SIZE).fill(null))
  for (const move of moves) {
    board[move.row][move.col] = move.symbol
  }
  return board
}
