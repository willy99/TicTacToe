import { useCallback, useRef, useState } from 'react'
import { durationOf } from '../flow/flowSteps'

// Real events (a request firing, a response landing, a move arriving over
// the stream) can happen faster than a human can watch a pulse travel - a
// local round trip is a few milliseconds, the animation is over half a
// second - and two emit() calls can even land in the same React render
// batch, which would silently drop whichever one isn't last. So events go
// into a plain queue (a ref, not React state) the moment they happen, and a
// self-scheduling timer drains it one at a time, each shown for its full
// duration, instead of the display jumping straight to whatever's most
// recent. Capped so a long game doesn't leave the diagram narrating long
// after the board itself has finished - "finished" always clears the
// backlog and jumps the queue, since there's no point still catching up on
// old moves once the game is over.
const MAX_QUEUE_LENGTH = 3

export function useRequestFlow() {
  const [current, setCurrent] = useState({ type: 'idle', id: 0 })
  const queueRef = useRef([])
  const nextIdRef = useRef(1)
  const playingRef = useRef(false)

  const playNext = useCallback(() => {
    const next = queueRef.current.shift()
    if (!next) {
      playingRef.current = false
      return
    }
    playingRef.current = true
    setCurrent(next)
    setTimeout(playNext, durationOf(next.type))
  }, [])

  const emit = useCallback(
    (type) => {
      const event = { type, id: nextIdRef.current++ }
      if (type === 'finished') {
        queueRef.current = [event]
      } else if (queueRef.current.length < MAX_QUEUE_LENGTH) {
        queueRef.current.push(event)
      } else {
        return
      }
      if (!playingRef.current) {
        playNext()
      }
    },
    [playNext],
  )

  return { current, emit }
}
