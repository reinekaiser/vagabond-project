import { Modal } from "antd";
import {useFieldArray, useForm} from "react-hook-form";
import React, { useEffect, useState, useRef } from "react";
import UploadImg from "./UploadImg";
import FormInput from "./FormInput";
import {useUpdateCityMutation} from "../redux/api/cityApiSlice";
import { toast } from "react-toastify";
import {useDeleteImageMutation, useUploadImagesMutation} from "../redux/api/uploadApiSlice.js";
import FormTextArea from "./FormTextArea.jsx";
import {FaRegTrashCan} from "react-icons/fa6";
import {FiPlusCircle} from "react-icons/fi";
import {CLOUDINARY_BASE_URL} from "../constants/hotel.js";

const CityEditModal = ({ open, onCancel, city }) => {

    const initialValues = {
        name: city?.name || '',
        description: city?.description || '',
        bestTimeToVisit: city?.bestTimeToVisit || '',
        images: city?.images || [],
        popularPlaces: city?.popularPlaces || [
            { name: '', description: '', image: ''}
        ],
        popularQuestions: city.popularQuestions || [
            { question: '', answer: ''}
        ]
    }
    const {
        control,
        getValues,
        handleSubmit,
        register,
        setValue,
        formState: { errors },
        watch,
        reset
    } = useForm(
        {
            defaultValues: initialValues
        }
    );

    useEffect(() => {
        if (city) {
            const initialValues = {
                name: city?.name || '',
                description: city?.description || '',
                bestTimeToVisit: city?.bestTimeToVisit || '',
                images: city?.images || [],
                popularPlaces: city?.popularPlaces || [
                    { name: '', description: '', image: '' }
                ],
            };
            reset(initialValues); // 👈 cập nhật lại form khi city đổi
        }
    }, [city, reset]);
    const [imagePreviews, setImagePreviews] = useState({});
    const [imageFiles, setImageFiles] = useState({});
    const [isSubmitting, setIsSubmitting] = useState(false);

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
        if (deletedExisting !== null) {
            console.log(deletedExisting)
            setValue(
                "images",
                getValues("images").filter((_, i) => i !== deletedExisting)
            )

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

    const watchedPlaces = watch('popularPlaces');

    const handlePlaceImageChange = (index, e) => {
        const file = e.target.files?.[0];
        if (!file) return;

        // Đọc file để preview
        const reader = new FileReader();
        reader.onloadend = () => {
            const base64String = reader.result;

            // Lưu preview và file base64
            setImagePreviews(prev => ({ ...prev, [index]: base64String }));
            setImageFiles(prev => ({ ...prev, [index]: base64String }));
        };
        reader.readAsDataURL(file);
    };

    const {
        fields: questionFields,
        append: questionAppend,
        remove: questionRemove,
    } = useFieldArray({
        control,
        name: "popularQuestions",
    });

    const [modalKey, setModalKey] = useState(0)

    const [updateCity] = useUpdateCityMutation()
    const [deleteImage] = useDeleteImageMutation()

    const onSubmit = async (data) => {
        try {
            setIsSubmitting(true)
            const imagesToUpload = [];
            const imageIndexMap = {};

            Object.keys(imagePreviews).forEach(index => {
                imageIndexMap[imagesToUpload.length] = parseInt(index);
                imagesToUpload.push(imagePreviews[index]);
            });

            let uploadedImageUrls = [];

            if (imagesToUpload.length > 0) {
                try {
                    uploadedImageUrls = await uploadPlaceImages({data: imagesToUpload}).unwrap();

                } catch (error) {
                    console.error('Error uploading images:', error);
                    setIsSubmitting(false);
                    return;
                }
            }

            data.popularPlaces = data.popularPlaces.map((place, index) => {
                const uploadIndex = Object.keys(imageIndexMap).find(
                    key => imageIndexMap[key] === index
                );

                if (uploadIndex !== undefined) {
                    return { ...place, image: uploadedImageUrls[uploadIndex] };
                }

                return place;
            });


            let uploadedImgs = [];
            if (imagesBase64.length > 0) {
                try {
                    uploadedImgs = await uploadImagesToCloudinary(imagesBase64);
                    console.log(uploadedImgs);
                } catch (error) {
                    console.error("Upload ảnh thất bại!", error);
                    if (uploadedImageUrls.length > 0) {
                        try {
                            await Promise.all(
                                uploadedImageUrls.map(async (imgUrl) => {
                                    try {
                                        await deleteImage(imgUrl).unwrap();
                                        console.log("Đã rollback ảnh con:", imgUrl);
                                    } catch (err) {
                                        console.warn("Không thể xóa ảnh rollback:", imgUrl, err);
                                    }
                                })
                            );
                        } catch (rollbackError) {
                            console.error("Lỗi khi rollback ảnh con:", rollbackError);
                        }
                    }

                    setIsSubmitting(false);
                    return;
                }

                const existingImages = getValues("images") || []; // Bảo vệ nếu không có giá trị images
                data.images = [...existingImages, ...uploadedImgs];
            }

            try {
                const result = await updateCity({cityId: city._id, data}).unwrap();
                toast.update(uploadToastId.current, {
                    render: "Cập nhật thành phố thành công",
                    type: "success",
                    isLoading: false,
                    closeButton: true,
                    autoClose:3000
                });
            } catch (error) {
                toast.error("Tạo thành phố thất bại. Vui lòng thử lại!");
                console.error(error);
            }

            setUploadImgKey((prev) => prev + 1);
            setModalKey((prev) => prev + 1)
            setImagesBase64([]);
            setImages([]);
            setIsSubmitting(false)
            onCancel();
        } catch (e) {
            console.error(e)
        }
    }

    const uploadToastId = useRef(null);

    useEffect(() => {
        if (isUploadPlaceImageLoading) {
            uploadToastId.current = toast.loading("Đang tải ảnh địa điểm...");
        } else if (isUploadPlaceImageError) {
            toast.update(uploadToastId.current, {
                render: "Tải ảnh địa điểm thất bại!",
                type: "error",
                isLoading: false,
                closeButton: true,
                autoClose:3000
            });
        } else if (isUploadLoading) {
            toast.update(uploadToastId.current, {
                render: "Đang tải ảnh thành phố...",
                isLoading: true,
            });
        }  else if (isUploadError) {
            toast.update(uploadToastId.current, {
                render: "Tải ảnh thành phố thất bại!",
                type: "error",
                isLoading: false,
                closeButton: true,
                autoClose: 3000
            });
        } else {
            uploadToastId.current = null;
        }

    }, [isUploadLoading, isUploadError, isUploadPlaceImageLoading, isUploadPlaceImageError]);
    return (
        <Modal
            key={modalKey}
            title={<p className="px-5 pb-2 pt-4 text-[18px]">Cập nhật tour</p>}
            open={open}
            onCancel={() => {
                onCancel();
                reset(initialValues);
                setModalKey((prev) => prev + 1)
            }}
            onOk={handleSubmit(onSubmit)}
            okText="Cập nhật"
            cancelText="Hủy"
            width={"60%"}
            centered
            styles={{
                content: {
                    padding: 0,
                },
                body: {
                    fontSize: "16px",
                },
                footer: {
                    padding: "16px"
                }
            }}
            destroyOnHidden={true}
            okButtonProps={{
                disabled: isSubmitting,
            }}
        >
            <div className="h-[450px] overflow-auto px-5 pb-2">
                <form className="space-y-4">
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

                        {placeFields.map((place, index) => {
                            let currentImage = ""
                            if (imagePreviews[index]) {
                                currentImage = imagePreviews[index]
                            } else if (watchedPlaces[index]?.image) {
                                currentImage = `${CLOUDINARY_BASE_URL}/${watchedPlaces[index]?.image}`
                            }

                            return (
                                <div key={index} className="border rounded-lg p-4 mb-4">
                                    <div className="flex justify-between items-center mb-3">
                                        <h3 className="font-medium">Địa điểm #{index + 1}</h3>
                                        <button
                                            type="button"
                                            onClick={() => {
                                                placeRemove(index);
                                                setImagePreviews(prev => {
                                                    const newPreviews = { ...prev };
                                                    delete newPreviews[index];
                                                    return newPreviews;
                                                });
                                                setImageFiles(prev => {
                                                    const newFiles = { ...prev };
                                                    delete newFiles[index];
                                                    return newFiles;
                                                });
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
                                                    {currentImage ? (
                                                        <div className="relative w-full h-full flex items-center justify-center">
                                                            <img
                                                                src={currentImage}
                                                                alt={place.name}
                                                                className="w-full h-full object-cover"
                                                            />
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
                                                    {currentImage
                                                        ? "Thay đổi ảnh"
                                                        : "Thêm ảnh"}
                                                    <input
                                                        type="file"
                                                        hidden
                                                        accept="image/*"
                                                        onChange={(e) =>
                                                            handlePlaceImageChange(
                                                                index,
                                                                e
                                                            )
                                                        }
                                                    />
                                                </label>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            )
                        } )}
                        <button
                            type="button"
                            onClick={() => {
                                placeAppend({ name: "", description: "", image: "" });
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


                </form>
            </div>
        </Modal>
    )
}
export default CityEditModal