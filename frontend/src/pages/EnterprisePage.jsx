import { useState } from 'react'

const API_BASE = import.meta.env.VITE_API_BASE ?? 'http://localhost:8080'
const inputStyle = { display: 'block', width: '100%', padding: 6, marginBottom: 8 }

export default function EnterprisePage() {
  const [result, setResult] = useState('')
  const [jobs, setJobs] = useState([])
  const [pageInfo, setPageInfo] = useState({ page: 0, size: 10, totalPages: 0 })
  const [enterpriseFilter, setEnterpriseFilter] = useState('')
  const [excelFile, setExcelFile] = useState(null)
  const [form, setForm] = useState({
    title: '',
    description: '',
    enterpriseName: '',
    department: '',
    location: '',
    salaryMin: '',
    salaryMax: '',
    experienceRequirement: '',
    educationRequirement: '',
    skills: ''
  })

  async function createJob(e) {
    e.preventDefault()
    setResult('提交中...')
    try {
      const response = await fetch(`${API_BASE}/enterprise/jobs`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          ...form,
          salaryMin: Number(form.salaryMin),
          salaryMax: Number(form.salaryMax)
        })
      })
      const data = await response.json()
      if (!response.ok) throw new Error(data.message ?? '创建失败')
      setResult(`创建成功：岗位ID ${data.id}`)
      await loadJobs(0, pageInfo.size)
    } catch (err) {
      setResult(`创建失败：${err.message}`)
    }
  }

  async function loadJobs(page = pageInfo.page, size = pageInfo.size) {
    setResult('加载列表中...')
    try {
      const params = new URLSearchParams({
        page: String(page),
        size: String(size)
      })
      if (enterpriseFilter.trim()) {
        params.set('enterpriseName', enterpriseFilter.trim())
      }
      const response = await fetch(`${API_BASE}/enterprise/jobs?${params.toString()}`)
      const data = await response.json()
      if (!response.ok) throw new Error(data.message ?? '加载失败')
      setJobs(data.content ?? [])
      setPageInfo({
        page: data.number ?? page,
        size: data.size ?? size,
        totalPages: data.totalPages ?? 0
      })
      setResult('列表加载成功')
    } catch (err) {
      setResult(`列表加载失败：${err.message}`)
    }
  }

  async function importExcel() {
    if (!excelFile) {
      setResult('请先选择文件')
      return
    }
    if (!form.enterpriseName.trim()) {
      setResult('请先填写企业名称用于导入归属')
      return
    }
    setResult('导入中...')
    const body = new FormData()
    body.append('file', excelFile)
    body.append('enterpriseName', form.enterpriseName.trim())
    try {
      const response = await fetch(`${API_BASE}/enterprise/jobs/import/excel`, {
        method: 'POST',
        body
      })
      const data = await response.json()
      if (!response.ok) throw new Error(data.message ?? '导入失败')
      setResult(`导入完成：新增 ${data.importedCount} 条，去重跳过 ${data.skippedDuplicateCount} 条`)
      await loadJobs(0, pageInfo.size)
    } catch (err) {
      setResult(`导入失败：${err.message}`)
    }
  }

  return (
    <section>
      <h2>/enterprise/...</h2>
      <p>岗位创建、分页列表、Excel导入（CSV格式）入口。</p>

      <form onSubmit={createJob} style={{ border: '1px solid #ddd', padding: 12, marginBottom: 12 }}>
        <h3>创建岗位</h3>
        <input style={inputStyle} placeholder="岗位名称" required onChange={(e) => setForm((p) => ({ ...p, title: e.target.value }))} />
        <input style={inputStyle} placeholder="企业名称" required onChange={(e) => setForm((p) => ({ ...p, enterpriseName: e.target.value }))} />
        <input style={inputStyle} placeholder="部门" required onChange={(e) => setForm((p) => ({ ...p, department: e.target.value }))} />
        <input style={inputStyle} placeholder="地点" required onChange={(e) => setForm((p) => ({ ...p, location: e.target.value }))} />
        <input style={inputStyle} placeholder="最低薪资" type="number" required onChange={(e) => setForm((p) => ({ ...p, salaryMin: e.target.value }))} />
        <input style={inputStyle} placeholder="最高薪资" type="number" required onChange={(e) => setForm((p) => ({ ...p, salaryMax: e.target.value }))} />
        <input style={inputStyle} placeholder="经验要求" required onChange={(e) => setForm((p) => ({ ...p, experienceRequirement: e.target.value }))} />
        <input style={inputStyle} placeholder="学历要求" required onChange={(e) => setForm((p) => ({ ...p, educationRequirement: e.target.value }))} />
        <input style={inputStyle} placeholder="技能（逗号分隔）" required onChange={(e) => setForm((p) => ({ ...p, skills: e.target.value }))} />
        <textarea style={{ ...inputStyle, minHeight: 80 }} placeholder="岗位描述" required onChange={(e) => setForm((p) => ({ ...p, description: e.target.value }))} />
        <button type="submit">创建岗位</button>
      </form>

      <section style={{ border: '1px solid #ddd', padding: 12, marginBottom: 12 }}>
        <h3>Excel导入入口（CSV）</h3>
        <p>
          列顺序 / Column order:
          title,department,location,salaryMin,salaryMax,experienceRequirement,educationRequirement,skills,description
        </p>
        <input type="file" accept=".csv,.txt" onChange={(e) => setExcelFile(e.target.files?.[0] ?? null)} />
        <button type="button" onClick={importExcel} style={{ marginLeft: 8 }}>上传导入</button>
      </section>

      <section style={{ border: '1px solid #ddd', padding: 12 }}>
        <h3>岗位分页列表</h3>
        <input
          style={inputStyle}
          placeholder="按企业名称筛选（可选）"
          onChange={(e) => setEnterpriseFilter(e.target.value)}
        />
        <button type="button" onClick={() => loadJobs(0, pageInfo.size)}>查询列表</button>
        <button type="button" onClick={() => loadJobs(Math.max(pageInfo.page - 1, 0), pageInfo.size)} style={{ marginLeft: 8 }}>
          上一页
        </button>
        <button type="button" onClick={() => loadJobs(Math.min(pageInfo.page + 1, Math.max(pageInfo.totalPages - 1, 0)), pageInfo.size)} style={{ marginLeft: 8 }}>
          下一页
        </button>
        <p>当前第 {pageInfo.page + 1} / {Math.max(pageInfo.totalPages, 1)} 页</p>
        <ul>
          {jobs.map((job) => (
            <li key={job.id}>
              {job.title} | {job.department} | {job.location} | {job.salaryMin}-{job.salaryMax} | {job.status}
            </li>
          ))}
        </ul>
      </section>
      <p>{result}</p>
    </section>
  )
}
