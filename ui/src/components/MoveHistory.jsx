export default function MoveHistory({ moves }) {
  if (moves.length === 0) {
    return <p className="move-history__empty">No moves played yet.</p>
  }

  return (
    <ol className="move-history">
      {moves.map((move) => (
        <li key={move.moveNumber}>
          #{move.moveNumber}: {move.symbol} &rarr; ({move.row}, {move.col})
        </li>
      ))}
    </ol>
  )
}
