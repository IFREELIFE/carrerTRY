import { useState } from 'react'

const API_BASE = import.meta.env.VITE_API_BASE ?? 'http://localhost:8080'

export default function SchoolPage() {
  const [studentName, setStudentName] = useState('')
  const [mentor, setMentor] = useState('')
  const [comment, setComment] = useState('')
  const [feedbacks, setFeedbacks] = useState([])
  const [message, setMessage] = useState('')

  async function submitFeedback(e) {
    e.preventDefault()
    if (!studentName.trim() || !mentor.trim() || !comment.trim()) {
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
          mentor: mentor.trim(),
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
            value={mentor}
            onChange={(e) => setMentor(e.target.value)}
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
      <p>{message}</p>
    </section>
  )
}
