import { FLOW_STEPS, NODES_BY_CONNECTOR } from '../flow/flowSteps'
import './RequestFlowDiagram.css'

const ICONS = {
  browser: (
    <svg viewBox="0 0 32 32" width="22" height="22" fill="none" stroke="currentColor" strokeWidth="2">
      <rect x="3" y="6" width="26" height="20" rx="3" />
      <line x1="3" y1="12" x2="29" y2="12" />
      <circle cx="7.5" cy="9" r="1" fill="currentColor" stroke="none" />
      <circle cx="11" cy="9" r="1" fill="currentColor" stroke="none" />
      <circle cx="14.5" cy="9" r="1" fill="currentColor" stroke="none" />
    </svg>
  ),
  server: (
    <svg viewBox="0 0 32 32" width="22" height="22" fill="none" stroke="currentColor" strokeWidth="2">
      <rect x="4" y="4" width="24" height="9" rx="2" />
      <rect x="4" y="19" width="24" height="9" rx="2" />
      <circle cx="9" cy="8.5" r="1.1" fill="currentColor" stroke="none" />
      <circle cx="9" cy="23.5" r="1.1" fill="currentColor" stroke="none" />
    </svg>
  ),
  // A tic-tac-toe grid rather than a generic server - the engine is the one
  // component that actually owns the board.
  grid: (
    <svg viewBox="0 0 32 32" width="22" height="22" fill="none" stroke="currentColor" strokeWidth="2">
      <rect x="4" y="4" width="24" height="24" rx="3" />
      <line x1="12" y1="4" x2="12" y2="28" />
      <line x1="20" y1="4" x2="20" y2="28" />
      <line x1="4" y1="12" x2="28" y2="12" />
      <line x1="4" y1="20" x2="28" y2="20" />
      <path d="M7 8 L11 12 M11 8 L7 12" strokeWidth="1.6" />
      <circle cx="24" cy="24" r="2.4" strokeWidth="1.6" />
    </svg>
  ),
  database: (
    <svg viewBox="0 0 32 32" width="18" height="18" fill="none" stroke="currentColor" strokeWidth="2">
      <ellipse cx="16" cy="7" rx="11" ry="4" />
      <path d="M5 7 L5 25 C5 27.2 9.9 29 16 29 C22.1 29 27 27.2 27 25 L27 7" />
      <path d="M5 16 C5 18.2 9.9 20 16 20 C22.1 20 27 18.2 27 16" />
    </svg>
  ),
}

function FlowNode({ icon, title, subtitle, active, compact }) {
  return (
    <div className={`flow__node${active ? ' flow__node--active' : ''}${compact ? ' flow__node--compact' : ''}`}>
      <div className="flow__node-icon">{ICONS[icon]}</div>
      <div className="flow__node-title">{title}</div>
      <div className="flow__node-subtitle">{subtitle}</div>
    </div>
  )
}

// Vertical (session/engine -> their repository) is the base geometry this
// component renders - see RequestFlowDiagram.css. Passing `horizontal`
// opts a connector out of that on wide screens, for the three main
// services sitting in a row instead of a column.
function FlowConnector({ eventId, pulses, connected, horizontal }) {
  return (
    <div
      className={`flow__connector${horizontal ? ' flow__connector--horizontal' : ''}${connected ? ' flow__connector--connected' : ''}`}
    >
      <span className="flow__connector-line" />
      {pulses.map((pulse, index) => (
        // Keyed on the event id (not just the index) so a repeated event of
        // the same kind - every move looks the same - still remounts this
        // element and replays the animation instead of no-opping.
        <span
          key={`${eventId}-${index}`}
          className={`flow__pulse flow__pulse--${pulse.direction}`}
          style={{ animationDelay: `${pulse.delayMs}ms` }}
        >
          <span className="flow__pulse-dot" />
          <span className="flow__pulse-label">{pulse.label}</span>
        </span>
      ))}
    </div>
  )
}

/**
 * Shows the five things actually involved in playing a game - the browser,
 * the two services, and the in-memory repository each service keeps its
 * state in - and animates a pulse along the right connector for whatever
 * `current` event is playing. `current` comes from useRequestFlow, which is
 * fed real fetch/EventSource events from App.jsx: what you see here is what
 * actually happened on the network (and in each service's own storage), in
 * the order it happened, just queued and paced so it's watchable instead of
 * flashing by at network speed.
 */
export default function RequestFlowDiagram({ current, streamConnected }) {
  const steps = FLOW_STEPS[current.type] ?? []
  const activeNodes = new Set(steps.flatMap((step) => NODES_BY_CONNECTOR[step.connector]))

  const pulsesFor = (connector) => steps.filter((step) => step.connector === connector)

  return (
    <section className="flow">
      <h2>Request Flow</h2>
      {current.type === 'idle' ? (
        <p className="flow__idle">Press &quot;Start Simulation&quot; to watch the requests happen.</p>
      ) : null}
      <div className="flow__grid">
        <div className="flow__grid-browser">
          <FlowNode icon="browser" title="Browser (UI)" subtitle="React · :5173" active={activeNodes.has('browser')} />
        </div>
        <div className="flow__grid-conn-h1">
          <FlowConnector eventId={current.id} pulses={pulsesFor(1)} connected={streamConnected} horizontal />
        </div>
        <div className="flow__grid-session">
          <FlowNode icon="server" title="Session Service" subtitle=":8082" active={activeNodes.has('session')} />
        </div>
        <div className="flow__grid-conn-h2">
          <FlowConnector eventId={current.id} pulses={pulsesFor(2)} horizontal />
        </div>
        <div className="flow__grid-engine">
          <FlowNode icon="grid" title="Engine Service" subtitle=":8081" active={activeNodes.has('engine')} />
        </div>

        <div className="flow__grid-conn-v1">
          <FlowConnector eventId={current.id} pulses={pulsesFor(3)} />
        </div>
        <div className="flow__grid-conn-v2">
          <FlowConnector eventId={current.id} pulses={pulsesFor(4)} />
        </div>

        <div className="flow__grid-session-repo">
          <FlowNode
            icon="database"
            title="Session Repository"
            subtitle="in-memory"
            active={activeNodes.has('sessionRepo')}
            compact
          />
        </div>
        <div className="flow__grid-engine-repo">
          <FlowNode
            icon="database"
            title="Game Repository"
            subtitle="in-memory"
            active={activeNodes.has('engineRepo')}
            compact
          />
        </div>
      </div>
    </section>
  )
}
