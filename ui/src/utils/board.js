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

// Finds which row, column, or diagonal is filled entirely with `winner`, so
// the board can draw a strike-through line over it. Generalized to any
// board size rather than assuming 3, same as the win check on the backend.
export function findWinningLine(cells, winner) {
  if (!winner) {
    return null
  }
  const size = cells.length

  for (let row = 0; row < size; row++) {
    if (cells[row].every((cell) => cell === winner)) {
      return { type: 'row', index: row }
    }
  }

  for (let col = 0; col < size; col++) {
    if (cells.every((row) => row[col] === winner)) {
      return { type: 'col', index: col }
    }
  }

  if (cells.every((row, i) => row[i] === winner)) {
    return { type: 'diagonal' }
  }

  if (cells.every((row, i) => row[size - 1 - i] === winner)) {
    return { type: 'anti-diagonal' }
  }

  return null
}
