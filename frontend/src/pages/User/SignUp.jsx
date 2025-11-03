import React, { useState, useRef } from "react";
import { Link, useNavigate } from "react-router-dom";
import { FaEye, FaEyeSlash } from "react-icons/fa";
import { toast } from "react-toastify";
import { useDispatch } from "react-redux";
import { setCredentials } from "../../redux/features/authSlice";
import { useLazyGetUserQuery, useRegisterMutation, useSendOtpMutation } from "../../redux/api/authApiSlice";

const SignUp = () => {
    const navigate = useNavigate();
    const dispatch = useDispatch();
    const [showPassword, setShowPassword] = useState(false);
    const [agreeTerms, setAgreeTerms] = useState(false);
    const [loading, setLoading] = useState(false);
    const [otpSent, setOtpSent] = useState(false);

    const [sendOtp] = useSendOtpMutation();
    const [register] = useRegisterMutation();
    const [getUser, { data: userData, error, isLoading }] = useLazyGetUserQuery();

    const [formData, setFormData] = useState({
        email: "",
        password: "",
        confirmPassword: "",
        firstName: "",
        lastName: "",
    });

    const togglePasswordVisibility = () => {
        setShowPassword(!showPassword);
    };

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value,
        });
    };
    const handleSendOtp = async (e) => {
        e.preventDefault();

        if (formData.password !== formData.confirmPassword) {
            toast.error("Mật khẩu xác nhận không khớp");
            return;
        }
        if (formData.password.length < 6) {
            toast.error("Mật khẩu phải có ít nhất 6 ký tự");
            return;
        }
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(formData.email)) {
            toast.error("Email không hợp lệ");
            return;
        }
        if (!agreeTerms) {
            toast.error("Vui lòng đồng ý với Điều khoản dịch vụ và Chính sách bảo mật trước khi đăng ký");
            return;
        }
        try {
            setLoading(true);
            const res = await sendOtp({ email: formData.email }).unwrap();
            toast.success("Mã OTP đã được gửi tới email của bạn!");
            setOtpSent(true);
        } catch (err) {
            toast.error(err?.data?.message || "Gửi OTP thất bại!");
        } finally {
            setLoading(false);
        }
    };
    const handleVerifyOtp = async (otpValue) => {
        if (!otpValue) {
            toast.error("Vui lòng nhập mã OTP");
            return;
        }
        try {
            setLoading(true);
            await register({
                firstName: formData.firstName,
                lastName: formData.lastName,
                email: formData.email,
                password: formData.password,
                otp: otpValue,
            }).unwrap();
            const userData = await getUser();
            dispatch(setCredentials(userData.data));
            toast.success("Đăng ký thành công!");
            navigate("/");
        } catch (err) {
            toast.error("Xác thực OTP thất bại! " + err);
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
            <div className="w-full min-h-screen flex items-center justify-center bg-white">
                <div className="w-full p-8">
                    <h1 className="text-2xl font-bold text-center mb-2">
                        Đăng ký
                    </h1>
                    <p className="text-center mb-6">
                        Đã có tài khoản?{" "}
                        <Link
                            to="/sign-in"
                            className="text-[#27B5FC] hover:underline"
                        >
                            Đăng nhập
                        </Link>
                    </p>
                    {!otpSent ? (
                        <form onSubmit={handleSendOtp} className="w-2/3 mx-auto">
                            <div className="mb-4">
                                <label
                                    htmlFor="firstName"
                                    className="block text-sm font-medium text-gray-700 mb-1"
                                >
                                    Họ
                                </label>
                                <input
                                    type="text"
                                    id="firstName"
                                    name="firstName"
                                    value={formData.firstName}
                                    onChange={handleChange}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                    required
                                />
                            </div>
                            <div className="mb-4">
                                <label
                                    htmlFor="lastName"
                                    className="block text-sm font-medium text-gray-700 mb-1"
                                >
                                    Tên
                                </label>
                                <input
                                    type="text"
                                    id="lastName"
                                    name="lastName"
                                    value={formData.lastName}
                                    onChange={handleChange}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                    required
                                />
                            </div>
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
                                    name="email"
                                    value={formData.email}
                                    onChange={handleChange}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                    required
                                />
                            </div>

                            <div className="mb-4">
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
                                        name="password"
                                        value={formData.password}
                                        onChange={handleChange}
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

                            <div className="mb-4">
                                <label
                                    htmlFor="confirmPassword"
                                    className="block text-sm font-medium text-gray-700 mb-1"
                                >
                                    Nhập lại mật khẩu
                                </label>
                                <input
                                    type={showPassword ? "text" : "password"}
                                    id="confirmPassword"
                                    name="confirmPassword"
                                    value={formData.confirmPassword}
                                    onChange={handleChange}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                    required
                                />
                            </div>

                            <div className="mb-6">
                                <div className="flex items-center">
                                    <input
                                        type="checkbox"
                                        id="terms"
                                        checked={agreeTerms}
                                        onChange={(e) => setAgreeTerms(e.target.checked)}
                                        className="h-4 w-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
                                    />
                                    <label
                                        htmlFor="terms"
                                        className="ml-2 block text-sm text-gray-700"
                                    >
                                        Đồng ý với{" "}
                                        <Link
                                            to="/terms"
                                            className="text-blue-600 hover:underline"
                                        >
                                            điều khoản dịch vụ
                                        </Link>{" "}
                                        và{" "}
                                        <Link
                                            to="/privacy"
                                            className="text-blue-600 hover:underline"
                                        >
                                            chính sách bảo mật
                                        </Link>
                                    </label>
                                </div>
                            </div>

                            <button
                                type="submit"
                                disabled={loading}
                                className="w-full py-2.5 bg-[#27B5FC] text-white rounded-full hover:bg-[#27B5FC]/80"
                            >
                                {loading ? "Đang gửi OTP..." : "Gửi mã xác thực"}
                            </button>
                        </form>
                    ) : (
                        <OtpInputForm handleVerifyOtp={handleVerifyOtp} handleSendOtp={handleSendOtp} loading={loading} />
                    )}
                </div>
            </div>
        </div>
    );
};

