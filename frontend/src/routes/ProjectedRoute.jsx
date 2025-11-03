import { useSelector } from "react-redux";
import { Routes, Route, Navigate, Outlet } from "react-router-dom";

const ProtectedRoutes = ({ allowedRoles }) => {
    const { user } = useSelector((state) => state.auth);
    if (!user) {
        return <Navigate to="/" replace />;
    }

    if (allowedRoles && !allowedRoles.includes(user.role)) {
        return <Navigate to="/" replace />;
    }

    return (
        <Outlet/>
    );
};

export default ProtectedRoutes;
