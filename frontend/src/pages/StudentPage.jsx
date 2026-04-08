import { useState } from 'react'

const API_BASE = import.meta.env.VITE_API_BASE ?? 'http://localhost:8080'

async function requestJson(url, options) {
  const response = await fetch(url, options)
  let data = null
  try {
    data = await response.json()
  } catch {
    data = null
  }
  if (!response.ok) throw new Error(data.message ?? '请求失败')
  return data
}

export default function StudentPage() {
  const [message, setMessage] = useState('')
  const [homePage, setHomePage] = useState(0)
  const [homeJobs, setHomeJobs] = useState([])
  const [jobId, setJobId] = useState('')
  const [resumeSummary, setResumeSummary] = useState('')
  const [jobDetail, setJobDetail] = useState(null)
  const [applications, setApplications] = useState([])
  const [onboarding, setOnboarding] = useState({ techStack: '', capabilityInfo: '', mbtiType: '', commitmentAgreed: false })
  const [profile, setProfile] = useState(null)
  const [center, setCenter] = useState({ displayName: '', phone: '', major: '' })
  const [resume, setResume] = useState({ fileName: 'resume.txt', content: '' })
  const [resumes, setResumes] = useState([])
  const [portraitMatches, setPortraitMatches] = useState([])
  const [plan, setPlan] = useState({ targetCity: '', targetCareer: '', progressPercent: 0, dynamicAdjustment: '' })
  const [myPlan, setMyPlan] = useState(null)
  const [notices, setNotices] = useState([])
  const [achievements, setAchievements] = useState([])
  const [correction, setCorrection] = useState({ wrongPoint: '', correctionAction: '', closedLoopStatus: 'OPEN' })
  const [corrections, setCorrections] = useState([])
  const [dailySummary, setDailySummary] = useState(null)
  const [homeSummary, setHomeSummary] = useState(null)
  const [reports, setReports] = useState([])

  async function loadHome(page = homePage) {
    try {
      const data = await requestJson(`${API_BASE}/student/home?page=${page}`)
      setHomeJobs(data.content ?? [])
      setHomePage(data.number ?? page)
      await requestJson(`${API_BASE}/student/activity`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ viewedJobs: true })
      })
      await loadDailySummary()
      await loadHomeSummary()
      setMessage('首页岗位加载成功')
    } catch (err) {
      setMessage(`首页岗位加载失败：${err.message}`)
    }
  }

  async function recordActive30s() {
    try {
      const data = await requestJson(`${API_BASE}/student/activity`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ activeSeconds: 30 })
      })
      setDailySummary(data)
      setMessage('已记录30秒活跃')
    } catch (err) {
      setMessage(`活跃记录失败：${err.message}`)
    }
  }

  async function checkInToday() {
    try {
      const data = await requestJson(`${API_BASE}/student/check-in`, { method: 'POST' })
      setDailySummary(data)
      setMessage('签到成功')
    } catch (err) {
      setMessage(`签到失败：${err.message}`)
    }
  }

  async function loadDailySummary() {
    try {
      const data = await requestJson(`${API_BASE}/student/check-in/summary`)
      setDailySummary(data)
    } catch (err) {
      console.warn('loadDailySummary failed', err)
      setDailySummary(null)
    }
  }

  async function loadHomeSummary() {
    try {
      const data = await requestJson(`${API_BASE}/student/home/summary`)
      setHomeSummary(data)
    } catch {
      setHomeSummary(null)
    }
  }

  async function submitOnboarding(e) {
    e.preventDefault()
    try {
      await requestJson(`${API_BASE}/student/onboarding`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(onboarding)
      })
      await loadHomeSummary()
      await loadReports()
      setMessage('首次链路完成')
    } catch (err) {
      setMessage(`首次链路失败：${err.message}`)
    }
  }

  async function loadProfile() {
    try {
      const data = await requestJson(`${API_BASE}/student/profile`)
      setProfile(data)
      setMessage('画像加载成功')
    } catch (err) {
      setMessage(`画像加载失败：${err.message}`)
    }
  }

  async function updateCenter() {
    try {
      const data = await requestJson(`${API_BASE}/student/center`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(center)
      })
      setMessage(`个人中心更新成功：${data.user.displayName}`)
    } catch (err) {
      setMessage(`个人中心更新失败：${err.message}`)
    }
  }

  async function uploadResume() {
    try {
      await requestJson(`${API_BASE}/student/resumes`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(resume)
      })
      setMessage('简历上传成功')
      await loadResumes()
    } catch (err) {
      setMessage(`简历上传失败：${err.message}`)
    }
  }

  async function loadResumes() {
    try {
      const data = await requestJson(`${API_BASE}/student/resumes`)
      setResumes(Array.isArray(data) ? data : [])
      await loadHomeSummary()
      setMessage('简历列表加载成功')
    } catch (err) {
      setMessage(`简历列表加载失败：${err.message}`)
    }
  }

  async function loadJobDetail() {
    if (!jobId) return
    try {
      const data = await requestJson(`${API_BASE}/student/jobs/${jobId}`)
      setJobDetail(data)
      setMessage('岗位详情加载成功')
    } catch (err) {
      setMessage(`岗位详情加载失败：${err.message}`)
    }
  }

  async function applyJob(e) {
    e.preventDefault()
    try {
      await requestJson(`${API_BASE}/student/applications`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ jobId: Number(jobId), resumeSummary })
      })
      setMessage('投递成功')
      await loadApplications()
    } catch (err) {
      setMessage(`投递失败：${err.message}`)
    }
  }

  async function loadApplications() {
    try {
      const data = await requestJson(`${API_BASE}/student/applications`)
      setApplications(Array.isArray(data) ? data : [])
      setMessage('投递记录加载成功')
    } catch (err) {
      setMessage(`投递记录加载失败：${err.message}`)
    }
  }

  async function loadPortraitMatches() {
    try {
      const data = await requestJson(`${API_BASE}/student/portrait/matches`)
      setPortraitMatches(Array.isArray(data) ? data : [])
      setMessage('画像匹配加载成功')
    } catch (err) {
      setMessage(`画像匹配加载失败：${err.message}`)
    }
  }

  async function savePlan() {
    try {
      const data = await requestJson(`${API_BASE}/student/plans`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(plan)
      })
      setMyPlan(data)
      await loadReports()
      await loadHomeSummary()
      setMessage('规划保存成功')
    } catch (err) {
      setMessage(`规划保存失败：${err.message}`)
    }
  }

  async function loadPlan() {
    try {
      const data = await requestJson(`${API_BASE}/student/plans`)
      setMyPlan(data)
      setMessage('规划加载成功')
    } catch (err) {
      setMessage(`规划加载失败：${err.message}`)
    }
  }

  async function loadReports() {
    try {
      const data = await requestJson(`${API_BASE}/student/reports`)
      setReports(Array.isArray(data) ? data : [])
      setMessage('报告列表加载成功')
    } catch (err) {
      setMessage(`报告列表加载失败：${err.message}`)
    }
  }

  async function loadNotices() {
    try {
      const data = await requestJson(`${API_BASE}/student/notices`)
      setNotices(Array.isArray(data) ? data : [])
      setMessage('通知加载成功')
    } catch (err) {
      setMessage(`通知加载失败：${err.message}`)
    }
  }

  async function loadAchievements() {
    try {
      const data = await requestJson(`${API_BASE}/student/achievements`)
      setAchievements(Array.isArray(data) ? data : [])
      setMessage('成就加载成功')
    } catch (err) {
      setMessage(`成就加载失败：${err.message}`)
    }
  }

  async function saveCorrection() {
    try {
      await requestJson(`${API_BASE}/student/corrections`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(correction)
      })
      setMessage('纠偏记录成功')
      await loadCorrections()
    } catch (err) {
      setMessage(`纠偏记录失败：${err.message}`)
    }
  }

  async function loadCorrections() {
    try {
      const data = await requestJson(`${API_BASE}/student/corrections`)
      setCorrections(Array.isArray(data) ? data : [])
      setMessage('纠偏列表加载成功')
    } catch (err) {
      setMessage(`纠偏列表加载失败：${err.message}`)
    }
  }

  return (
    <section>
      <h2>/student/...</h2>
      <p>首页、首次必填、岗位投递、个人中心、简历、画像、规划、通知、成就、纠偏。</p>

      <section style={{ border: '1px solid #ddd', padding: 12, marginBottom: 12 }}>
        <h3>首页岗位（15条分页）</h3>
        <button onClick={loadDailySummary}>加载签到信息</button>
        <button onClick={loadHomeSummary} style={{ marginLeft: 8 }}>加载首页概览</button>
        <button onClick={loadReports} style={{ marginLeft: 8 }}>加载报告列表</button>
        <button onClick={recordActive30s} style={{ marginLeft: 8 }}>记录30秒活跃</button>
        <button onClick={checkInToday} style={{ marginLeft: 8 }}>今日签到</button>
        <pre>{dailySummary ? JSON.stringify(dailySummary, null, 2) : '暂无签到数据'}</pre>
        <ul>
          <li>每日签到：{homeSummary?.dailyCheckIn?.checkedIn ? '已签到' : '未签到'}</li>
          <li>简历上传：{homeSummary?.resumeUploaded ? `已上传(${homeSummary?.resumeCount})` : '未上传'}</li>
          <li>MBTI测试：{homeSummary?.mbtiCompleted ? '已完成' : '未完成'}</li>
          <li>匹配岗位：{homeSummary?.matchedJobCount ?? 0}</li>
          <li>连续签到日长：{homeSummary?.consecutiveCheckInDays ?? 0}</li>
        </ul>
        <h4>已生成报告</h4>
        <ul>
          {reports.map((r) => <li key={r.id}>{r.reportType} | {r.reportTitle} | {r.createdAt}</li>)}
        </ul>
        <button onClick={() => loadHome(Math.max(homePage - 1, 0))}>上一页</button>
        <button onClick={() => loadHome(homePage + 1)} style={{ marginLeft: 8 }}>下一页</button>
        <button onClick={() => loadHome(homePage)} style={{ marginLeft: 8 }}>刷新</button>
        <ul>{homeJobs.map((j) => <li key={j.id}>{j.id} | {j.title} | {j.location}</li>)}</ul>
      </section>

      <form onSubmit={submitOnboarding} style={{ border: '1px solid #ddd', padding: 12, marginBottom: 12 }}>
        <h3>首次必填链路</h3>
        <input placeholder="技术栈" value={onboarding.techStack} onChange={(e) => setOnboarding((p) => ({ ...p, techStack: e.target.value }))} style={{ width: '100%', marginBottom: 8 }} />
        <textarea placeholder="能力信息" value={onboarding.capabilityInfo} onChange={(e) => setOnboarding((p) => ({ ...p, capabilityInfo: e.target.value }))} style={{ width: '100%', marginBottom: 8 }} />
        <input placeholder="MBTI" value={onboarding.mbtiType} onChange={(e) => setOnboarding((p) => ({ ...p, mbtiType: e.target.value }))} style={{ width: '100%', marginBottom: 8 }} />
        <label><input type="checkbox" checked={onboarding.commitmentAgreed} onChange={(e) => setOnboarding((p) => ({ ...p, commitmentAgreed: e.target.checked }))} /> 已勾选保证书</label>
        <div><button type="submit">提交首次链路</button></div>
      </form>

      <section style={{ border: '1px solid #ddd', padding: 12, marginBottom: 12 }}>
        <h3>岗位详情与投递</h3>
        <input placeholder="岗位ID" value={jobId} onChange={(e) => setJobId(e.target.value)} style={{ width: '100%', marginBottom: 8 }} />
        <button onClick={loadJobDetail}>查看详情</button>
        <pre>{jobDetail ? JSON.stringify(jobDetail, null, 2) : '暂无详情'}</pre>
        <form onSubmit={applyJob}>
          <textarea placeholder="简历摘要" value={resumeSummary} onChange={(e) => setResumeSummary(e.target.value)} style={{ width: '100%', marginBottom: 8 }} />
          <button type="submit">投递岗位</button>
          <button type="button" onClick={loadApplications} style={{ marginLeft: 8 }}>我的投递</button>
        </form>
        <ul>{applications.map((a) => <li key={a.id}>#{a.id} job:{a.jobId} status:{a.status}</li>)}</ul>
      </section>

      <section style={{ border: '1px solid #ddd', padding: 12, marginBottom: 12 }}>
        <h3>个人中心可编辑</h3>
        <input placeholder="姓名" value={center.displayName} onChange={(e) => setCenter((p) => ({ ...p, displayName: e.target.value }))} style={{ width: '100%', marginBottom: 8 }} />
        <input placeholder="电话" value={center.phone} onChange={(e) => setCenter((p) => ({ ...p, phone: e.target.value }))} style={{ width: '100%', marginBottom: 8 }} />
        <input placeholder="专业" value={center.major} onChange={(e) => setCenter((p) => ({ ...p, major: e.target.value }))} style={{ width: '100%', marginBottom: 8 }} />
        <button onClick={updateCenter}>保存个人中心</button>
      </section>

      <section style={{ border: '1px solid #ddd', padding: 12, marginBottom: 12 }}>
        <h3>简历管理（上传/建议/美化）</h3>
        <input placeholder="文件名" value={resume.fileName} onChange={(e) => setResume((p) => ({ ...p, fileName: e.target.value }))} style={{ width: '100%', marginBottom: 8 }} />
        <textarea placeholder="简历正文" value={resume.content} onChange={(e) => setResume((p) => ({ ...p, content: e.target.value }))} style={{ width: '100%', marginBottom: 8 }} />
        <button onClick={uploadResume}>上传并生成建议</button>
        <button onClick={loadResumes} style={{ marginLeft: 8 }}>查看简历</button>
        <ul>{resumes.map((r) => <li key={r.id}>{r.fileName} | 建议:{r.aiSuggestion} | 美化:{r.beautifiedContent}</li>)}</ul>
      </section>

      <section style={{ border: '1px solid #ddd', padding: 12, marginBottom: 12 }}>
        <h3>12维画像与四维匹配</h3>
        <button onClick={loadProfile}>加载画像</button>
        <button onClick={loadPortraitMatches} style={{ marginLeft: 8 }}>加载匹配分</button>
        <pre>{profile ? JSON.stringify(profile, null, 2) : '暂无画像'}</pre>
        <ul>{portraitMatches.map((m) => <li key={m.jobId}>{m.jobTitle} 总分:{m.totalScore}</li>)}</ul>
      </section>

      <section style={{ border: '1px solid #ddd', padding: 12, marginBottom: 12 }}>
        <h3>目标设定与个性规划</h3>
        <input placeholder="目标城市" value={plan.targetCity} onChange={(e) => setPlan((p) => ({ ...p, targetCity: e.target.value }))} style={{ width: '100%', marginBottom: 8 }} />
        <input placeholder="目标职业" value={plan.targetCareer} onChange={(e) => setPlan((p) => ({ ...p, targetCareer: e.target.value }))} style={{ width: '100%', marginBottom: 8 }} />
        <input placeholder="进度(0-100)" type="number" value={plan.progressPercent} onChange={(e) => setPlan((p) => ({ ...p, progressPercent: Number(e.target.value) }))} style={{ width: '100%', marginBottom: 8 }} />
        <textarea placeholder="动态调整" value={plan.dynamicAdjustment} onChange={(e) => setPlan((p) => ({ ...p, dynamicAdjustment: e.target.value }))} style={{ width: '100%', marginBottom: 8 }} />
        <button onClick={savePlan}>保存规划</button>
        <button onClick={loadPlan} style={{ marginLeft: 8 }}>加载规划</button>
        <a href={`${API_BASE}/student/plans/pdf`} target="_blank" rel="noreferrer" style={{ marginLeft: 8 }}>下载PDF</a>
        <pre>{myPlan ? JSON.stringify(myPlan, null, 2) : '暂无规划'}</pre>
      </section>

      <section style={{ border: '1px solid #ddd', padding: 12, marginBottom: 12 }}>
        <h3>通知中心与成就系统</h3>
        <button onClick={loadNotices}>加载通知</button>
        <button onClick={loadAchievements} style={{ marginLeft: 8 }}>加载成就</button>
        <ul>{notices.map((n) => <li key={n.id}>{n.noticeType} | {n.title}</li>)}</ul>
        <ul>{achievements.map((a) => <li key={a.id}>{a.achievementCode} | {a.achievedAt}</li>)}</ul>
      </section>

      <section style={{ border: '1px solid #ddd', padding: 12 }}>
        <h3>错题本纠偏闭环</h3>
        <textarea placeholder="问题" value={correction.wrongPoint} onChange={(e) => setCorrection((p) => ({ ...p, wrongPoint: e.target.value }))} style={{ width: '100%', marginBottom: 8 }} />
        <textarea placeholder="纠偏动作" value={correction.correctionAction} onChange={(e) => setCorrection((p) => ({ ...p, correctionAction: e.target.value }))} style={{ width: '100%', marginBottom: 8 }} />
        <input placeholder="状态" value={correction.closedLoopStatus} onChange={(e) => setCorrection((p) => ({ ...p, closedLoopStatus: e.target.value }))} style={{ width: '100%', marginBottom: 8 }} />
        <button onClick={saveCorrection}>提交纠偏</button>
        <button onClick={loadCorrections} style={{ marginLeft: 8 }}>查看纠偏</button>
        <ul>{corrections.map((c) => <li key={c.id}>{c.closedLoopStatus} | {c.wrongPoint}</li>)}</ul>
      </section>

      <p>{message}</p>
    </section>
  )
}
