import { useCallback, useEffect, useRef, useState } from 'react'
import Board from './components/Board'
import StatusPanel from './components/StatusPanel'
import MoveHistory from './components/MoveHistory'
import ErrorBanner from './components/ErrorBanner'
import { createSession, getSession, simulateSession } from './api/sessionApi'
import './App.css'

const POLL_INTERVAL_MS = 500

export default function App() {
  const [session, setSession] = useState(null)
  const [isRunning, setIsRunning] = useState(false)
  const [error, setError] = useState('')
  const pollHandleRef = useRef(null)

  const stopPolling = useCallback(() => {
    if (pollHandleRef.current) {
      clearInterval(pollHandleRef.current)
      pollHandleRef.current = null
    }
  }, [])

  useEffect(() => stopPolling, [stopPolling])

  const pollSessionUntilFinished = useCallback(
    (sessionId) => {
      stopPolling()
      pollHandleRef.current = setInterval(async () => {
        try {
          const latest = await getSession(sessionId)
          setSession(latest)
          if (latest.status !== 'IN_PROGRESS') {
            stopPolling()
            setIsRunning(false)
          }
        } catch (err) {
          stopPolling()
          setIsRunning(false)
          setError(err.message)
        }
      }, POLL_INTERVAL_MS)
    },
    [stopPolling],
  )

  const handleStart = useCallback(async () => {
    setError('')
    setSession(null)
    setIsRunning(true)
    try {
      const created = await createSession()
      setSession(created)
      // simulate() kicks off the automated game on the server and returns
      // immediately - it doesn't wait for the game to finish. The poller
      // above is what actually renders progress, by re-fetching the session
      // as moves land, so we don't need to await simulateSession() before
      // updating the UI.
      pollSessionUntilFinished(created.sessionId)
      await simulateSession(created.sessionId)
    } catch (err) {
      stopPolling()
      setIsRunning(false)
      setError(err.message)
    }
  }, [pollSessionUntilFinished, stopPolling])

  const moves = session?.moves ?? []

  return (
    <main className="app">
      <h1>Distributed Tic Tac Toe</h1>
      <p className="app__subtitle">
        Two automated players, coordinated by the Game Session Service, play against
        each other through the Game Engine Service.
      </p>

      <ErrorBanner message={error} onDismiss={() => setError('')} />

      <button type="button" className="start-button" onClick={handleStart} disabled={isRunning}>
        {isRunning ? 'Simulating...' : 'Start Simulation'}
      </button>

      <section className="game">
        <div className="game__board-column">
          <Board moves={moves} boardSize={session?.boardSize} />
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
