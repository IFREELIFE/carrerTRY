import { Link, Navigate, Route, Routes } from 'react-router-dom'
import EnterprisePage from './pages/EnterprisePage'
import AdminPage from './pages/AdminPage'
import StudentPage from './pages/StudentPage'
import SchoolPage from './pages/SchoolPage'

const navStyle = { display: 'flex', gap: '12px', marginBottom: '16px' }

export default function App() {
  return (
    <main style={{ fontFamily: 'Arial, sans-serif', padding: 24 }}>
      <h1>careerTRY</h1>
      <nav style={navStyle}>
        <Link to="/enterprise/jobs">Enterprise</Link>
        <Link to="/admin/jobs">Admin</Link>
        <Link to="/student/jobs">Student</Link>
        <Link to="/school/feedbacks">School</Link>
      </nav>
      <Routes>
        <Route path="/enterprise/*" element={<EnterprisePage />} />
        <Route path="/admin/*" element={<AdminPage />} />
        <Route path="/student/*" element={<StudentPage />} />
        <Route path="/school/*" element={<SchoolPage />} />
        <Route path="*" element={<Navigate to="/enterprise/jobs" replace />} />
      </Routes>
    </main>
  )
}
