import { buildBoardFromMoves, findWinningLine, DEFAULT_BOARD_SIZE } from '../utils/board'
import './Board.css'

export default function Board({ moves, boardSize = DEFAULT_BOARD_SIZE, winner }) {
  const cells = buildBoardFromMoves(moves, boardSize)
  const winningLine = findWinningLine(cells, winner)

  return (
    <div
      className="board"
      role="grid"
      aria-label="Tic Tac Toe board"
      style={{ '--board-size': boardSize }}
    >
      {/* Cells get a fixed grid position instead of letting the browser
          place them automatically. The win-line below also has a fixed
          position, and the browser never lets an automatically-placed
          cell overlap one with a fixed position - it just pushes it into
          a new row instead. Giving both a fixed position lets them share
          the same cells on purpose. */}
      {cells.map((row, rowIndex) =>
        row.map((symbol, colIndex) => (
          <div
            key={`${rowIndex}-${colIndex}`}
            role="gridcell"
            className={`board__cell${symbol ? ` board__cell--${symbol.toLowerCase()}` : ''}`}
            style={{ gridRow: rowIndex + 1, gridColumn: colIndex + 1 }}
          >
            {symbol}
          </div>
        )),
      )}
      {winningLine && (
        <div
          className={`board__win-line`}
          style={winLineGridArea(winningLine)}
          aria-hidden="true"
        >
          <span
            className={`board__win-line-bar board__win-line-bar--${winningLine.type} board__win-line-bar--${winner.toLowerCase()}`}
          />
        </div>
      )}
    </div>
  )
}

// The win-line sits in the same grid as the cells, placed to cover exactly
// the winning row/column/diagonal - so its position and size come from the
// grid itself instead of manually calculated pixels.
function winLineGridArea({ type, index }) {
  switch (type) {
    case 'row':
      return { gridRow: `${index + 1} / span 1`, gridColumn: '1 / -1' }
    case 'col':
      return { gridColumn: `${index + 1} / span 1`, gridRow: '1 / -1' }
    default:
      return { gridRow: '1 / -1', gridColumn: '1 / -1' }
  }
}
