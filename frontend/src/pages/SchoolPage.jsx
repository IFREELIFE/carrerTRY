import { useState } from 'react'

const API_BASE = import.meta.env.VITE_API_BASE ?? 'http://localhost:8080'

export default function SchoolPage() {
  const [studentName, setStudentName] = useState('')
  const [mentorName, setMentorName] = useState('')
  const [comment, setComment] = useState('')
  const [feedbacks, setFeedbacks] = useState([])
  const [message, setMessage] = useState('')
  const [mentorForm, setMentorForm] = useState({ name: '', expertise: '', phone: '', availableTime: '', location: '' })
  const [mentors, setMentors] = useState([])
  const [students, setStudents] = useState([])
  const [studentPage, setStudentPage] = useState(0)
  const [studentTotalPages, setStudentTotalPages] = useState(0)
  const [studentDetail, setStudentDetail] = useState(null)
  const [dashboard, setDashboard] = useState(null)

  async function submitFeedback(e) {
    e.preventDefault()
    if (!studentName.trim() || !mentorName.trim() || !comment.trim()) {
      setMessage('请完整填写学生、导师和评语内容')
      return
    }
    setMessage('提交中...')
    try {
      const response = await fetch(`${API_BASE}/school/feedbacks`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          studentName: studentName.trim(),
          mentor: mentorName.trim(),
          comment: comment.trim()
        })
      })
      const data = await response.json()
      if (!response.ok) throw new Error(data.message ?? '提交失败')
      setMessage(`提交成功，评语ID：${data.id}`)
      await queryFeedbacks()
    } catch (err) {
      setMessage(`提交失败：${err.message}`)
    }
  }

  async function queryFeedbacks() {
    if (!studentName.trim()) {
      setMessage('请先填写学生姓名后再查询')
      return
    }
    setMessage('查询中...')
    try {
      const response = await fetch(
        `${API_BASE}/school/feedbacks?studentName=${encodeURIComponent(studentName.trim())}`
      )
      const data = await response.json()
      if (!response.ok) throw new Error(data.message ?? '查询失败')
      setFeedbacks(Array.isArray(data) ? data : [])
      setMessage('查询成功')
    } catch (err) {
      setFeedbacks([])
      setMessage(`查询失败：${err.message}`)
    }
  }

  async function addMentor() {
    setMessage('新增老师中...')
    try {
      const response = await fetch(`${API_BASE}/school/mentors`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(mentorForm)
      })
      const data = await response.json()
      if (!response.ok) throw new Error(data.message ?? '新增失败')
      setMessage(`新增老师成功：${data.name}`)
      await loadMentors()
    } catch (err) {
      setMessage(`新增失败：${err.message}`)
    }
  }

  async function loadMentors() {
    setMessage('加载老师中...')
    try {
      const response = await fetch(`${API_BASE}/school/mentors`)
      const data = await response.json()
      if (!response.ok) throw new Error(data.message ?? '加载失败')
      setMentors(Array.isArray(data) ? data : [])
      setMessage('老师加载成功')
    } catch (err) {
      setMessage(`加载失败：${err.message}`)
    }
  }

  async function loadStudents(page = studentPage) {
    setMessage('加载学生画像中...')
    try {
      const response = await fetch(`${API_BASE}/school/students?page=${page}&size=10`)
      const data = await response.json()
      if (!response.ok) throw new Error(data.message ?? '加载失败')
      setStudents(Array.isArray(data.content) ? data.content : [])
      setStudentPage(data.number ?? page)
      setStudentTotalPages(data.totalPages ?? 0)
      setMessage('学生画像加载成功')
    } catch (err) {
      setMessage(`加载失败：${err.message}`)
    }
  }

  async function loadStudentDetail(username) {
    setMessage('加载学生详情中...')
    try {
      const response = await fetch(`${API_BASE}/school/students/${encodeURIComponent(username)}`)
      const data = await response.json()
      if (!response.ok) throw new Error(data.message ?? '加载详情失败')
      setStudentDetail(data)
      setMessage('学生详情加载成功')
    } catch (err) {
      setStudentDetail(null)
      setMessage(`加载详情失败：${err.message}`)
    }
  }

  async function loadDashboard() {
    setMessage('加载学校仪表盘中...')
    try {
      const response = await fetch(`${API_BASE}/school/dashboard`)
      const data = await response.json()
      if (!response.ok) throw new Error(data.message ?? '加载仪表盘失败')
      setDashboard(data)
      setMessage('学校仪表盘加载成功')
    } catch (err) {
      setDashboard(null)
      setMessage(`加载仪表盘失败：${err.message}`)
    }
  }

  return (
    <section>
      <h2>/school/...</h2>
      <p>辅导评语录入与按学生查询。</p>

      <form onSubmit={submitFeedback}>
        <div style={{ marginBottom: 8 }}>
          <input
            placeholder="学生姓名"
            value={studentName}
            onChange={(e) => setStudentName(e.target.value)}
            style={{ width: '100%', padding: 6 }}
          />
        </div>
        <div style={{ marginBottom: 8 }}>
          <input
            placeholder="导师姓名"
            value={mentorName}
            onChange={(e) => setMentorName(e.target.value)}
            style={{ width: '100%', padding: 6 }}
          />
        </div>
        <div style={{ marginBottom: 8 }}>
          <textarea
            placeholder="评语内容"
            value={comment}
            onChange={(e) => setComment(e.target.value)}
            style={{ width: '100%', minHeight: 80, padding: 6 }}
          />
        </div>
        <button type="submit">提交评语</button>
        <button type="button" onClick={queryFeedbacks} style={{ marginLeft: 8 }}>
          查询该学生评语
        </button>
      </form>

      <h3>查询结果</h3>
      <ul>
        {feedbacks.map((item) => (
          <li key={item.id}>
            {item.createdAt} | {item.studentName} | 导师：{item.mentor} | {item.comment}
          </li>
        ))}
      </ul>
      <section style={{ border: '1px solid #ddd', padding: 12, marginTop: 12 }}>
        <h3>老师管理</h3>
        <input placeholder="姓名" value={mentorForm.name} onChange={(e) => setMentorForm((p) => ({ ...p, name: e.target.value }))} style={{ width: '100%', padding: 6, marginBottom: 8 }} />
        <input placeholder="领域" value={mentorForm.expertise} onChange={(e) => setMentorForm((p) => ({ ...p, expertise: e.target.value }))} style={{ width: '100%', padding: 6, marginBottom: 8 }} />
        <input placeholder="电话" value={mentorForm.phone} onChange={(e) => setMentorForm((p) => ({ ...p, phone: e.target.value }))} style={{ width: '100%', padding: 6, marginBottom: 8 }} />
        <input placeholder="时间" value={mentorForm.availableTime} onChange={(e) => setMentorForm((p) => ({ ...p, availableTime: e.target.value }))} style={{ width: '100%', padding: 6, marginBottom: 8 }} />
        <input placeholder="地点" value={mentorForm.location} onChange={(e) => setMentorForm((p) => ({ ...p, location: e.target.value }))} style={{ width: '100%', padding: 6, marginBottom: 8 }} />
        <button type="button" onClick={addMentor}>新增老师</button>
        <button type="button" onClick={loadMentors} style={{ marginLeft: 8 }}>查询老师</button>
        <ul>
          {mentors.map((m) => (
            <li key={m.id}>{m.name} | {m.expertise} | {m.phone} | {m.availableTime} | {m.location}</li>
          ))}
        </ul>
      </section>
      <section style={{ border: '1px solid #ddd', padding: 12, marginTop: 12 }}>
        <h3>学校仪表盘</h3>
        <button type="button" onClick={loadDashboard}>加载仪表盘</button>
        <p>学生总数：{dashboard?.totalStudents ?? 0}，平均评分：{dashboard?.averageScore ?? 0}</p>
        <h4>就业意向画像</h4>
        <ul>
          {(dashboard?.intentionPortrait ?? []).map((item) => (
            <li key={item.career}>{item.career}：{item.count}</li>
          ))}
        </ul>
        <h4>评分扇形图数据</h4>
        <ul>
          {(dashboard?.scoreFanChart ?? []).map((item) => (
            <li key={item.range}>{item.range}：{item.count}</li>
          ))}
        </ul>
      </section>
      <section style={{ border: '1px solid #ddd', padding: 12, marginTop: 12 }}>
        <h3>学生列表与画像查看</h3>
        <button type="button" onClick={() => loadStudents(Math.max(studentPage - 1, 0))}>上一页</button>
        <button type="button" onClick={() => loadStudents(Math.min(studentPage + 1, Math.max(studentTotalPages - 1, 0)))} style={{ marginLeft: 8 }}>下一页</button>
        <button type="button" onClick={() => loadStudents(studentPage)} style={{ marginLeft: 8 }}>刷新</button>
        <p>当前第 {studentPage + 1} / {Math.max(studentTotalPages, 1)} 页</p>
        <ul>
          {students.map((s, idx) => (
            <li key={`${s.username}-${idx}`}>
              {s.username} | {s.displayName} | {s.portraitTags} | {s.aiSummary}
              <button type="button" onClick={() => loadStudentDetail(s.username)} style={{ marginLeft: 8 }}>查看详情</button>
            </li>
          ))}
        </ul>
        <h4>学生详情（画像/简历/意向岗位）</h4>
        <pre>{studentDetail ? JSON.stringify(studentDetail, null, 2) : '未选择学生'}</pre>
      </section>
      <p>{message}</p>
    </section>
  )
}
