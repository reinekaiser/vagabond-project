import { useEffect, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { BsCheckCircle } from "react-icons/bs";
import dayjs from 'dayjs';
import { useCaptureHotelPaypalOrderAndSaveHotelBookingMutation, useCaptureHotelVnpayOrderMutation } from "../redux/api/hotelBookingApiSlice";
import { CircularProgress, Box } from "@mui/material";

const HotelCheckoutSuccess = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const orderID = searchParams.get("token") || searchParams.get("vnp_TxnRef");

    const [booking, setBooking] = useState(null);

    const [capturePaypalOrder, { isLoading: loadingPaypal, error: paypalError }] =
        useCaptureHotelPaypalOrderAndSaveHotelBookingMutation();
    const [captureVnpayOrder, { isLoading: loadingVnpay, error: vnpayError }] =
        useCaptureHotelVnpayOrderMutation();

    const localHotelBooingData = JSON.parse(
        localStorage.getItem("pendingHotelBooking")
    );

    useEffect(() => {
        const confirmOrder = async () => {
            try {
                if (!orderID || !localHotelBooingData) return;

                if (localHotelBooingData?.paymentMethod === "paypal") {

                    const res = await capturePaypalOrder({
                        orderID,
                        ...localHotelBooingData,
                    }).unwrap();

                    setBooking(res.order);

                    localStorage.removeItem("pendingHotelBooking");
                }
                else if (localHotelBooingData?.paymentMethod === "vnpay") {

                    const allParams = Object.fromEntries(searchParams.entries());
                    const res = await captureVnpayOrder({
                        allParams, request: localHotelBooingData
                    }).unwrap();

                    if (res.success) {
                        setBooking(res.order);
                    }
                    else {
                        window.location.href = res.url;
                    }
                }

            } catch (err) {
                console.error("Lỗi khi lưu đơn hàng:", err);
            }
        };

        confirmOrder();
    }, [orderID]);

    if (loadingPaypal || loadingVnpay) {
        return (
            <Box
                display="flex"
                justifyContent="center"
                alignItems="center"
                minHeight="100vh"
            >
                <CircularProgress />
            </Box>
        );
    };
    if (paypalError || vnpayError) return <p>Đã có lỗi xảy ra khi xác nhận thanh toán.</p>;
    if (!booking) return null;

    return (
        <div className="min-h-screen flex items-center justify-center">
            <div className="max-w-[440px] m-auto p-6 bg-white shadow-lg rounded-lg border">
                <div className="flex flex-col items-center text-center">
                    <BsCheckCircle className="text-green-500 w-[50px] h-[50px]" />
                    <h2 className="text-2xl font-bold ">Thanh toán thành công</h2>
                    <p className="text-gray-600 mt-2 font-semibold">
                        Cảm ơn bạn đã đặt chỗ. Đặt chỗ của bạn đang được xử lý
                    </p>
                </div>

                <hr className="my-6" />

                <div className="space-y-3 text-base text-gray-700">
                    <div className="flex justify-between">
                        <span className="font-medium">Số tiền thanh toán:</span>
                        <span className="font-semibold text-orange_primary">{booking.totalPrice.toLocaleString("vi-VN")} VND</span>
                    </div>
                    <div className="flex justify-between">
                        <span className="font-medium">Phương thức thanh toán:</span>
                        <span className="font-semibold">
                            {booking.paymentMethod.charAt(0).toUpperCase() + booking.paymentMethod.slice(1)}
                        </span>
                    </div>
                    <div className="flex justify-between">
                        <span className="font-medium">Ngày thanh toán:</span>
                        <span className="font-semibold">
                            {dayjs(booking.createdAt).locale('vi').format('D [thg] M, YYYY [lúc] HH:mm')}
                        </span>
                    </div>
                </div>

                <div className="mt-6 flex justify-center">
                    <Link to={"/"} className="bg-primary text-white px-4 py-2 rounded hover:opacity-80 op transition">
                        Tiếp tục trải nghiệm
                    </Link>
                </div>
            </div>
        </div>
    );
};

export default HotelCheckoutSuccess;
