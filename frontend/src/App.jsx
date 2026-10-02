import { useEffect, useState } from 'react'

const API = 'http://localhost:8080/api/logs/search'

function App() {
  const [q, setQ] = useState('')
  const [level, setLevel] = useState('')
  const [service, setService] = useState('')
  const [minResponseTime, setMinResponseTime] = useState('')
  const [maxResponseTime, setMaxResponseTime] = useState('')
  const [results, setResults] = useState([])
  const [totalHits, setTotalHits] = useState(0)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const searchLogs = async () => {
    setLoading(true)
    setError('')

    const params = new URLSearchParams()
    if (q) params.set('q', q)
    if (level) params.set('level', level)
    if (service) params.set('service', service)
    if (minResponseTime) params.set('minResponseTime', minResponseTime)
    if (maxResponseTime) params.set('maxResponseTime', maxResponseTime)
    params.set('page', '0')
    params.set('pageSize', '20')

    try {
      const response = await fetch(`${API}?${params.toString()}`)
      if (!response.ok) throw new Error('Backend request failed')
      const data = await response.json()
      setResults(data.results)
      setTotalHits(data.totalHits)
    } catch (e) {
      setError('Could not connect to LogStream backend. Start Spring Boot on port 8080.')
      setResults([])
      setTotalHits(0)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    searchLogs()
  }, [])

  return (
    <div className="app">
      <header>
        <div>
          <h1>LogStream</h1>
          <p>Distributed Log Analytics Dashboard</p>
        </div>
        <span className="status">Week 1–2</span>
      </header>

      <main>
        <section className="card search-card">
          <h2>Search Logs</h2>

          <div className="grid">
            <label>
              Keyword
              <input
                value={q}
                onChange={e => setQ(e.target.value)}
                placeholder="database timeout"
              />
            </label>

            <label>
              Level
              <select value={level} onChange={e => setLevel(e.target.value)}>
                <option value="">All levels</option>
                <option value="ERROR">ERROR</option>
                <option value="WARN">WARN</option>
                <option value="INFO">INFO</option>
                <option value="DEBUG">DEBUG</option>
              </select>
            </label>

            <label>
              Service
              <input
                value={service}
                onChange={e => setService(e.target.value)}
                placeholder="payment-service"
              />
            </label>

            <label>
              Min response (ms)
              <input
                type="number"
                value={minResponseTime}
                onChange={e => setMinResponseTime(e.target.value)}
                placeholder="1000"
              />
            </label>

            <label>
              Max response (ms)
              <input
                type="number"
                value={maxResponseTime}
                onChange={e => setMaxResponseTime(e.target.value)}
                placeholder="5000"
              />
            </label>
          </div>

          <button onClick={searchLogs} disabled={loading}>
            {loading ? 'Searching...' : 'Search Logs'}
          </button>
        </section>

        {error && <div className="error">{error}</div>}

        <section className="card">
          <div className="results-header">
            <h2>Results</h2>
            <span>{totalHits} matching logs</span>
          </div>

          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Timestamp</th>
                  <th>Level</th>
                  <th>Service</th>
                  <th>Message</th>
                  <th>Response</th>
                  <th>Host</th>
                </tr>
              </thead>
              <tbody>
                {results.map(log => (
                  <tr key={log.id}>
                    <td>{log.timestamp}</td>
                    <td>
                      <span className={`level ${log.level.toLowerCase()}`}>
                        {log.level}
                      </span>
                    </td>
                    <td>{log.service}</td>
                    <td>{log.message}</td>
                    <td>{log.responseTimeMs} ms</td>
                    <td>{log.host || '-'}</td>
                  </tr>
                ))}
                {results.length === 0 && (
                  <tr>
                    <td colSpan="6" className="empty">
                      No matching logs
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </section>
      </main>
    </div>
  )
}

export default App
