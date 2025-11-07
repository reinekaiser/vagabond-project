import { useSearchParams, useParams } from "react-router-dom";
import React, { useState, useEffect, useRef } from "react";
import { DatePicker, message, Drawer } from "antd";
import { Link, useNavigate } from "react-router-dom";
import {
    useGetHotelByIdQuery,
    useGetAvailableRoomsQuery,
} from "../../redux/api/hotelApiSlice";
import { FaAngleLeft } from "react-icons/fa6";
import { PiCity } from "react-icons/pi";
import { LuMapPin, LuHotel } from "react-icons/lu";
import { GrLocation } from "react-icons/gr";
import { IoMdClose } from "react-icons/io";
import { IoMdCheckmarkCircleOutline } from "react-icons/io";
import { AiOutlinePlus, AiOutlineMinus } from "react-icons/ai";
import { CiSearch } from "react-icons/ci";
import dayjs from "dayjs";
import weekday from "dayjs/plugin/weekday";
import localeData from "dayjs/plugin/localeData";
import customParseFormat from "dayjs/plugin/customParseFormat";
import { FaBuilding, FaAngleRight } from "react-icons/fa6";
import HotelInformation from "../../components/HotelInformation";
import ImageGalleryFromCloudinary from "../../components/ImageGalleryFromCloudinary";
import { CLOUDINARY_BASE_URL } from "../../constants/hotel";
import RoomTypesInformation from "../../components/RoomTypesInfomation";
import { useGetSearchHotelSuggestionQuery } from "../../redux/api/hotelApiSlice";
import { skipToken } from "@reduxjs/toolkit/query";
dayjs.extend(customParseFormat);
const dateFormat = "DD/MM/YYYY";
dayjs.extend(weekday);
dayjs.extend(localeData);
import { Box, CircularProgress } from "@mui/material";
import ReviewCard from "../../components/ReviewCard";
const { RangePicker } = DatePicker;

