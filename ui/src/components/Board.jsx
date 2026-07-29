import { buildBoardFromMoves, DEFAULT_BOARD_SIZE } from '../utils/board'
import './Board.css'

export default function Board({ moves, boardSize = DEFAULT_BOARD_SIZE }) {
  const cells = buildBoardFromMoves(moves, boardSize)

  return (
    <div
      className="board"
      role="grid"
      aria-label="Tic Tac Toe board"
      style={{ '--board-size': boardSize }}
    >
      {cells.map((row, rowIndex) =>
        row.map((symbol, colIndex) => (
          <div
            key={`${rowIndex}-${colIndex}`}
            role="gridcell"
            className={`board__cell${symbol ? ` board__cell--${symbol.toLowerCase()}` : ''}`}
          >
            {symbol}
          </div>
        )),
      )}
    </div>
  )
}
