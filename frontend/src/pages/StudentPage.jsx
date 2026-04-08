import { useState } from 'react'

const API_BASE = import.meta.env.VITE_API_BASE ?? 'http://localhost:8080'

export default function StudentPage() {
  const [jobs, setJobs] = useState([])
  const [matches, setMatches] = useState([])
  const [applications, setApplications] = useState([])
  const [selectedJob, setSelectedJob] = useState(null)
  const [jobId, setJobId] = useState('')
  const [keywords, setKeywords] = useState('')
  const [resumeSummary, setResumeSummary] = useState('')
  const [message, setMessage] = useState('')

  async function loadJobs() {
    setMessage('加载岗位中...')
    try {
      const response = await fetch(`${API_BASE}/student/jobs`)
      const data = await response.json()
      if (!response.ok) throw new Error(data.message ?? '加载失败')
      setJobs(Array.isArray(data) ? data : [])
      setMessage('岗位加载成功')
    } catch (err) {
      setMessage(`加载岗位失败：${err.message}`)
    }
  }

  async function loadJobDetail() {
    if (!jobId) {
      setMessage('请输入岗位ID')
      return
    }
    setMessage('加载岗位详情中...')
    try {
      const response = await fetch(`${API_BASE}/student/jobs/${jobId}`)
      const data = await response.json()
      if (!response.ok) throw new Error(data.message ?? '加载详情失败')
      setSelectedJob(data)
      setMessage('岗位详情加载成功')
    } catch (err) {
      setSelectedJob(null)
      setMessage(`加载岗位详情失败：${err.message}`)
    }
  }

  async function loadMatches() {
    setMessage('匹配中...')
    try {
      const query = keywords.trim() ? `?keywords=${encodeURIComponent(keywords.trim())}` : ''
      const response = await fetch(`${API_BASE}/student/matches${query}`)
      const data = await response.json()
      if (!response.ok) throw new Error(data.message ?? '匹配失败')
      setMatches(Array.isArray(data) ? data : [])
      setMessage('匹配完成')
    } catch (err) {
      setMessage(`匹配失败：${err.message}`)
    }
  }

  async function applyJob(e) {
    e.preventDefault()
    if (!jobId || !resumeSummary.trim()) {
      setMessage('请填写岗位ID与简历摘要')
      return
    }
    setMessage('投递中...')
    try {
      const response = await fetch(`${API_BASE}/student/applications`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          jobId: Number(jobId),
          resumeSummary: resumeSummary.trim()
        })
      })
      const data = await response.json()
      if (!response.ok) throw new Error(data.message ?? '投递失败')
      setMessage(`投递成功，申请ID：${data.id}`)
      await loadMyApplications()
    } catch (err) {
      setMessage(`投递失败：${err.message}`)
    }
  }

  async function loadMyApplications() {
    setMessage('加载投递记录中...')
    try {
      const response = await fetch(`${API_BASE}/student/applications`)
      const data = await response.json()
      if (!response.ok) throw new Error(data.message ?? '加载投递失败')
      setApplications(Array.isArray(data) ? data : [])
      setMessage('投递记录加载成功')
    } catch (err) {
      setMessage(`加载投递失败：${err.message}`)
    }
  }

  return (
    <section>
      <h2>/student/...</h2>
      <p>岗位浏览、详情、投递、匹配与我的投递记录。</p>
      <div style={{ marginBottom: 12 }}>
        <button type="button" onClick={loadJobs}>加载岗位列表</button>
        <button type="button" onClick={loadMatches} style={{ marginLeft: 8 }}>关键词匹配</button>
        <button type="button" onClick={loadMyApplications} style={{ marginLeft: 8 }}>我的投递</button>
      </div>

      <div style={{ marginBottom: 12 }}>
        <input
          placeholder="关键词（用于匹配）"
          value={keywords}
          onChange={(e) => setKeywords(e.target.value)}
          style={{ width: '100%', padding: 6 }}
        />
      </div>

      <div style={{ marginBottom: 12 }}>
        <input
          placeholder="岗位ID（详情/投递）"
          value={jobId}
          onChange={(e) => setJobId(e.target.value)}
          style={{ width: '100%', padding: 6 }}
        />
        <button type="button" onClick={loadJobDetail} style={{ marginTop: 8 }}>查看岗位详情</button>
      </div>

      <form onSubmit={applyJob} style={{ marginBottom: 12 }}>
        <textarea
          placeholder="简历摘要"
          value={resumeSummary}
          onChange={(e) => setResumeSummary(e.target.value)}
          style={{ width: '100%', minHeight: 80, padding: 6 }}
          required
        />
        <button type="submit" style={{ marginTop: 8 }}>投递岗位</button>
      </form>

      <h3>岗位列表</h3>
      <ul>
        {jobs.map((job) => (
          <li key={job.id}>{job.id} | {job.title} | {job.skills}</li>
        ))}
      </ul>

      <h3>匹配结果</h3>
      <ul>
        {matches.map((job) => (
          <li key={job.id}>{job.id} | {job.title} | {job.skills}</li>
        ))}
      </ul>

      <h3>岗位详情</h3>
      {selectedJob ? (
        <pre style={{ whiteSpace: 'pre-wrap' }}>{JSON.stringify(selectedJob, null, 2)}</pre>
      ) : (
        <p>暂无详情</p>
      )}

      <h3>我的投递记录</h3>
      <ul>
        {applications.map((a) => (
          <li key={a.id}>申请ID {a.id} | 岗位ID {a.jobId} | 时间 {a.appliedAt}</li>
        ))}
      </ul>
      <p>{message}</p>
    </section>
  )
}