const HotelDetails = () => {
    const navigate = useNavigate();
    const param = useParams();
    const [searchParams] = useSearchParams();
    const initType = searchParams.get("type");
    const initLocation = searchParams.get("location");
    const initCheckIn = searchParams.get("checkIn");
    const initCheckOut = searchParams.get("checkOut");
    const initRooms = parseInt(searchParams.get("rooms"));
    const initAdults = parseInt(searchParams.get("adults"));

    const [location, setLocation] = useState({
        type: initType || '',
        name: initLocation || ''
    });
    const [checkIn, setCheckIn] = useState(
        initCheckIn ? dayjs(initCheckIn, dateFormat) : null
    );
    const [checkOut, setCheckOut] = useState(
        initCheckOut ? dayjs(initCheckOut, dateFormat) : null
    );
    const [rooms, setRooms] = useState(initRooms || 1);
    const [adults, setAdults] = useState(initAdults || 2);
    const [openDetails, setOpenDetails] = useState(false);

    const { data: hotel, isLoading: isHotelLoading } = useGetHotelByIdQuery(
        param._id
    );

    const { data: availableRooms, isLoading: isAvaiRoomsLoading } =
        useGetAvailableRoomsQuery({
            id: param._id,
            checkInDate: checkIn?.format('YYYY-MM-DD'),
            checkOutDate: checkOut?.format('YYYY-MM-DD'),
            numRooms: rooms,
            numAdults: adults,
        });

    console.log(availableRooms)

    const showDrawer = () => setOpenDetails(true);
    const onClose = () => setOpenDetails(false);
    const handleSearch = async () => {
        if (location.name != initLocation) {
            if (!location.name || !checkIn || !checkOut) {
                messageApi.error("Điền đầy đủ các thông tin");
                return;
            }
            const params = new URLSearchParams();
            params.set('type', location.type);
            params.set("location", location.name);
            if (checkIn) params.set("checkIn", checkIn.format(dateFormat));
            if (checkOut) params.set("checkOut", checkOut.format(dateFormat));
            params.set("rooms", rooms);
            params.set("adults", adults);

            navigate(`/hotels/search?${params.toString()}`);
        } else {
            if (!location.name || !checkIn || !checkOut) {
                messageApi.error("Điền đầy đủ các thông tin");
                return;
            }
            const params = new URLSearchParams();
            params.set('type', location.type);
            params.set("location", location.name);
            if (checkIn) params.set("checkIn", checkIn.format(dateFormat));
            if (checkOut) params.set("checkOut", checkOut.format(dateFormat));
            params.set("rooms", rooms);
            params.set("adults", adults);

            navigate(`/hotels/${param._id}?${params.toString()}`);
        }
    };

    const handleBookingRoom = (roomTypeId, roomId) => {
        const params = new URLSearchParams();
        params.set("location", location.name);
        params.set("checkIn", checkIn.format(dateFormat));
        params.set("checkOut", checkOut.format(dateFormat));
        params.set("rooms", rooms);
        params.set("adults", adults);
        params.set("roomTypeId", roomTypeId);
        params.set("roomId", roomId);

        navigate(`/hotels/${hotel._id}/pay?${params.toString()}`);
    };

    const chooseRoomRef = useRef(null);
    const scrollToRef = (ref) => {
        ref.current?.scrollIntoView({ behavior: "smooth" });
    };
    const [messageApi, contextMessageHolder] = message.useMessage();

    useEffect(() => {
        const url = new URL(window.location.href);
        if (url.searchParams.has("token")) {
            url.searchParams.delete("token");

            window.history.replaceState({}, "", url.pathname + url.search);
        }
    }, []);

    if (isHotelLoading || isAvaiRoomsLoading) {
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
    }

    return (
        <div>
            <div>
                {contextMessageHolder}
                <div className="bg-blue-900 py-2">
                    <div className="container mx-auto">
                        <form onSubmit={(e) => e.preventDefault()}>
                            <SearchHotel
                                location={location} setLocation={setLocation}
                                checkIn={checkIn} setCheckIn={setCheckIn}
                                checkOut={checkOut} setCheckOut={setCheckOut}
                                rooms={rooms} setRooms={setRooms}
                                adults={adults} setAdults={setAdults}
                                handleSearch={handleSearch}
                            />
                        </form>
                    </div>
                </div>
                <div className="container mx-auto">
                    <div className="mt-5 mb-2">
                        <button
                            className="flex items-center text-[18px] font-semibold px-2 py-1 rounded-md"
                            onClick={() =>
                                navigate(
                                    `/hotels/search?${searchParams.toString()}`
                                )
                            }
                        >
                            <FaAngleLeft className="mr-3 hover:text-slate-400 duration-300" />
                            Quay lại
                        </button>
                    </div>
                    <div className="mb-5">
                        <ImageGalleryFromCloudinary
                            existingImages={hotel.img}
                            option={1}
                        />
                    </div>
                    <div className="grid grid-cols-[70%_30%] mb-5">
                        <div>
                            <p className="text-[24px] font-semibold mb-5">
                                {hotel.name}
                            </p>
                            <div className="flex items-start">
                                <FaBuilding className="text-[24px] mr-2 mt-[2px] text-orange-500" />
                                <div className="relative">
                                    <p className="text-[15px] font-light leading-relaxed">
                                        {hotel.description.slice(0, 210)}...
                                    </p>
                                    <button
                                        className="absolute right-4 bottom-0 text-sm flex items-center underline"
                                        onClick={showDrawer}
                                    >
                                        Xem chi tiết{" "}
                                        <FaAngleRight className="ml-1" />
                                    </button>
                                </div>
                                <Drawer
                                    title={null}
                                    placement="right"
                                    closable={false}
                                    width={670}
                                    onClose={onClose}
                                    open={openDetails}
                                >
                                    <div className="flex justify-start mb-4">
                                        <button
                                            onClick={onClose}
                                            className="text-xl font-bold focus:outline-none focus:ring-0 focus:border-0 active:border-0"
                                        >
                                            <IoMdClose className="text-[28px] hover:text-orange-500 duration-300" />
                                        </button>
                                    </div>
                                    <div className="overflow-auto max-h-[calc(100vh-160px)] pr-2">
                                        <div className="overflow-hidden rounded-xl h-[280px] mb-6">
                                            <img
                                                src={`${CLOUDINARY_BASE_URL}/${hotel.img[0]}`}
                                                alt="only"
                                                className="w-full h-full object-cover"
                                            />
                                        </div>
                                        <HotelInformation finalData={hotel} />
                                    </div>
                                    <div className="absolute bottom-0 left-0 w-full px-6 py-4 bg-white border-t">
                                        <button
                                            className="w-full bg-orange-500 text-white py-3 rounded-xl hover:bg-orange-600 transition"
                                            onClick={() => {
                                                scrollToRef(chooseRoomRef);
                                                onClose();
                                            }}
                                        >
                                            Chọn phòng
                                        </button>
                                    </div>
                                </Drawer>
                            </div>
                        </div>
                        <div>
                            <p className="text-end font-semibold text-[24px]">
                                {Number(hotel.fromPrice).toLocaleString("vi-VN")} ₫{" "}
                                <span className="text-[14px] text-gray-500 font-normal">
                                    Mỗi đêm
                                </span>
                            </p>
                            <div className="flex justify-end items-end mt-1">
                                <button
                                    className="py-2 p-16 bg-orange-500 rounded-lg text-white"
                                    onClick={() => scrollToRef(chooseRoomRef)}
                                >
                                    Chọn phòng
                                </button>
                            </div>
                        </div>
                    </div>
                    <div className="grid grid-cols-3 gap-4 mb-12">
                        <div className="p-4 border border-gray-300 rounded-lg">
                            {/* <ReviewCard
                                hotelId={hotel._id}
                                type="Hotel"
                            /> */}
                        </div>
                        <div className="p-4 border border-gray-300 rounded-lg grid grid-cols-2">
                            {hotel?.serviceFacilities.slice(0, 4).map((fc, index) => (
                                <p
                                    key={index}
                                    className="flex items-center text-gray-600 text-[14px] mb-2"
                                >
                                    <IoMdCheckmarkCircleOutline className="ml-1 mr-3 text-[18px]" />
                                    {fc.name}
                                </p>
                            ))}
                            <button
                                className="ml-2 text-sm flex items-center underline"
                                onClick={showDrawer}
                            >
                                Xem chi tiết <FaAngleRight className="ml-1" />
                            </button>
                        </div>
                        <div className="p-4 border border-gray-300 rounded-lg text-gray-600 space-y-3">
                            <div className="flex items-center">
                                <p className="text-[16px]">Địa chỉ:</p>
                            </div>
                            <div className="flex items-center ml-3">
                                <LuMapPin className="text-[18px] mr-3" />
                                <p className="text-[14px]">{hotel.address}</p>
                            </div>
                            <div className="flex items-center ml-3">
                                <PiCity className="text-[20px] mr-3" />
                                <p className="text-[14px]">{hotel.city.name}</p>
                            </div>
                        </div>
                    </div>

                    <div ref={chooseRoomRef} className="">
                        <div className="flex items-center space-x-2 mb-8">
                            <div className="w-2 h-7 bg-orange-500 rounded"></div>
                            <h2 className="text-[20px] font-semibold">
                                Chọn phòng của bạn
                            </h2>
                        </div>
                        <RoomTypesInformation
                            roomTypesData={availableRooms}
                            review={2}
                            handleBookingRoom={handleBookingRoom}
                        />
                    </div>
                </div>
            </div>
        </div>
    );
};

