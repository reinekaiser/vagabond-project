import React from "react";
import { useGoogleLogin } from "@react-oauth/google";
import { FcGoogle } from "react-icons/fc";
import { toast } from "react-toastify";
import { useDispatch } from "react-redux";
import { useNavigate } from "react-router-dom";
import { useLazyGetUserQuery, useLoginWithGoogleMutation } from "../redux/api/authApiSlice";
import { setCredentials } from "../redux/features/authSlice";


const GoogleLoginButton = ({ text = "Đăng nhập bằng Google" }) => {
    const dispatch = useDispatch();
    const navigate = useNavigate();

    const [loginWithGoogle, { isLoading }] = useLoginWithGoogleMutation();
    const [getUser] = useLazyGetUserQuery();

    const handleGoogleLoginSuccess = async (codeResponse) => {
        try {

            await loginWithGoogle({ code: codeResponse.code }).unwrap();
            const userData = await getUser().unwrap();
            dispatch(setCredentials(userData));
            toast.success("Đăng nhập Google thành công!");
            navigate(userData.role === "ADMIN" ? "/admin/profile" : "/");
        } catch (err) {
            console.error("Google Login Error:", err);
            toast.error(err?.data?.message || "Đăng nhập Google thất bại!");
        }
    };

    const login = useGoogleLogin({
        onSuccess: handleGoogleLoginSuccess,
        onError: (error) => {
            console.error("Google Login Failed:", error);
            toast.error("Không thể kết nối tới Google");
        },
        flow: "auth-code",
    });

    return (
        <button
            type="button"
            onClick={() => login()}
            disabled={isLoading}
            className="w-full flex items-center justify-center gap-2 py-2.5 border border-gray-300 rounded-full hover:bg-gray-50 transition-colors duration-200 mb-4 disabled:opacity-50 disabled:cursor-not-allowed"
        >
            {isLoading ? (
                <span className="text-gray-500 text-sm">Đang xử lý...</span>
            ) : (
                <>
                    <FcGoogle size={22} />
                    <span className="text-gray-700 font-medium">{text}</span>
                </>
            )}
        </button>
    );
};

export default GoogleLoginButton;