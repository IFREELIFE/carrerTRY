import { useState } from 'react'

const API_BASE = import.meta.env.VITE_API_BASE ?? 'http://localhost:8080'

export default function AdminPage() {
  const [jobs, setJobs] = useState([])
  const [aiTasks, setAiTasks] = useState([])
  const [qualityMetrics, setQualityMetrics] = useState([])
  const [acceptanceItems, setAcceptanceItems] = useState([])
  const [jobId, setJobId] = useState('')
  const [taskStatusFilter, setTaskStatusFilter] = useState('')
  const [taskId, setTaskId] = useState('')
  const [notice, setNotice] = useState({ title: '', content: '', audienceRole: 'STUDENT' })
  const [acceptance, setAcceptance] = useState({ stepNo: 1, itemName: '', doneFlag: false, note: '' })
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
      const query = taskStatusFilter.trim() ? `?status=${encodeURIComponent(taskStatusFilter.trim())}` : ''
      const response = await fetch(`${API_BASE}/admin/ai-tasks${query}`)
      const data = await response.json()
      if (!response.ok) throw new Error(data.message ?? '加载AI任务失败')
      setAiTasks(Array.isArray(data) ? data : [])
      setMessage('AI任务加载成功')
    } catch (err) {
      setMessage(`加载AI任务失败：${err.message}`)
    }
  }

  async function retryTask() {
    if (!taskId.trim()) {
      setMessage('请先输入任务ID')
      return
    }
    setMessage('重试任务中...')
    try {
      const response = await fetch(`${API_BASE}/admin/ai-tasks/${taskId}/retry`, { method: 'PATCH' })
      const data = await response.json()
      if (!response.ok) throw new Error(data.message ?? '重试失败')
      setMessage(`重试已入队：任务 ${data.id}`)
      await loadAiTasks()
    } catch (err) {
      setMessage(`重试失败：${err.message}`)
    }
  }

  async function publishNotice() {
    setMessage('发布公告中...')
    try {
      const response = await fetch(`${API_BASE}/admin/notices`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(notice)
      })
      const data = await response.json()
      if (!response.ok) throw new Error(data.message ?? '发布公告失败')
      setMessage(`公告发布成功：${data.title}`)
    } catch (err) {
      setMessage(`发布公告失败：${err.message}`)
    }
  }

  async function initAchievements() {
    setMessage('初始化成就定义中...')
    try {
      const response = await fetch(`${API_BASE}/admin/achievements/init`, { method: 'POST' })
      const data = await response.json()
      if (!response.ok) throw new Error(data.message ?? '初始化失败')
      setMessage(data.message)
    } catch (err) {
      setMessage(`初始化失败：${err.message}`)
    }
  }

  async function loadQualityMetrics() {
    setMessage('加载质量指标中...')
    try {
      const response = await fetch(`${API_BASE}/admin/quality-metrics`)
      const data = await response.json()
      if (!response.ok) throw new Error(data.message ?? '加载失败')
      setQualityMetrics(Array.isArray(data) ? data : [])
      setMessage('质量指标加载成功')
    } catch (err) {
      setMessage(`加载失败：${err.message}`)
    }
  }

  async function submitAcceptance() {
    setMessage('更新验收项中...')
    try {
      const response = await fetch(`${API_BASE}/admin/acceptance`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(acceptance)
      })
      const data = await response.json()
      if (!response.ok) throw new Error(data.message ?? '更新失败')
      setMessage(`验收项更新成功：${data.itemName}`)
      await loadAcceptance()
    } catch (err) {
      setMessage(`更新失败：${err.message}`)
    }
  }

  async function loadAcceptance() {
    setMessage('加载验收清单中...')
    try {
      const response = await fetch(`${API_BASE}/admin/acceptance`)
      const data = await response.json()
      if (!response.ok) throw new Error(data.message ?? '加载失败')
      setAcceptanceItems(Array.isArray(data) ? data : [])
      setMessage('验收清单加载成功')
    } catch (err) {
      setMessage(`加载失败：${err.message}`)
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
        <button type="button" onClick={loadQualityMetrics} style={{ marginLeft: 8 }}>质量指标</button>
        <button type="button" onClick={loadAcceptance} style={{ marginLeft: 8 }}>验收清单</button>
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

      <div style={{ marginBottom: 12, border: '1px solid #ddd', padding: 8 }}>
        <h3>任务中心基础版</h3>
        <input
          placeholder="任务状态过滤(QUEUED/EXECUTING/SUCCESS/FAILED)"
          value={taskStatusFilter}
          onChange={(e) => setTaskStatusFilter(e.target.value)}
          style={{ width: '100%', padding: 6, marginBottom: 8 }}
        />
        <input
          placeholder="失败任务ID（重试）"
          value={taskId}
          onChange={(e) => setTaskId(e.target.value)}
          style={{ width: '100%', padding: 6, marginBottom: 8 }}
        />
        <button type="button" onClick={retryTask}>失败重试</button>
      </div>

      <div style={{ marginBottom: 12, border: '1px solid #ddd', padding: 8 }}>
        <h3>通告中心与成就</h3>
        <input placeholder="公告标题" value={notice.title} onChange={(e) => setNotice((p) => ({ ...p, title: e.target.value }))} style={{ width: '100%', padding: 6, marginBottom: 8 }} />
        <textarea placeholder="公告内容" value={notice.content} onChange={(e) => setNotice((p) => ({ ...p, content: e.target.value }))} style={{ width: '100%', minHeight: 70, marginBottom: 8 }} />
        <input placeholder="受众角色" value={notice.audienceRole} onChange={(e) => setNotice((p) => ({ ...p, audienceRole: e.target.value }))} style={{ width: '100%', padding: 6, marginBottom: 8 }} />
        <button type="button" onClick={publishNotice}>发布公告</button>
        <button type="button" onClick={initAchievements} style={{ marginLeft: 8 }}>初始化30个成就</button>
      </div>

      <div style={{ marginBottom: 12, border: '1px solid #ddd', padding: 8 }}>
        <h3>联调验收清单</h3>
        <input type="number" placeholder="步骤" value={acceptance.stepNo} onChange={(e) => setAcceptance((p) => ({ ...p, stepNo: Number(e.target.value) }))} style={{ width: '100%', padding: 6, marginBottom: 8 }} />
        <input placeholder="事项" value={acceptance.itemName} onChange={(e) => setAcceptance((p) => ({ ...p, itemName: e.target.value }))} style={{ width: '100%', padding: 6, marginBottom: 8 }} />
        <textarea placeholder="备注" value={acceptance.note} onChange={(e) => setAcceptance((p) => ({ ...p, note: e.target.value }))} style={{ width: '100%', minHeight: 60, marginBottom: 8 }} />
        <label><input type="checkbox" checked={acceptance.doneFlag} onChange={(e) => setAcceptance((p) => ({ ...p, doneFlag: e.target.checked }))} /> 已完成</label>
        <div><button type="button" onClick={submitAcceptance}>提交验收项</button></div>
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
            {task.id} | {task.taskName} | {task.taskStatus} | 重试:{task.retryCount} | {task.updatedAt}
          </li>
        ))}
      </ul>
      <h3>质量指标</h3>
      <ul>
        {qualityMetrics.map((m) => (
          <li key={m.id}>{m.metricName} | {m.metricValue} | {m.snapshotTime}</li>
        ))}
      </ul>
      <h3>验收清单</h3>
      <ul>
        {acceptanceItems.map((a) => (
          <li key={a.id}>Step {a.stepNo} | {a.itemName} | {a.doneFlag ? '已完成' : '未完成'} | {a.note}</li>
        ))}
      </ul>
      <p>{message}</p>
    </section>
  )
}
