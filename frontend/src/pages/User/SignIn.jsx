import React, { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { FaFacebook, FaEye, FaEyeSlash } from "react-icons/fa";
import { toast } from "react-toastify";
import { useDispatch, useSelector } from "react-redux";
import { setCredentials } from "../../redux/features/authSlice";
import { useLoginMutation, useLazyGetUserQuery } from "../../redux/api/authApiSlice";

const SignIn = () => {
    const dispatch = useDispatch();
    const navigate = useNavigate();
    const [showPassword, setShowPassword] = useState(false);
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [loading, setLoading] = useState(false);

    const [login] = useLoginMutation();
    const [getUser] = useLazyGetUserQuery();

    const { user } = useSelector((state) => state.auth);
    useEffect(() => {
        if (user) {
            navigate(user.role === "ADMIN" ? "/admin/profile" : "/");
        }
    }, [user, navigate]);

    const togglePasswordVisibility = () => {
        setShowPassword(!showPassword);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            await login({ email, password }).unwrap();
            const userData = await getUser().unwrap();
            dispatch(setCredentials(userData));
            toast.success("Đăng nhập thành công");
            navigate(userData.role === "ADMIN" ? "/admin/profile" : "/");
        } catch (err) {
            console.error("Login error:", err);
            toast.error("Đăng nhập thất bại");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div
            className="grid grid-cols-2 min-h-screen bg-cover bg-center"
            style={{ backgroundImage: "url('/images/login/background.png')" }}
        >
            <div></div>
            <div className="flex items-center justify-center bg-white">
                <div className="w-full p-8">
                    <h1 className="text-2xl font-bold text-center mb-2">
                        Đăng nhập
                    </h1>

                    <p className="text-center mb-6">
                        Chưa có tài khoản?{" "}
                        <Link
                            to="/sign-up"
                            className="text-[#27B5FC] hover:underline"
                        >
                            Đăng ký
                        </Link>
                    </p>

                    <form onSubmit={handleSubmit} className="w-2/3 mx-auto">
                        <div className="mb-4">
                            <label
                                htmlFor="email"
                                className="block text-sm font-medium text-gray-700 mb-1"
                            >
                                Email
                            </label>
                            <input
                                type="email"
                                id="email"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                required
                            />
                        </div>

                        <div className="mb-2">
                            <label
                                htmlFor="password"
                                className="block text-sm font-medium text-gray-700 mb-1"
                            >
                                Mật khẩu
                            </label>
                            <div className="relative">
                                <input
                                    type={showPassword ? "text" : "password"}
                                    id="password"
                                    value={password}
                                    onChange={(e) =>
                                        setPassword(e.target.value)
                                    }
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                    required
                                />
                                <button
                                    type="button"
                                    onClick={togglePasswordVisibility}
                                    className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500 flex items-center"
                                >
                                    {showPassword ? (
                                        <FaEyeSlash className="mr-1" />
                                    ) : (
                                        <FaEye className="mr-1" />
                                    )}
                                    <span>{showPassword ? "Ẩn" : "Hiện"}</span>
                                </button>
                            </div>
                        </div>

                        <div className="text-right mb-6">
                            <Link
                                to="/forgot-password"
                                className="text-sm text-gray-600 hover:underline"
                            >
                                Quên mật khẩu?
                            </Link>
                        </div>

                        <button
                            type="submit"
                            disabled={loading}
                            className="w-full py-2.5 bg-[#27B5FC] text-white rounded-full hover:bg-[#27B5FC]/80 focus:outline-none focus:ring-2 focus:ring-gray-500 transition-colors duration-200"
                        >
                            {loading ? "Logging in..." : "Log in"}
                        </button>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default SignIn;
