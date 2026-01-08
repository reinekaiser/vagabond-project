import React, { useEffect, useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { toast } from "react-toastify";
import { useResetPasswordMutation } from "../../redux/api/authApiSlice";

const ResetPassword = () => {
    const navigate = useNavigate();
    const location = useLocation();

    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");

    const [confirmResetPassword, { isLoading }] =
        useResetPasswordMutation();

    const token = new URLSearchParams(location.search).get("token");

    useEffect(() => {
        if (!token) {
            toast.error("Link reset password không hợp lệ");
            navigate("/forgot-password");
        }
    }, [token, navigate]);

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (newPassword.length < 6) {
            toast.error("Mật khẩu phải có ít nhất 6 ký tự");
            return;
        }

        if (newPassword !== confirmPassword) {
            toast.error("Mật khẩu xác nhận không khớp");
            return;
        }

        try {
            const res = await confirmResetPassword({
                token,
                newPassword,
            }).unwrap();

            toast.success(res.message || "Đổi mật khẩu thành công");
            navigate("/sign-in");
        } catch (error) {
            toast.error(
                error?.data?.message ||
                "Link reset password không hợp lệ hoặc đã hết hạn"
            );
        }
    };

    return (
        <div
            className="grid grid-cols-2 h-screen bg-cover bg-center"
            style={{ backgroundImage: "url('/images/login/background.png')" }}
        >
            <div></div>

            <div className="min-h-screen flex items-center justify-center bg-white">
                <div className=" w-full max-w-md p-8">
                    <h1 className="text-2xl font-bold text-center mb-2">
                        Đặt lại mật khẩu
                    </h1>

                    <p className="text-center text-gray-600 mb-6">
                        Nhập mật khẩu mới của bạn
                    </p>

                    <form onSubmit={handleSubmit} className="w-full">
                        <div className="mb-4">
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Mật khẩu mới
                            </label>
                            <input
                                type="password"
                                value={newPassword}
                                onChange={(e) => setNewPassword(e.target.value)}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500"
                                required
                            />
                        </div>

                        <div className="mb-6">
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Xác nhận mật khẩu
                            </label>
                            <input
                                type="password"
                                value={confirmPassword}
                                onChange={(e) => setConfirmPassword(e.target.value)}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500"
                                required
                            />
                        </div>

                        <button
                            type="submit"
                            disabled={isLoading}
                            className="w-full py-2.5 bg-[#27B5FC] text-white rounded-full hover:bg-[#27B5FC]/80 disabled:opacity-50"
                        >
                            {isLoading ? "Đang xử lý..." : "Đặt lại mật khẩu"}
                        </button>

                        <div className="mt-4 text-center">
                            <Link
                                to="/sign-in"
                                className="text-[#27B5FC] hover:underline"
                            >
                                Quay lại đăng nhập
                            </Link>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default ResetPassword;