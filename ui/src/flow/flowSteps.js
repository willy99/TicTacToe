// How long one pulse takes to travel its connector - must match the CSS
// animation-duration in RequestFlowDiagram.css.
export const PULSE_MS = 600

// Maps each real thing that can happen to the connector pulses it should
// draw, in the order it actually happens on the backend:
//   connector 1: Browser <-> Session Service
//   connector 2: Session Service <-> Engine Service
//   connector 3: Session Service -> Session Repository (in-memory)
//   connector 4: Engine Service -> Game Repository (in-memory)
// delayMs staggers pulses on the same event so a multi-hop request - e.g. a
// move: browser asks session, session asks engine, engine saves the board,
// engine answers, session saves the session, session pushes the update back
// over SSE - reads left to right (and top to bottom) in the order it really
// happens, not all at once. Tuned to roughly fit inside one
// simulation.move-delay-ms window, so the diagram keeps pace with the board
// instead of trailing further behind with every move.
export const FLOW_STEPS = {
  'create-session': [{ connector: 1, direction: 'forward', label: 'POST /sessions', delayMs: 0 }],
  'session-created': [
    { connector: 2, direction: 'forward', label: 'PUT /games/:id', delayMs: 0 },
    { connector: 4, direction: 'forward', label: 'GameRepository.save()', delayMs: 500 },
    { connector: 2, direction: 'backward', label: '200 OK', delayMs: 950 },
    { connector: 3, direction: 'forward', label: 'SessionRepository.save()', delayMs: 1250 },
    { connector: 1, direction: 'backward', label: '201 Created', delayMs: 1600 },
  ],
  'simulate-request': [
    { connector: 1, direction: 'forward', label: 'POST .../simulate', delayMs: 0 },
    { connector: 1, direction: 'backward', label: '202 Accepted', delayMs: 400 },
  ],
  'stream-connected': [{ connector: 1, direction: 'backward', label: 'SSE connected', delayMs: 0 }],
  move: [
    { connector: 2, direction: 'forward', label: 'POST .../move', delayMs: 0 },
    { connector: 4, direction: 'forward', label: 'GameRepository.save()', delayMs: 500 },
    { connector: 2, direction: 'backward', label: '200 OK', delayMs: 950 },
    { connector: 3, direction: 'forward', label: 'SessionRepository.save()', delayMs: 1250 },
    { connector: 1, direction: 'backward', label: 'SSE: session-update', delayMs: 1600 },
  ],
  finished: [{ connector: 1, direction: 'backward', label: 'SSE: final state', delayMs: 0 }],
}

export const NODES_BY_CONNECTOR = {
  1: ['browser', 'session'],
  2: ['session', 'engine'],
  3: ['session', 'sessionRepo'],
  4: ['engine', 'engineRepo'],
}

// How long a queued event needs on screen before the next one can start -
// the last pulse's delay plus the time it takes to travel.
export function durationOf(type) {
  const steps = FLOW_STEPS[type]
  if (!steps || steps.length === 0) {
    return 0
  }
  return Math.max(...steps.map((step) => step.delayMs)) + PULSE_MS
}
