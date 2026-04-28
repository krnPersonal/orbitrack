import { useEffect, useState } from "react";
import { Navigate, Outlet } from "react-router-dom";
import { getToken, subscribeToAuthStateChange } from "../Client";

export default function PublicOnlyRoute() {
    const [token, setTokenState] = useState(getToken());

    useEffect(() => {
        return subscribeToAuthStateChange(setTokenState);
    },[]);

    if(token){
        return <Navigate to="/dashboard" replace />;
    }
    return <Outlet/>;
}