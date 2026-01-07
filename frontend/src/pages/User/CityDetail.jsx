import React, { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import { CLOUDINARY_BASE_URL } from "../../constants/hotel";
import { CATEGORY_OPTIONS, DURATION_OPTIONS, LANGUAGE_OPTIONS } from "../../constants/tour";
import { Checkbox, Collapse, Slider } from "antd";
import { MdOutlineKeyboardArrowDown } from "react-icons/md";
import { useGetToursQuery } from "../../redux/api/tourApiSlice";
import SelectItem from "../../components/SelectItem";
import TourCard from "../../components/TourCard";
import { Box, CircularProgress } from "@mui/material";
import { useGetReviewByCityQuery } from "../../redux/api/reviewApiSlice";
import GeneralCarousel from "../../components/GeneralCarousel";
import { FaStar } from "react-icons/fa6";
import { useGetCityQuery } from "../../redux/api/cityApiSlice";


const CityDetail = () => {
    const { cityId } = useParams();
    const { data: city, isLoading: loading } = useGetCityQuery(cityId);
    const [showFullDescription, setShowFullDescription] = useState(false);
    const [showImageGallery, setShowImageGallery] = useState(false);
    const [currentImageIndex, setCurrentImageIndex] = useState(0);

    const responsive = {
        desktop: {
            breakpoint: {
                max: 3000,
                min: 1024,
            },
            items: 2,
        },
        mobile: {
            breakpoint: {
                max: 464,
                min: 0,
            },
            items: 1,
        },
        tablet: {
            breakpoint: {
                max: 1024,
                min: 464,
            },
            items: 3,
        },
    };

    const { data: reviews, isLoading: isLoadingReview } = useGetReviewByCityQuery({
        cityId: cityId,
    });
    console.log(reviews)

    const panelStyle = {
        background: "#fff",
        border: "1px solid #e8e8e8",
    };

    const getItems = (style, fqas) =>
        fqas.map((fqa, index) => {
            const isLast = index === fqas.length - 1;
            const childStyle = !isLast
                ? style
                : {
                    ...style,
                    borderRadius: "0 0 8px 8px",
                };

            return {
                key: index,
                label: <p className="text-xl py-2 font-semibold ">{fqa.question}</p>,
                children: <p className="text-base p-6 bg-gray-100">{fqa.answer}</p>,
                style: childStyle,
            };
        });
    if (loading || !city || isLoadingReview)
        return (
            <div>
                <Box display="flex" justifyContent="center" alignItems="center" minHeight="100vh">
                    <CircularProgress />
                </Box>
            </div>
        );

    const TourInCity = () => {
        const sortOptionList = [
            { label: "Mới nhất", value: "new" },
            { label: "Đánh giá cao nhất", value: "rating" },
            { label: "Giá từ thấp đến cao", value: "price" },
        ];
        const [sort, setSort] = useState(sortOptionList[0].value);

        const [priceRange, setPriceRange] = useState([0, 6000000]);
        const [language, setLanguage] = useState([]);
        const [category, setCategory] = useState([]);
        const [duration, setDuration] = useState([]);

        const handleFiler = (item, setFilter) => {
            setFilter((prev) => {
                if (prev.includes(item)) {
                    return prev.filter((i) => i !== item);
                } else {
                    return [...prev, item];
                }
            });
        };
        const {
            data: tours,
            refetch,
            isLoading,
        } = useGetToursQuery({
            category: category.join(","),
            languageService: language.join(","),
            minPrice: priceRange[0],
            maxPrice: priceRange[1],
            duration: duration.join(","),
            sort,
            cityId,
        });

        if (isLoading) return <div></div>;

        return (
            <div className="container mx-auto flex gap-6 items-start ">
                <div className="w-[25%] p-4 border rounded-xl bg-white border-gray-200 space-y-5">
                    <div>
                        <p className="font-semibold">Danh mục tour</p>
                        <Checkbox.Group
                            value={category}
                            options={CATEGORY_OPTIONS}
                            onChange={(newCate) => {
                                setCategory(newCate);
                            }}
                            style={{
                                display: "flex",
                                flexDirection: "column",
                                gap: 8,
                                fontSize: "20px",
                                marginTop: "10px",
                                padding: "0 10px",
                            }}
                        />
                    </div>
                    <div className="">
                        <p className="font-semibold">Giá</p>
                        <Slider
                            range
                            min={0}
                            max={2000000}
                            step={100000}
                            value={priceRange}
                            onChange={(newRange) => {
                                setPriceRange(newRange);
                            }}
                            tooltip={{
                                formatter: (value) => `${value.toLocaleString("vi-VN")} VND`,
                            }}
                            styles={{
                                track: { background: "#3b82f6" }, // Màu phần được chọn (track)
                                rail: { background: "#d9d9d9" }, // Màu phần nền (rail)
                                handle: {
                                    borderColor: "#3b82f6",
                                }, // Màu handle
                            }}
                        />
                        <div className="flex items-center gap-2">
                            <div className="border rounded-md px-2 py-1 text-sm flex-1">
                                {priceRange[0].toLocaleString("vi-VN")} VND
                            </div>
                            <div className="h-[1px] w-2 bg-gray-300"></div>
                            <div className="border rounded-md px-2 py-1 text-sm">
                                {priceRange[1].toLocaleString("vi-VN")} VND
                            </div>
                        </div>
                    </div>
                    <div>
                        <p className="font-semibold">Dịch vụ ngôn ngữ</p>
                        <div className="flex gap-3 mt-3 flex-wrap">
                            {LANGUAGE_OPTIONS.map((languageOption) => (
                                <div
                                    onClick={() => handleFiler(languageOption.value, setLanguage)}
                                    key={languageOption.value}
                                    className={`cursor-pointer px-2 py-2 border rounded-lg ${language.includes(languageOption.value)
                                            ? " border-blue-500 text-blue-500 bg-white"
                                            : ""
                                        }`}
                                >
                                    {languageOption.label}
                                </div>
                            ))}
                        </div>
                    </div>
                    <div>
                        <p className="font-semibold">Thời lượng</p>
                        <div className="flex gap-3 mt-3 flex-wrap">
                            {DURATION_OPTIONS.map((durationOption) => (
                                <div
                                    key={durationOption.value}
                                    className={`cursor-pointer border px-2 py-2 rounded-lg ${duration.includes(durationOption.value)
                                            ? " border-blue-500 text-blue-500 bg-white"
                                            : ""
                                        }`}
                                    onClick={() => handleFiler(durationOption.value, setDuration)}
                                >
                                    {durationOption.label}
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
                <div className="flex-1">
                    <div className="flex items-center justify-between">
                        <p className="font-semibold">Tìm thấy {tours.totalTours} kết quả</p>
                        <SelectItem
                            selectTitle={"Sắp xếp theo"}
                            optionList={sortOptionList}
                            selected={sort}
                            setSelected={(newSort) => {
                                setSort(newSort);
                            }}
                        ></SelectItem>
                    </div>
                    <div className="">
                        <div className="grid grid-cols-3 gap-5 mt-4">
                            {tours.data?.map((tour, index) => (
                                <TourCard tour={tour} key={index}></TourCard>
                            ))}
                        </div>
                        {/* {data.total > 0 && (
                        <Pagination
                            total={data.total}
                            align="end"
                            style={{
                                marginTop: "20px",
                            }}
                            pageSize={1}
                            current={currentPage}
                            onChange={handlePageChange}
                        ></Pagination>
                    )} */}
                    </div>
                </div>
            </div>
        );
    };

    return (
        <div className="container mx-auto py-8">
            {/* Card thông tin thành phố chính */}
            <div className="bg-white rounded-2xl shadow-lg overflow-hidden mb-8">
                <div className="flex flex-col lg:flex-row">
                    {/* Thông tin thành phố - bên trái */}
                    <div className="lg:w-1/2 p-8">
                        <h1 className="text-4xl font-bold text-gray-800 mb-4">{city.name}</h1>

                        <div className="text-gray-600 mb-6 leading-relaxed">
                            {!showFullDescription && city.description.length > 200 ? (
                                <>
                                    {city.description.substring(0, 200)}...
                                    <button
                                        className="text-blue-600 hover:underline ml-2"
                                        onClick={() => setShowFullDescription(true)}
                                    >
                                        Xem thêm
                                    </button>
                                </>
                            ) : (
                                <>
                                    {city.description}
                                    {city.description.length > 200 && showFullDescription && (
                                        <button
                                            className="text-blue-600 hover:underline ml-2"
                                            onClick={() => setShowFullDescription(false)}
                                        >
                                            Thu gọn
                                        </button>
                                    )}
                                </>
                            )}
                        </div>

                        {/* Thông tin chi tiết */}
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
                            <div>
                                <div className="text-sm text-gray-500 mb-1">
                                    Thời gian tuyệt nhất để đến
                                </div>
                                <div className="font-semibold text-gray-800">
                                    {city.bestTimeToVisit}
                                </div>
                            </div>
                            <div>
                                <div className="text-sm text-gray-500 mb-1">
                                    Thời lượng lý tưởng
                                </div>
                                <div className="font-semibold text-gray-800">3 ngày</div>
                            </div>
                        </div>
                    </div>

                    {/* Hình ảnh thành phố - bên phải */}
                    <div className="lg:w-1/2 relative">
                        {city.images && city.images.length > 0 && (
                            <div className="relative h-80 lg:h-full">
                                {/* Layout ảnh: 1 ảnh lớn bên trái, 2 ảnh nhỏ bên phải */}
                                <div className="flex gap-2 h-full">
                                    {/* Ảnh chính lớn */}
                                    <div
                                        className="flex-1 relative cursor-pointer"
                                        onClick={() => setShowImageGallery(true)}
                                    >
                                        <img
                                            src={`${CLOUDINARY_BASE_URL}/${city.images[0]}`}
                                            alt={city.name}
                                            className="w-full h-full object-cover rounded-l-lg"
                                        />
                                    </div>

                                    {/* 2 ảnh nhỏ bên phải */}
                                    {city.images.length > 1 && (
                                        <div className="w-1/3 flex flex-col gap-2">
                                            {city.images[1] && (
                                                <div
                                                    className="flex-1 relative cursor-pointer"
                                                    onClick={() => {
                                                        setCurrentImageIndex(1);
                                                        setShowImageGallery(true);
                                                    }}
                                                >
                                                    <img
                                                        src={`${CLOUDINARY_BASE_URL}/${city.images[1]}`}
                                                        alt={`${city.name} - 2`}
                                                        className="w-full h-full object-cover rounded-tr-lg"
                                                    />
                                                </div>
                                            )}
                                            {city.images[2] && (
                                                <div
                                                    className="flex-1 relative cursor-pointer"
                                                    onClick={() => {
                                                        setCurrentImageIndex(2);
                                                        setShowImageGallery(true);
                                                    }}
                                                >
                                                    <img
                                                        src={`${CLOUDINARY_BASE_URL}/${city.images[2]}`}
                                                        alt={`${city.name} - 3`}
                                                        className="w-full h-full object-cover rounded-br-lg"
                                                    />

                                                    {/* Overlay với số ảnh còn lại nếu có nhiều hơn 3 ảnh */}
                                                    {city.images.length > 3 && (
                                                        <div className="absolute inset-0 bg-black/50 flex items-center justify-center rounded-br-lg">
                                                            <span className="text-white font-semibold text-lg">
                                                                +{city.images.length - 3}
                                                            </span>
                                                        </div>
                                                    )}
                                                </div>
                                            )}

                                            {/* Nếu chỉ có 2 ảnh, tạo placeholder cho ảnh thứ 3 */}
                                            {city.images.length === 2 && (
                                                <div className="flex-1 bg-gray-200 rounded-br-lg"></div>
                                            )}
                                        </div>
                                    )}

                                    {/* Nếu chỉ có 1 ảnh, hiển thị ảnh đó full width */}
                                    {city.images.length === 1 && (
                                        <div className="w-1/3 bg-gray-200 rounded-r-lg"></div>
                                    )}
                                </div>

                                {/* Nút thư viện ảnh */}
                                <button
                                    className="absolute bottom-4 right-4 bg-black/70 text-white px-4 py-2 rounded-lg flex items-center space-x-2 hover:bg-black/80 transition-colors z-10"
                                    onClick={() => setShowImageGallery(true)}
                                >
                                    <svg
                                        className="w-4 h-4"
                                        fill="none"
                                        stroke="currentColor"
                                        viewBox="0 0 24 24"
                                    >
                                        <path
                                            strokeLinecap="round"
                                            strokeLinejoin="round"
                                            strokeWidth={2}
                                            d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
                                        />
                                    </svg>
                                    <span>Thư viện ảnh</span>
                                </button>
                            </div>
                        )}
                    </div>
                </div>
            </div>

            {/* Địa điểm nổi bật */}
            <div className="mb-8">
                <h2 className="text-2xl font-semibold mb-6">Địa điểm nổi bật</h2>
                <div className="grid grid-cols-2 gap-6">
                    {city.popularPlaces &&
                        city.popularPlaces.map((place, idx) => (
                            <div
                                key={idx}
                                className="bg-white rounded-xl shadow-lg overflow-hidden hover:shadow-xl transition-shadow duration-300"
                            >
                                <div className="flex flex-col md:flex-row">
                                    {/* Hình ảnh địa điểm */}
                                    <div className="md:w-1/3 flex-1">
                                        {place.image ? (
                                            <img
                                                src={`${CLOUDINARY_BASE_URL}/${place.image}`}
                                                alt={place.name}
                                                className="w-full h-full object-cover block"
                                            />
                                        ) : (
                                            <div className="w-full h-full bg-gray-200 flex items-center justify-center">
                                                <svg
                                                    className="w-16 h-16 text-gray-400"
                                                    fill="none"
                                                    stroke="currentColor"
                                                    viewBox="0 0 24 24"
                                                >
                                                    <path
                                                        strokeLinecap="round"
                                                        strokeLinejoin="round"
                                                        strokeWidth={2}
                                                        d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
                                                    />
                                                </svg>
                                            </div>
                                        )}
                                    </div>

                                    {/* Thông tin địa điểm */}
                                    <div className="md:w-2/3 p-6 flex flex-col justify-center">
                                        <h3 className="text-xl font-bold text-gray-800 mb-3">
                                            {place.name}
                                        </h3>
                                        <p className="text-gray-600 leading-relaxed text-base">
                                            {place.description}
                                        </p>

                                        {/* Có thể thêm các thông tin khác như rating, giờ mở cửa, etc. */}
                                        <div className="mt-4 flex items-center space-x-4">
                                            <div className="flex items-center text-yellow-500">
                                                <svg
                                                    className="w-4 h-4 mr-1"
                                                    fill="currentColor"
                                                    viewBox="0 0 20 20"
                                                >
                                                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                                                </svg>
                                                <span className="text-sm text-gray-600">
                                                    Điểm tham quan nổi tiếng
                                                </span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        ))}

                    {/* Nếu không có địa điểm nổi bật */}
                    {(!city.popularPlaces || city.popularPlaces.length === 0) && (
                        <div className="text-center py-12 bg-gray-50 rounded-xl">
                            <svg
                                className="w-16 h-16 text-gray-400 mx-auto mb-4"
                                fill="none"
                                stroke="currentColor"
                                viewBox="0 0 24 24"
                            >
                                <path
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                    strokeWidth={2}
                                    d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"
                                />
                                <path
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                    strokeWidth={2}
                                    d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"
                                />
                            </svg>
                            <p className="text-gray-500 text-lg">
                                Chưa có thông tin về địa điểm nổi bật
                            </p>
                        </div>
                    )}
                </div>
            </div>

            {/* Tour trong thành phố */}
            <div className="mb-8">
                <h2 className="text-2xl font-semibold mb-6">Các tour nổi bật</h2>

                <TourInCity></TourInCity>
            </div>

            {/* Review */}
            <div className="mb-8">
                <p className="text-2xl font-semibold mb-6">
                    Mọi người nghĩ gì về các dịch vụ ở {city.name}
                </p>
                <GeneralCarousel responsive={responsive}>
                    {reviews.map((review, index) => (
                        <div
                            key={index}
                            className="max-h-[250px] p-6 border border-gray-200 rounded-lg flex flex-col justify-between"
                        >
                            <div className="flex items-center gap-3">
                                {review.userAvatar ? (
                                    <img
                                        src={review.userAvatar}
                                        alt="avatar"
                                        className="w-10 h-10 rounded-full object-cover"
                                    />
                                ) : (
                                    <div className="w-10 h-10 rounded-full bg-gray-300 flex items-center justify-center text-sm font-semibold text-gray-700">
                                        {review.userFirstName?.charAt(0)?.toUpperCase() || "?"}
                                    </div>
                                )}

                                <div>
                                    <p className="font-medium text-gray-800">
                                        {review.userFirstName} {review.userLastName}
                                    </p>
                                    <p className="text-xs text-gray-400">
                                        {new Date(review.createdAt).toLocaleDateString("vi-VN")}
                                    </p>
                                </div>
                            </div>

                            <div className="flex items-center gap-1 text-sm mt-2">
                                {Array(5)
                                    .fill(0)
                                    .map((_, i) => (
                                        <FaStar
                                            key={i}
                                            className={
                                                i < review.rating
                                                    ? "text-yellow-400"
                                                    : "text-gray-300"
                                            }
                                        />
                                    ))}
                                <span className="ml-1 text-gray-500">
                                    ({review.rating}/5)
                                </span>
                            </div>

                            <p className="text-gray-400 text-sm mt-1">
                                Đánh giá cho:{" "}
                                <Link
                                    to={`/tour/${review.tourId}`}
                                    className="text-blue-500 hover:underline"
                                >
                                    {review.tourName}
                                </Link>
                            </p>

                            <div className="max-h-[90px] overflow-auto">
                                <p className="mt-2 text-gray-700 text-sm">
                                    {review.comment}
                                </p>
                            </div>
                        </div>
                    ))}
                </GeneralCarousel>
            </div>
            {/* Câu hỏi phổ biến */}
            <div>
                <h2 className="text-2xl font-semibold mb-6">Du lịch {city.name} cần lưu ý gì</h2>

                {city.popularQuestions && city.popularQuestions.length > 0 ? (
                    <Collapse
                        accordion
                        bordered={false}
                        defaultActiveKey={[0]}
                        expandIcon={({ isActive }) => (
                            <MdOutlineKeyboardArrowDown
                                className={`w-5 h-5 ${isActive ? "rotate-180" : "rotate-0"}`}
                            />
                        )}
                        className="rounded-lg"
                        expandIconPosition="end"
                        items={getItems(panelStyle, city.popularQuestions)}
                    ></Collapse>
                ) : (
                    <div className="text-gray-400 text-center py-8">Chưa có câu hỏi nào.</div>
                )}
            </div>

            {/* Modal thư viện ảnh */}
            {showImageGallery && (
                <div className="fixed inset-0 bg-black/90 z-50 flex items-center justify-center">
                    <div className="relative max-w-4xl max-h-full p-4">
                        {/* Nút đóng */}
                        <button
                            className="absolute top-4 right-4 text-white hover:text-gray-300 z-10"
                            onClick={() => setShowImageGallery(false)}
                        >
                            <svg
                                className="w-8 h-8"
                                fill="none"
                                stroke="currentColor"
                                viewBox="0 0 24 24"
                            >
                                <path
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                    strokeWidth={2}
                                    d="M6 18L18 6M6 6l12 12"
                                />
                            </svg>
                        </button>

                        {/* Ảnh chính */}
                        <div className="relative">
                            <img
                                src={`${CLOUDINARY_BASE_URL}/${city.images[currentImageIndex]}`}
                                alt={`${city.name} - ${currentImageIndex + 1}`}
                                className="w-[600px] h-[320px] object-cover mx-auto"
                            />

                            {/* Nút previous */}
                            {city.images.length > 1 && currentImageIndex > 0 && (
                                <button
                                    className="absolute left-4 top-1/2 transform -translate-y-1/2 text-white hover:text-gray-300"
                                    onClick={() => setCurrentImageIndex(currentImageIndex - 1)}
                                >
                                    <svg
                                        className="w-8 h-8"
                                        fill="none"
                                        stroke="currentColor"
                                        viewBox="0 0 24 24"
                                    >
                                        <path
                                            strokeLinecap="round"
                                            strokeLinejoin="round"
                                            strokeWidth={2}
                                            d="M15 19l-7-7 7-7"
                                        />
                                    </svg>
                                </button>
                            )}

                            {/* Nút next */}
                            {city.images.length > 1 && currentImageIndex < city.images.length - 1 && (
                                <button
                                    className="absolute right-4 top-1/2 transform -translate-y-1/2 text-white hover:text-gray-300"
                                    onClick={() => setCurrentImageIndex(currentImageIndex + 1)}
                                >
                                    <svg
                                        className="w-8 h-8"
                                        fill="none"
                                        stroke="currentColor"
                                        viewBox="0 0 24 24"
                                    >
                                        <path
                                            strokeLinecap="round"
                                            strokeLinejoin="round"
                                            strokeWidth={2}
                                            d="M9 5l7 7-7 7"
                                        />
                                    </svg>
                                </button>
                            )}
                        </div>

                        {/* Thumbnails */}
                        {city.images.length > 1 && (
                            <div className="flex justify-center mt-4 space-x-2 overflow-x-auto">
                                {city.images.map((img, idx) => (
                                    <button
                                        key={idx}
                                        className={`flex-shrink-0 w-16 h-16 rounded-lg overflow-hidden border-2 ${idx === currentImageIndex
                                                ? "border-white"
                                                : "border-transparent"
                                            }`}
                                        onClick={() => setCurrentImageIndex(idx)}
                                    >
                                        <img
                                            src={`${CLOUDINARY_BASE_URL}/${img}`}
                                            alt={`Thumbnail ${idx + 1}`}
                                            className="w-full h-full object-cover"
                                        />
                                    </button>
                                ))}
                            </div>
                        )}

                        {/* Chỉ số ảnh */}
                        <div className="text-center text-white mt-4">
                            {currentImageIndex + 1} / {city.images.length}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default CityDetail;
