// Builds the board grid from a session's move history, since the backend
// only sends a list of moves, not a ready-made board. Board size can be
// changed on the backend, so it's passed in here instead of always being 3.

export const DEFAULT_BOARD_SIZE = 3

export function buildBoardFromMoves(moves, boardSize = DEFAULT_BOARD_SIZE) {
  const board = Array.from({ length: boardSize }, () => Array(boardSize).fill(null))
  for (const move of moves) {
    board[move.row][move.col] = move.symbol
  }
  return board
}

// Finds which row, column, or diagonal is completely filled with `winner`,
// so we know where to draw the strike-through line. Works for any board
// size, not just 3, same as the backend's win check.
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
