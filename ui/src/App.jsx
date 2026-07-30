import { useCallback, useEffect, useRef, useState } from 'react'
import Board from './components/Board'
import StatusPanel from './components/StatusPanel'
import MoveHistory from './components/MoveHistory'
import ErrorBanner from './components/ErrorBanner'
import RequestFlowDiagram from './components/RequestFlowDiagram'
import { useRequestFlow } from './hooks/useRequestFlow'
import { createSession, simulateSession, sessionStreamUrl } from './api/sessionApi'
import './App.css'

export default function App() {
  const [session, setSession] = useState(null)
  const [isRunning, setIsRunning] = useState(false)
  const [error, setError] = useState('')
  const [streamConnected, setStreamConnected] = useState(false)
  const { current: currentFlowStep, emit: emitFlow } = useRequestFlow()
  const eventSourceRef = useRef(null)
  const moveCountRef = useRef(0)
  const receivedFirstMessageRef = useRef(false)

  const stopStreaming = useCallback(() => {
    if (eventSourceRef.current) {
      eventSourceRef.current.close()
      eventSourceRef.current = null
    }
    setStreamConnected(false)
  }, [])

  useEffect(() => stopStreaming, [stopStreaming])

  // Opens a live feed of the session instead of polling for it: the server
  // pushes the current state right away, then again after every move, so
  // this just has to listen instead of asking on a timer.
  const streamSession = useCallback(
    (sessionId) => {
      stopStreaming()
      moveCountRef.current = 0
      receivedFirstMessageRef.current = false
      const source = new EventSource(sessionStreamUrl(sessionId))
      eventSourceRef.current = source

      source.onmessage = (event) => {
        const latest = JSON.parse(event.data)
        setSession(latest)

        if (!receivedFirstMessageRef.current) {
          receivedFirstMessageRef.current = true
          setStreamConnected(true)
          emitFlow('stream-connected')
        } else if (latest.moves.length > moveCountRef.current) {
          emitFlow('move')
        }
        moveCountRef.current = latest.moves.length

        if (latest.status !== 'IN_PROGRESS') {
          emitFlow('finished')
          stopStreaming()
          setIsRunning(false)
        }
      }

      source.onerror = () => {
        stopStreaming()
        setIsRunning(false)
        setError('Lost connection while watching the game.')
      }
    },
    [stopStreaming, emitFlow],
  )

  const handleStart = useCallback(async () => {
    setError('')
    setSession(null)
    setIsRunning(true)
    try {
      emitFlow('create-session')
      const created = await createSession()
      setSession(created)
      emitFlow('session-created')
      // simulate() starts the automated game on the server and returns
      // right away - it doesn't wait for the game to finish. The stream
      // opened above is what actually shows progress, so we don't need to
      // wait for simulateSession() before updating the screen.
      streamSession(created.sessionId)
      emitFlow('simulate-request')
      await simulateSession(created.sessionId)
    } catch (err) {
      stopStreaming()
      setIsRunning(false)
      setError(err.message)
    }
  }, [streamSession, stopStreaming, emitFlow])

  const moves = session?.moves ?? []

  return (
    <main className="app">
      <h1>Tic Tac Toe</h1>

      <ErrorBanner message={error} onDismiss={() => setError('')} />

      <button type="button" className="start-button" onClick={handleStart} disabled={isRunning}>
        {isRunning ? 'Simulating...' : 'Start Simulation'}
      </button>

      <RequestFlowDiagram current={currentFlowStep} streamConnected={streamConnected} />

      <section className="game">
        <div className="game__board-column">
          <Board moves={moves} boardSize={session?.boardSize} winner={session?.winner} />
          <StatusPanel
            status={session?.status}
            winner={session?.winner}
            failureReason={session?.failureReason}
          />
        </div>
        <div className="game__history">
          <h2>Move History</h2>
          <MoveHistory moves={moves} />
        </div>
      </section>
    </main>
  )
}
