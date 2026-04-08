import { useState } from 'react'

const API_BASE = import.meta.env.VITE_API_BASE ?? 'http://localhost:8080'

const sectionStyle = { border: '1px solid #ddd', padding: 12, marginBottom: 12 }
const inputStyle = { display: 'block', width: '100%', marginBottom: 8, padding: 6 }

function RegisterForm({ title, endpoint, fields }) {
  const [form, setForm] = useState({})
  const [result, setResult] = useState('')

  async function submit(e) {
    e.preventDefault()
    setResult('提交中...')
    try {
      const response = await fetch(`${API_BASE}${endpoint}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form)
      })
      const data = await response.json()
      if (!response.ok) {
        throw new Error(data.message ?? '注册失败')
      }
      setResult(`注册成功：${data.username} (${data.role})`)
    } catch (err) {
      setResult(`注册失败：${err.message}`)
    }
  }

  return (
    <form style={sectionStyle} onSubmit={submit}>
      <h3>{title}</h3>
      {fields.map((field) => (
        <input
          key={field.name}
          style={inputStyle}
          type={field.type ?? 'text'}
          placeholder={field.label}
          onChange={(e) => setForm((prev) => ({ ...prev, [field.name]: e.target.value }))}
          required
        />
      ))}
      <button type="submit">提交注册</button>
      <p>{result}</p>
    </form>
  )
}

export default function AuthPage() {
  return (
    <section>
      <h2>/auth/register</h2>
      <p>注册完成后，可使用 HTTP Basic 账号密码访问对应分端接口。</p>

      <RegisterForm
        title="学生注册"
        endpoint="/auth/register/student"
        fields={[
          { name: 'username', label: '用户名' },
          { name: 'password', label: '密码', type: 'password' },
          { name: 'displayName', label: '姓名' },
          { name: 'email', label: '邮箱' },
          { name: 'schoolName', label: '学校名称' }
        ]}
      />

      <RegisterForm
        title="学校注册"
        endpoint="/auth/register/school"
        fields={[
          { name: 'username', label: '用户名' },
          { name: 'password', label: '密码', type: 'password' },
          { name: 'displayName', label: '联系人' },
          { name: 'schoolName', label: '学校名称' },
          { name: 'email', label: '学校邮箱(.edu/.edu.cn)' }
        ]}
      />

      <RegisterForm
        title="企业注册"
        endpoint="/auth/register/enterprise"
        fields={[
          { name: 'username', label: '用户名' },
          { name: 'password', label: '密码', type: 'password' },
          { name: 'displayName', label: '联系人' },
          { name: 'email', label: '邮箱' },
          { name: 'enterpriseName', label: '企业名称' },
          { name: 'unifiedSocialCreditCode', label: '统一社会信用代码' },
          { name: 'legalRepresentative', label: '法定代表人' }
        ]}
      />
    </section>
  )
}
