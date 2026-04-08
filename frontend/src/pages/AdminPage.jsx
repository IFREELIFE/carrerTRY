import { useState } from 'react'

const API_BASE = import.meta.env.VITE_API_BASE ?? 'http://localhost:8080'

export default function AdminPage() {
  const [jobs, setJobs] = useState([])
  const [aiTasks, setAiTasks] = useState([])
  const [jobId, setJobId] = useState('')
  const [message, setMessage] = useState('')

  async function loadJobs() {
    setMessage('加载岗位中...')
    try {
      const response = await fetch(`${API_BASE}/admin/jobs`)
      const data = await response.json()
      if (!response.ok) throw new Error(data.message ?? '加载岗位失败')
      setJobs(Array.isArray(data) ? data : [])
      setMessage('岗位加载成功')
    } catch (err) {
      setMessage(`加载岗位失败：${err.message}`)
    }
  }

  async function loadAiTasks() {
    setMessage('加载AI任务中...')
    try {
      const response = await fetch(`${API_BASE}/admin/ai-tasks`)
      const data = await response.json()
      if (!response.ok) throw new Error(data.message ?? '加载AI任务失败')
      setAiTasks(Array.isArray(data) ? data : [])
      setMessage('AI任务加载成功')
    } catch (err) {
      setMessage(`加载AI任务失败：${err.message}`)
    }
  }

  async function reviewJob(action) {
    if (!jobId.trim()) {
      setMessage('请先输入岗位ID')
      return
    }
    const normalizedId = Number(jobId)
    if (!Number.isInteger(normalizedId) || normalizedId <= 0) {
      setMessage('岗位ID必须是正整数')
      return
    }

    setMessage(action === 'approve' ? '审核通过中...' : '审核驳回中...')
    try {
      const response = await fetch(`${API_BASE}/admin/jobs/${normalizedId}/${action}`, {
        method: 'PATCH'
      })
      const data = await response.json()
      if (!response.ok) throw new Error(data.message ?? '审核失败')
      setMessage(
        action === 'approve'
          ? `审核通过成功：岗位 ${data.id}`
          : `审核驳回成功：岗位 ${data.id}`
      )
      await loadJobs()
    } catch (err) {
      setMessage(`审核失败：${err.message}`)
    }
  }

  return (
    <section>
      <h2>/admin/...</h2>
      <p>岗位审核发布与 AI 任务监控。</p>

      <div style={{ marginBottom: 12 }}>
        <button type="button" onClick={loadJobs}>加载岗位列表</button>
        <button type="button" onClick={loadAiTasks} style={{ marginLeft: 8 }}>加载AI任务</button>
      </div>

      <div style={{ marginBottom: 12 }}>
        <input
          placeholder="岗位ID（审核通过/驳回）"
          value={jobId}
          onChange={(e) => setJobId(e.target.value)}
          style={{ width: '100%', padding: 6 }}
        />
        <button type="button" onClick={() => reviewJob('approve')} style={{ marginTop: 8 }}>
          审核通过
        </button>
        <button type="button" onClick={() => reviewJob('reject')} style={{ marginTop: 8, marginLeft: 8 }}>
          审核驳回
        </button>
      </div>

      <h3>岗位列表</h3>
      <ul>
        {jobs.map((job) => (
          <li key={job.id}>
            {job.id} | {job.title} | {job.enterpriseName} | {job.status}
          </li>
        ))}
      </ul>

      <h3>AI任务列表</h3>
      <ul>
        {aiTasks.map((task) => (
          <li key={task.id}>
            {task.id} | {task.taskName} | {task.taskStatus} | {task.updatedAt}
          </li>
        ))}
      </ul>
      <p>{message}</p>
    </section>
  )
}
