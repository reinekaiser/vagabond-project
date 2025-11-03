import { useSelector } from "react-redux";
import { Navigate, Outlet, useLocation } from "react-router-dom";

const ProtectedRoute = ({ allowedRoles }) => {
    const { user } = useSelector((state) => state.auth);
    const location = useLocation();
    if (!user) {
        return <Navigate to="/login" state={{ from: location }} replace />;
    }

    if (user.role?.toUpperCase() === "ADMIN" && !location.pathname.startsWith("/admin")) {
        return <Navigate to="/admin/dashboard" replace />;
    }

    if (
        allowedRoles &&
        !allowedRoles.map((r) => r.toUpperCase()).includes(user.role?.toUpperCase())
    ) {
        return <Navigate to="/" replace />;
    }

    return <Outlet />;
};

export default ProtectedRoute;