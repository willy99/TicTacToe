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
      {/* Cells get an explicit grid position rather than relying on
          auto-placement: the win-line below is also explicitly placed, and
          an auto-placed item is never allowed to overlap an explicitly
          placed one - it gets pushed into a new row instead. Placing both
          explicitly lets them intentionally share the same cells. */}
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

// The line lives in the same CSS grid as the cells, explicitly placed to
// span the winning row/column/diagonal - so its position and length come
// from the grid's own layout instead of hand-computed pixel math.
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
