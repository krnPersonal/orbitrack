import { useEffect, useState } from "react";
import {Navigate, Outlet, useLocation} from "react-router-dom";
import {getToken, subscribeToAuthStateChange} from "../Client";

export default function ProtectedRoute() {
    const location = useLocation();
    const [token, setTokenState] = useState(getToken());

    useEffect(() => {
        return subscribeToAuthStateChange(setTokenState);
    }, []);

    if (!token) {
        return <Navigate to="/login" replace state={{from: location}}/>;
    }
    return <Outlet/>;
}