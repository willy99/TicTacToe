import { buildBoardFromMoves } from '../utils/board'
import './Board.css'

export default function Board({ moves }) {
  const cells = buildBoardFromMoves(moves)

  return (
    <div className="board" role="grid" aria-label="Tic Tac Toe board">
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
