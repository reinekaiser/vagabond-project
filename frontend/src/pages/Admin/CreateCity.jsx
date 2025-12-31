import React, {useEffect, useRef, useState} from "react";
import {useFieldArray, useForm} from "react-hook-form";
import {useNavigate} from "react-router-dom";
import {useCreateCityMutation} from "../../redux/api/cityApiSlice";
import FormInput from "../../components/FormInput";
import FormTextArea from "../../components/FormTextArea";
import UploadImg from "../../components/UploadImg";
import {useDeleteImageMutation, useUploadImagesMutation} from "../../redux/api/uploadApiSlice";
import {FiPlusCircle} from "react-icons/fi";
import {FaRegTrashCan} from "react-icons/fa6";
import {IoIosClose} from "react-icons/io";
import {toast} from "react-toastify";
import { Modal, Spin } from "antd";
import { LoadingOutlined } from '@ant-design/icons';

const CreateCity = () => {
    const navigate = useNavigate();

    const {
        register,
        handleSubmit,
        formState: { errors },
        getValues,
        control,
        watch,
        setValue,
    } = useForm();

    const [deleteImage] = useDeleteImageMutation();

    const deleteImagesFromCloudinary = async (publicId) => {
        if (!publicId) return;
        try {
            await deleteImage(publicId).unwrap();
            setValue(
                "images",
                getValues("images").filter((id) => id !== publicId)
            );
        } catch (error) {
            console.log(error);
        }
    };

    const [uploadImgKey, setUploadImgKey] = useState(0);
    const [images, setImages] = useState([]);
    const [imagesBase64, setImagesBase64] = useState([]);

    const [
        uploadImages,
        { isLoading: isUploadLoading, isError: isUploadError, isSuccess: isUploadSuccess },
    ] = useUploadImagesMutation();

    const [
        uploadPlaceImages,
        { isLoading: isUploadPlaceImageLoading, isError: isUploadPlaceImageError, isSuccess: isUploadPlaceImageSuccess },
    ] = useUploadImagesMutation();

    const [isUploadingImages, setIsUploadingImages] = useState(false)

    const uploadImagesToCloudinary = async (imagesBase64) => {
        if (imagesBase64.length === 0) return [];
        try {
            const res = await uploadImages({
                data: imagesBase64,
            }).unwrap();
            return res;
        } catch (error) {
            console.log(error);
        }
    };

    const handleImagesChange = async ({ newImages, deletedExisting }) => {
        if (deletedExisting) {
            await deleteImagesFromCloudinary(deletedExisting);
        }
        if (newImages) {
            console.log("newImages", newImages);
            setImages(newImages);
            setImagesBase64(newImages.map((img) => img.base64));
        }
    };


    const {
        fields: placeFields,
        append: placeAppend,
        remove: placeRemove,
    } = useFieldArray({
        control,
        name: "popularPlaces",
    });

    const [placeImages, setPlaceImages] = useState([]);
    const handlePlaceImageChange = (index, file) => {
        setPlaceImages((prev) => {
            const updated = [...prev];
            updated[index] = file;
            return updated;
        });
    };

    // 🗑️ Xóa ảnh
    const handlePlaceImageRemove = (index) => {
        setPlaceImages((prev) => {
            const updated = [...prev];
            updated[index] = null;
            return updated;
        });
    };

    const {
        fields: questionFields,
        append: questionAppend,
        remove: questionRemove,
    } = useFieldArray({
        control,
        name: "popularQuestions",
    });

    const uploadToastId = useRef(null);

    // useEffect(() => {
    //     if (isUploadLoading) {
    //         uploadToastId.current = toast.loading("Đang tải ảnh thành phố...");
    //     } else if (isUploadSuccess) {
    //         toast.update(uploadToastId.current, {
    //             render: "Tải ảnh thành phố thành công!",
    //             type: "success",
    //             isLoading: false,
    //             closeButton: true,
    //             autoClose:3000
    //         });
    //     } else if (isUploadError) {
    //         toast.update(uploadToastId.current, {
    //             render: "Tải ảnh thành phố thất bại!",
    //             type: "error",
    //             isLoading: false,
    //             closeButton: true,
    //             autoClose:3000
    //         });
    //     }
    // }, [isUploadLoading, isUploadSuccess, isUploadError]);
    //
    // const uploadPlaceImageToastId = useRef(null);
    //
    // useEffect(() => {
    //     if (isUploadPlaceImageLoading) {
    //         uploadPlaceImageToastId.current = toast.loading("Đang tải ảnh địa điểm...");
    //     } else if (isUploadPlaceImageSuccess) {
    //         toast.update(uploadPlaceImageToastId.current, {
    //             render: "Tải ảnh địa điểm thành công!",
    //             type: "success",
    //             isLoading: false,
    //             closeButton: true,
    //             autoClose:3000
    //         });
    //     } else if (isUploadPlaceImageError) {
    //         toast.update(uploadPlaceImageToastId.current, {
    //             render: "Tải ảnh địa điểm thất bại!",
    //             type: "error",
    //             isLoading: false,
    //             closeButton: true,
    //             autoClose:3000
    //         });
    //     }
    // }, [isUploadPlaceImageLoading, isUploadPlaceImageSuccess, isUploadPlaceImageError]);

    const [createCity, {isloading: isCreatingCity }] = useCreateCityMutation()

    const onSubmit = async (data) => {
        try {
            setIsUploadingImages(true)
            const base64List = await Promise.all(
                placeImages.map((file) => {
                    if (!file) return null;
                    return new Promise((resolve, reject) => {
                        const reader = new FileReader();
                        reader.onloadend = () => resolve(reader.result);
                        reader.onerror = reject;
                        reader.readAsDataURL(file);
                    });
                })
            );

            const validBase64List = base64List.filter((b64) => b64 !== null);

            const uploadedUrls = await uploadPlaceImages({ data: validBase64List }).unwrap();

            data.popularPlaces = data.popularPlaces.map((p, i) => ({
                ...p,
                image: uploadedUrls[i] || null,
            }));

            let uploadedImgs = [];
            if (imagesBase64.length > 0) {
                try {
                    uploadedImgs = await uploadImagesToCloudinary(imagesBase64);
                    console.log(uploadedImgs);
                } catch (error) {
                    console.error("Upload ảnh thất bại!");
                    return;
                }

                const existingImages = getValues("images") || []; // Bảo vệ nếu không có giá trị images
                data.images = [...existingImages, ...uploadedImgs];
            }

            setIsUploadingImages(false)
            try {
                const result = await createCity(data).unwrap();
                toast.success("Tạo thành phố thành công");
                navigate("/admin/manage-cities");
            } catch (error) {
                toast.error("Tạo thành phố thất bại. Vui lòng thử lại!");
                console.error(error);
            }

            setUploadImgKey((prev) => prev + 1);
            setImagesBase64([]);
            setImages([]);

        } catch (err) {
            console.error(err);
            alert("Lỗi khi lưu dữ liệu!");
        }

    };

    return (
        <div className="bg-softBlue min-h-screen">
            <div className="w-[80%] mx-auto py-6">
                <p className="font-semibold text-[20px] md:text-[24px]">Thêm thành phố</p>
                <div className="bg-white rounded-lg shadow-md mt-4 p-4 md:p-6">
                    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                        <FormInput
                            label={"Tên thành phố"}
                            register={register}
                            errors={errors}
                            name={"name"}
                            validationRules={{
                                required: "Tên là bắt buộc",
                            }}
                            placeholder={"Nhập tên thành phố"}
                        ></FormInput>

                        <FormTextArea
                            row={6}
                            label={"Mô tả thành phố"}
                            register={register}
                            errors={errors}
                            name={"description"}
                            validationRules={{
                                required: "Mô tả là bắt buộc",
                            }}
                            placeholder={"Nhập mô tả về thành phố"}
                        ></FormTextArea>

                        <FormTextArea
                            label={"Thời điểm tốt nhất để đến thăm"}
                            register={register}
                            errors={errors}
                            name={"bestTimeToVisit"}
                            placeholder={"Nhập mô tả về thành phố"}
                        ></FormTextArea>

                        <UploadImg
                            label={"Ảnh thành phố"}
                            existingImages={watch("images") || []}
                            newImages={images}
                            onImagesChange={handleImagesChange}
                            key={uploadImgKey}
                        ></UploadImg>

                        <div className="mb-6">
                            <div className="flex justify-between items-center mb-4">
                                <label className="block font-medium">Địa điểm nổi bật</label>
                            </div>

                            {placeFields.map((place, index) => (
                                <div key={index} className="border rounded-lg p-4 mb-4">
                                    <div className="flex justify-between items-center mb-3">
                                        <h3 className="font-medium">Địa điểm #{index + 1}</h3>
                                        <button
                                            type="button"
                                            onClick={() => {
                                                placeRemove(index);
                                                setPlaceImages((prev) =>
                                                    prev.filter((_, i) => i !== index)
                                                );
                                            }}
                                            className="inline-flex items-center gap-2 text-sm text-red-500 hover:bg-red-100 rounded-full"
                                        >
                                            <FaRegTrashCan className="w-[18px] h-[18px]"></FaRegTrashCan>
                                        </button>
                                    </div>

                                    <div className="flex gap-3">
                                        <div className="w-[65%] flex flex-col gap-2">
                                            <div>
                                                <label className="block text-sm font-medium mb-1">
                                                    Tên địa điểm{" "}
                                                    <span className="text-red-500">*</span>
                                                </label>
                                                <input
                                                    type="text"
                                                    placeholder="Nhập tên địa điểm"
                                                    {...register(`popularPlaces.${index}.name`, {
                                                        required: "Tên điểm đến là bắt buộc",
                                                    })}
                                                    className="w-full border border-gray-300 p-2 rounded"
                                                />
                                                {errors.popularPlaces?.[index]?.name && (
                                                    <p className="text-red-500 text-sm mt-1">
                                                        {errors.popularPlaces[index].name.message}
                                                    </p>
                                                )}
                                            </div>

                                            <div className="flex-1 flex flex-col">
                                                <label className="block text-sm font-medium mb-1">
                                                    Mô tả <span className="text-red-500">*</span>
                                                </label>
                                                <textarea
                                                    rows={3}
                                                    placeholder="Nhập mô tả địa điểm"
                                                    {...register(
                                                        `popularPlaces.${index}.description`,
                                                        {
                                                            required: "Tên điểm đến là bắt buộc",
                                                        }
                                                    )}
                                                    className="w-full resize-none flex-1 border border-gray-300 p-2 rounded"
                                                />
                                                {errors.popularPlaces?.[index]?.description && (
                                                    <p className="text-red-500 text-sm mt-1">
                                                        {
                                                            errors.popularPlaces[index].description
                                                                .message
                                                        }
                                                    </p>
                                                )}
                                            </div>
                                        </div>
                                        <div className="flex-1">
                                            <label className="block text-sm font-medium mb-1">
                                                Hình ảnh
                                                <span className="text-red-500">*</span>
                                            </label>
                                            <div className="">
                                                <div className="border w-full h-[200px] border-gray-300 rounded flex items-center justify-center bg-gray-50">
                                                    {placeImages[index] ? (
                                                        <div className="relative w-full h-full flex items-center justify-center">
                                                            <img
                                                                src={URL.createObjectURL(
                                                                    placeImages[index]
                                                                )}
                                                                alt={place.name}
                                                                className="w-full h-full object-cover"
                                                            />
                                                            <button
                                                                onClick={() =>
                                                                    handlePlaceImageRemove(index)
                                                                }
                                                                type="button"
                                                                className="absolute top-2 right-2 bg-red-500 text-white rounded-full w-8 h-8 flex items-center justify-center hover:bg-red-600"
                                                            >
                                                                <IoIosClose></IoIosClose>
                                                            </button>
                                                        </div>
                                                    ) : (
                                                        <div className="r">
                                                            <svg
                                                                className="w-32 h-32 mx-auto text-gray-400 mb-4"
                                                                fill="none"
                                                                stroke="currentColor"
                                                                viewBox="0 0 24 24"
                                                            >
                                                                <path
                                                                    strokeLinecap="round"
                                                                    strokeLinejoin="round"
                                                                    strokeWidth="1"
                                                                    d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
                                                                />
                                                            </svg>
                                                        </div>
                                                    )}
                                                </div>
                                                <label className="mt-2 w-full text-center cursor-pointer py-1 inline-block rounded bg-primary text-white">
                                                    {placeImages[index]
                                                        ? "Thay đổi ảnh"
                                                        : "Thêm ảnh"}
                                                    <input
                                                        type="file"
                                                        hidden
                                                        accept="image/*"
                                                        onChange={(e) =>
                                                            handlePlaceImageChange(
                                                                index,
                                                                e.target.files?.[0] || null
                                                            )
                                                        }
                                                    />
                                                </label>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            ))}
                            <button
                                type="button"
                                onClick={() => {
                                    placeAppend({ name: "", description: "" });
                                    setPlaceImages((prev) => [...prev, null]);
                                }}
                                className="inline-flex items-center gap-3 px-3 py-1 border font-medium rounded-md text-blue-500  hover:bg-blue-100 border-blue-500"
                            >
                                <FiPlusCircle></FiPlusCircle>
                                Thêm địa điểm
                            </button>
                        </div>

                        <div className="mb-6">
                            <div className="flex justify-between items-center mb-4">
                                <label className="block font-medium">
                                    Câu hỏi phổ biến về thành phố
                                </label>
                            </div>
                            {questionFields.map((question, index) => (
                                <div key={index} className="border rounded-lg p-4 mb-4">
                                    <div className="flex justify-between items-center mb-3">
                                        <h3 className="font-medium">Câu hỏi #{index + 1}</h3>
                                        <button
                                            type="button"
                                            onClick={() => {
                                                questionRemove(index);
                                            }}
                                            className="inline-flex items-center gap-2 text-sm text-red-500 hover:bg-red-100 rounded-full"
                                        >
                                            <FaRegTrashCan className="w-[18px] h-[18px]"></FaRegTrashCan>
                                        </button>
                                    </div>
                                    <div className="space-y-2">
                                        <div>
                                            <label className="block text-sm font-medium mb-1">
                                                Câu hỏi <span className="text-red-500">*</span>
                                            </label>
                                            <input
                                                type="text"
                                                placeholder="Nhập câu hỏi"
                                                {...register(`popularQuestions.${index}.question`, {
                                                    required: "Câu hỏi là bắt buộc",
                                                })}
                                                className="w-full border border-gray-300 p-2 rounded"
                                            />
                                            {errors.popularQuestions?.[index]?.question && (
                                                <p className="text-red-500 text-sm mt-1">
                                                    {errors.popularQuestions[index].question.message}
                                                </p>
                                            )}
                                        </div>

                                        <div className="flex-1 flex flex-col">
                                            <label className="block text-sm font-medium mb-1">
                                                Câu trả lời <span className="text-red-500">*</span>
                                            </label>
                                            <textarea
                                                rows={2}
                                                placeholder="Nhập câu trả lời"
                                                {...register(`popularQuestions.${index}.answer`, {
                                                    required: "Câu trả lời là bắt buộc",
                                                })}
                                                className="w-full resize-none flex-1 border border-gray-300 p-2 rounded"
                                            />
                                            {errors.popularQuestions?.[index]?.answer && (
                                                <p className="text-red-500 text-sm mt-1">
                                                    {
                                                        errors.popularPlaces[index].answer
                                                            .message
                                                    }
                                                </p>
                                            )}
                                        </div>
                                    </div>
                                </div>
                            ))}
                            <button
                                type="button"
                                onClick={() => {
                                    questionAppend({ question: "", answer: "" });
                                }}
                                className="inline-flex items-center gap-3 px-3 py-1 border font-medium rounded-md text-blue-500  hover:bg-blue-100 border-blue-500"
                            >
                                <FiPlusCircle></FiPlusCircle>
                                Thêm câu hỏi
                            </button>
                        </div>

                        <button
                            type="submit"
                            disabled={isCreatingCity}
                            className={`bg-blue-500 text-white p-2 rounded hover:bg-blue-600 transition-colors ${
                                isCreatingCity ? "opacity-50 cursor-not-allowed" : ""
                            }`}
                        >
                            {isCreatingCity ? "Đang xử lý..." : "Thêm thành phố"}
                        </button>
                    </form>
                </div>
            </div>
            <Modal
                open={isUploadingImages}
                footer={null}
                closable={false}
                centered
                width={300}
                style={{ textAlign: 'center' }}
            >
                <Spin
                    indicator={<LoadingOutlined style={{ fontSize: 60 }} spin />}
                    size="large"
                />
                <div style={{ marginTop: 16 }}>
                    <p>Đang upload ảnh...</p>
                </div>
            </Modal>
        </div>
    );
};

export default CreateCity;
