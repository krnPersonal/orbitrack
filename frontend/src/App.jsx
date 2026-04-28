import {Navigate, Route, Routes} from "react-router-dom";
import {clearToken, setToken} from "./Client.js";
import ProtectedRoute from "./routes/ProtectedRoute";
import PublicOnlyRoute from "./routes/PublicOnlyRoute";

function LoginPage() {
    return (
        <main>
            <h1>Login Page</h1>
            <button type="button" onClick={() => setToken("demo-token")}>
                Simulate Login
            </button>
        </main>
    );
}

function DashboardPage() {
    return (
        <main>
            <h1>Dashboard Page</h1>
            <button type="button" onClick={clearToken}>
                Simulate Logout
            </button>
        </main>
    );
}

export default function App() {
    return (
        <Routes>
            <Route path="/" element={<Navigate to="/login" replace/>}/>

            <Route element={<PublicOnlyRoute/>}>
                <Route path="/login" element={<LoginPage/>}/>
            </Route>

            <Route element={<ProtectedRoute/>}>
                <Route path="/dashboard" element={<DashboardPage/>}/>
            </Route>
        </Routes>
    )
}