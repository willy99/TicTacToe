export default function MoveHistory({ moves }) {
  if (moves.length === 0) {
    return <p className="move-history__empty">No moves played yet.</p>
  }

  return (
    <ol className="move-history">
      {moves.map((move) => (
        <li key={move.moveNumber} className="move-history__row">
          <span className="move-history__index">#{move.moveNumber}</span>
          <span className={`move-history__symbol move-history__symbol--${move.symbol.toLowerCase()}`}>
            {move.symbol}
          </span>
          <span className="move-history__coords">
            ({move.row}, {move.col})
          </span>
        </li>
      ))}
    </ol>
  )
}