const SearchHotel = ({ location, setLocation, checkIn, setCheckIn, checkOut, setCheckOut, rooms, setRooms, adults, setAdults, handleSearch }) => {

    const [openDr, setOpenDr] = useState(false);
    const inputRef = useRef(null);
    const { data: searchSuggestions, error, isFetching } = useGetSearchHotelSuggestionQuery(
        location.name ? { key: location.name } : skipToken,
        {
            refetchOnMountOrArgChange: true,
            refetchOnFocus: false,
            refetchOnReconnect: false,
            keepUnusedDataFor: 0,
        }
    );

    const onLocationChange = (e) => {
        const name = e.target.value;
        setLocation({ type: '', name });
        setOpenDr(Boolean(name && name.trim().length > 0));
    };

    useEffect(() => {
        const selectFirstSuggestion = () => {
            if (
                openDr &&
                (searchSuggestions?.cities?.length > 0 || searchSuggestions?.hotels?.length > 0) &&
                location.name !== ''
            ) {
                const firstCity = searchSuggestions?.cities?.[0];
                const firstHotel = searchSuggestions?.hotels?.[0];
                const firstItem = firstCity
                    ? { type: 'city', name: firstCity.name }
                    : firstHotel
                        ? { type: 'hotel', name: firstHotel.name }
                        : null;
                if (firstItem) setLocation(firstItem);
                setOpenDr(false);
            }
        };
        const handleClickOutside = (event) => {
            if (inputRef.current && !inputRef.current.contains(event.target)) {
                selectFirstSuggestion();
            }
        };
        const handleEnterPress = (event) => {
            if (event.key === 'Enter') {
                selectFirstSuggestion();
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        document.addEventListener('keydown', handleEnterPress);

        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
            document.removeEventListener('keydown', handleEnterPress);
        };
    }, [location.name, setLocation, openDr, searchSuggestions]);

    const [messageApi, contextMessageHolder] = message.useMessage();

    return (
        <div className={`flex items-center gap-2`}>
            {contextMessageHolder}
            <div ref={inputRef} className='relative rounded-lg bg-slate-100 py-2 px-3 w-full'>
                {/* <label className='text-[12px] text-gray-500 font-medium'>Địa điểm</label> */}
                <input
                    value={location.name}
                    onChange={onLocationChange}
                    type='text'
                    placeholder='Nhập địa điểm, tên khách sạn'
                    className={`w-full outline-none bg-transparent placeholder-gray-400 placeholder:text-[14px] placeholder:font-normal font-semibold`}
                />
                <div className='absolute left-0 top-full mt-1 w-full z-10 rounded-lg'>
                    {openDr && location.name?.trim() !== '' && (
                        <div className='absolute left-0 top-full mt-1 w-full z-10 rounded-lg'>
                            <div className='border border-gray-50 bg-white shadow-lg rounded-lg overflow-hidden'>
                                {isFetching && (
                                    <div className='py-4 text-center text-gray-500 text-sm flex items-center justify-center gap-2'>
                                        <span className="animate-spin rounded-full h-4 w-4 border-t-2 border-primary"></span>
                                        <span>Đang tìm kiếm...</span>
                                    </div>
                                )}
                                {!isFetching && searchSuggestions && (
                                    <div className='max-h-64 overflow-y-auto'>
                                        {searchSuggestions.cities?.length > 0 && (
                                            <div>
                                                {searchSuggestions.cities.map((city, index) => (
                                                    <div
                                                        key={`city-${index}`}
                                                        className='hover:bg-gray-100 p-4 duration-300 text-base cursor-pointer'
                                                        onClick={() => {
                                                            setLocation({ type: 'city', name: city.name });
                                                            setOpenDr(false);
                                                        }}
                                                    >
                                                        <div className='flex items-center mb-1'>
                                                            <GrLocation className='text-gray-500 w-5 h-5 mr-3 flex-shrink-0' />
                                                            <span className='truncate max-w-[90%]'>{city.name}</span>
                                                        </div>
                                                        <div className='text-sm text-gray-400 pl-8'>Thành phố</div>
                                                    </div>
                                                ))}
                                            </div>
                                        )}
                                        {searchSuggestions.hotels?.length > 0 && (
                                            <div>
                                                {searchSuggestions.hotels.map((hotel, index) => (
                                                    <div
                                                        key={`hotel-${index}`}
                                                        className='hover:bg-gray-100 p-4 duration-300 text-base cursor-pointer'
                                                        onClick={() => {
                                                            setLocation({ type: 'hotel', name: hotel.name });
                                                            setOpenDr(false);
                                                        }}
                                                    >
                                                        <div className='flex items-center mb-1'>
                                                            <LuHotel className='text-gray-500 w-5 h-5 mr-3 flex-shrink-0' />
                                                            <span className='truncate max-w-[90%]'>{hotel.name}</span>
                                                        </div>
                                                        <div className='text-sm text-gray-400 pl-8'>{hotel?.city?.name}</div>
                                                    </div>
                                                ))}
                                            </div>
                                        )}
                                        {searchSuggestions.cities?.length === 0 &&
                                            searchSuggestions.hotels?.length === 0 && (
                                                <div className='py-3 text-center text-gray-500 text-sm'>
                                                    Không tìm thấy kết quả
                                                </div>
                                            )}
                                    </div>
                                )}
                            </div>
                        </div>
                    )}
                </div>
            </div>

            <div className='rounded-lg bg-white py-[5px] px-3 w-full'>
                {/* <label className='text-[12px] text-gray-500 font-medium'>Ngày nhận / trả phòng</label> */}
                <RangePicker
                    className='rounded-lg bg-transparent w-full custom-range-picker font-semibold'
                    placeholder={['Chọn ngày', 'Chọn ngày']}
                    style={{ width: '100%' }}
                    suffixIcon={false}
                    value={checkIn && checkOut ? [checkIn, checkOut] : null}
                    format={dateFormat}
                    onChange={(dates) => {
                        if (dates) {
                            const [start, end] = dates;
                            if (start.isSame(end, "day")) {
                                messageApi.error(
                                    "Ngày nhận và trả phòng không được trùng nhau!"
                                );
                                setCheckIn(null);
                                setCheckOut(null);
                                return;
                            }
                            setCheckIn(dates[0]);
                            setCheckOut(dates[1]);
                        } else {
                            setCheckIn(null);
                            setCheckOut(null);
                        }
                    }}
                    disabledDate={(current) => {
                        return current && current < dayjs().startOf('day');
                    }}
                />
            </div>

            <div className='rounded-lg bg-white py-2 px-3 w-full'>
                {/* <label className='text-[12px] text-gray-500 font-medium'>Số khách và phòng</label> */}
                <GuestRoomSelector
                    rooms={rooms}
                    setRooms={setRooms}
                    adults={adults}
                    setAdults={setAdults}
                />
            </div>

            <div>
                <button
                    type='button'
                    className='flex items-center bg-orange-500 hover:bg-orange-700 text-white rounded-lg px-3 font-semibold text-[16px] w-[130px] h-[40px] cursor-pointer  duration-300'
                    onClick={() => {
                        handleSearch();
                    }}
                >
                    <CiSearch className='text-[24px] mr-1' />
                    Tìm kiếm
                </button>
            </div>
        </div>
    );
}

const GuestRoomSelector = ({ rooms, setRooms, adults, setAdults }) => {
    const [open, setOpen] = useState(false);
    const ref = useRef(null);

    const handleChange = (type, operation) => {
        const update = (prev, min = 0) =>
            operation === "increase" ? prev + 1 : Math.max(prev - 1, min);
        if (type === "room") setRooms((prev) => update(prev, 1));
        if (type === "guest") setAdults((prev) => update(prev, 1));
    };

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (ref.current && !ref.current.contains(event.target)) {
                setOpen(false);
            }
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, []);

    return (
        <div ref={ref} className="relative w-full">
            <button
                type="button"
                className="text-[16px] font-semibold w-full text-left"
                onClick={() => setOpen((prev) => !prev)}
            >
                {`${adults} Người lớn, ${rooms} Phòng`}
            </button>
            {open && (
                <div className="absolute z-10 bg-white rounded-xl shadow-lg w-[300px] p-4 space-y-3 mt-2">
                    {[
                        { label: "Phòng", value: rooms, type: "room" },
                        { label: "Số khách", value: adults, type: "guest" },
                    ].map((item) => (
                        <div
                            key={item.type}
                            className="flex justify-between items-center border-b pb-4 last:border-b-0 last:pb-0"
                        >
                            <span className="text-base font-medium">
                                {item.label}
                            </span>
                            <div className="flex items-center space-x-3">
                                <button
                                    type="button"
                                    className="w-8 h-8 rounded bg-slate-100 flex items-center justify-center text-gray-700"
                                    onClick={() =>
                                        handleChange(item.type, "decrease")
                                    }
                                >
                                    <AiOutlineMinus size={18} />
                                </button>
                                <span className="w-5 text-center">
                                    {item.value}
                                </span>
                                <button
                                    type="button"
                                    className="w-8 h-8 rounded bg-slate-100 flex items-center justify-center text-gray-700"
                                    onClick={() =>
                                        handleChange(item.type, "increase")
                                    }
                                >
                                    <AiOutlinePlus size={18} />
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default HotelDetails;
