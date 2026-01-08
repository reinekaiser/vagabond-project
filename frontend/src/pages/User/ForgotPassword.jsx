import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { useSendResetPasswordLinkMutation } from '../../redux/api/authApiSlice';
import { Modal } from "antd";
import { useEffect } from "react";


const ForgotPassword = () => {
    const navigate = useNavigate();
    const [email, setEmail] = useState('');
    const [sendResetLink, { isLoading: loading, isSuccess }] = useSendResetPasswordLinkMutation();
    const [open, setOpen] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!email) {
            toast.error('Vui lòng nhập email');
            return;
        }
        try {
            const res = await sendResetLink(email).unwrap();
            toast.success(res.message);
        } catch (error) {
            console.log("Error send email:", error)
            toast.error("Lỗi gửi email")
        }
    };


    useEffect(() => {
        if (isSuccess) {
            setOpen(true);
        }
    }, [isSuccess]);

    return (
        <div className="grid grid-cols-2 h-screen bg-cover bg-center"
            style={{ backgroundImage: "url('/images/login/background.png')" }}>
            <div></div>
            <div className="items-center justify-center h-screen">
                <div className="bg-white w-[1/2] h-full justify-center rounded-lg shadow-xl p-8 mx-auto">
                    <h1 className="text-2xl font-bold text-center mb-2">Forgot Password</h1>

                    <p className="text-center text-gray-600 mb-4">
                        Enter your email and we'll send you a link to reset your password
                    </p>

                    <p className="text-center mb-6">
                        Remember your password? <Link to="/sign-in" className="text-[#27B5FC] hover:underline">Log in</Link>
                    </p>

                    <form onSubmit={handleSubmit} className="w-2/3 mx-auto">
                        <div className="mb-6">
                            <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-1">
                                Email
                            </label>
                            <input
                                type="email"
                                id="email"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                placeholder="Nhập địa chỉ email của bạn"
                                required
                            />
                        </div>

                        <button
                            type="submit"
                            disabled={loading}
                            className="w-full py-2.5 bg-[#27B5FC] text-white rounded-full hover:bg-[#27B5FC]/80 focus:outline-none focus:ring-2 focus:ring-gray-500 mb-6 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            {loading ? 'Sending...' : 'Send reset link'}
                        </button>
                    </form>
                </div>
            </div>

            <Modal
                open={open}
                onOk={() => setOpen(false)}
                onCancel={() => setOpen(false)}
                okText="OK"
                cancelButtonProps={{ style: { display: "none" } }}
            >
                <h2 className="text-lg font-semibold mb-2">Email đã được gửi</h2>
                <p className='text-gray-500'>
                    Chúng tôi đã gửi link reset mật khẩu tới email:
                    <span className="text-black font-medium"> {email}</span>
                </p>
                <p className="mt-1 text-gray-500 text-sm">
                    Vui lòng kiểm tra hộp thư đến hoặc thư rác (spam).
                </p>
            </Modal>
        </div>
    );
};

export default ForgotPassword;