const OtpInputForm = ({ handleVerifyOtp, handleSendOtp, loading }) => {
    const [otp, setOtp] = useState(["", "", "", "", "", ""]);
    const inputsRef = useRef([]);

    const handleChange = (value, index) => {
        if (/^[0-9]?$/.test(value)) {
            const newOtp = [...otp];
            newOtp[index] = value;
            setOtp(newOtp);

            if (value && index < 5) {
                inputsRef.current[index + 1].focus();
            }
        }
    };
    const handleKeyDown = (e, index) => {
        if (e.key === "Backspace" && !otp[index] && index > 0) {
            inputsRef.current[index - 1].focus();
        }
    };
    const onSubmit = (e) => {
        e.preventDefault();
        const otpValue = otp.join("");
        handleVerifyOtp(otpValue);
    };
    return (
        <form onSubmit={onSubmit} className="w-2/3 mx-auto">
            <h2 className="text-lg text-center font-semibold mb-4">
                Nhập mã OTP được gửi tới email
            </h2>

            <div className="flex justify-between mb-6">
                {otp.map((digit, index) => (
                    <input
                        key={index}
                        ref={(el) => (inputsRef.current[index] = el)}
                        type="text"
                        maxLength="1"
                        value={digit}
                        onChange={(e) => handleChange(e.target.value, index)}
                        onKeyDown={(e) => handleKeyDown(e, index)}
                        className="w-12 h-12 border border-gray-300 rounded-md text-center text-xl font-semibold focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                ))}
            </div>

            <button
                type="submit"
                disabled={loading}
                className="w-full py-2.5 bg-[#27B5FC] text-white rounded-full hover:bg-[#27B5FC]/80"
            >
                {loading ? "Đang xác thực..." : "Xác nhận OTP"}
            </button>

            <p className="text-center text-sm mt-4 text-gray-600">
                Không nhận được mã?{" "}
                <button
                    type="button"
                    onClick={handleSendOtp}
                    className="text-[#27B5FC] hover:underline"
                >
                    Gửi lại
                </button>
            </p>
        </form>
    );
}

export default SignUp;