// Thin client for the Game Session Service REST API. Every function returns
// the parsed JSON body on success and throws an Error (with a readable
// message extracted from the backend's ProblemDetail response) on failure.

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
