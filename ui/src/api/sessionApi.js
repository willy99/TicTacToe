// Small wrapper around the Game Session Service's REST API. Each function
// returns the parsed JSON on success, or throws an Error with a readable
// message (taken from the backend's error response) on failure.

const BASE_URL = '/sessions'

async function handleResponse(response) {
  if (response.ok) {
    return response.json()
  }

  let detail = `Request failed with status ${response.status}`
  try {
    const problem = await response.json()
    detail = problem.detail || problem.title || detail
  } catch {
    // Body wasn't JSON (e.g. the backend is unreachable and a proxy/gateway
    // returned an HTML error page) - fall back to the generic message above.
  }
  throw new Error(detail)
}

export async function createSession() {
  const response = await fetch(BASE_URL, { method: 'POST' })
  return handleResponse(response)
}

export async function simulateSession(sessionId) {
  const response = await fetch(`${BASE_URL}/${sessionId}/simulate`, { method: 'POST' })
  return handleResponse(response)
}

export async function getSession(sessionId) {
  const response = await fetch(`${BASE_URL}/${sessionId}`)
  return handleResponse(response)
}
