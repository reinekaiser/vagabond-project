import React, { useState, useEffect } from 'react'
import { Modal, message, Rate } from "antd";
import UploadImg from "./UploadImg";
import { useForm, Controller } from "react-hook-form";
import { useUploadImagesMutation, useDeleteImageMutation } from '../redux/api/uploadApiSlice'
import FormTextArea from './FormTextArea';

const ReviewModal = ({ visible, onCancel, onAddReview, onUpdateReview, editingReview, isLoading }) => {
    const [messageApi, contextMessageHolder] = message.useMessage();
    const [uploadKey, setUploadKey] = useState(0);
    const [images, setImages] = useState([]);
    const [imgesBase64, setImagesBase64] = useState([]);
    const [deletedImgs, setDeletedImgs] = useState([]);
    const [uploadImages, { isLoading: isUploadLoading, isError: isUploadError, isSuccess }] = useUploadImagesMutation();

    const { register, handleSubmit, control, formState: { errors }, setValue, getValues, watch } = useForm({
        defaultValues: editingReview || {
            rating: 4.5,
            comment: "",
            images: [],
        }
    });

    const handleReviewImagesChange = async ({ deletedExisting, newImages }) => {
        if (deletedExisting != undefined) {
            const currentImgs = getValues("images");
            const deletedId = currentImgs[deletedExisting];
            const updatedImgs = currentImgs.filter((_, i) => i !== deletedExisting);
            setValue("images", updatedImgs);

            setDeletedImgs(prev => [...prev, deletedId]);
        }
        if (newImages) {
            setImages(newImages);
            setImagesBase64(newImages.map((img) => img.base64));
        }
    }
    const uploadImagesToCloudinary = async (imagesBase64) => {
        if (imagesBase64.length === 0) return [];
        try {
            const res = await uploadImages({ data: imagesBase64 }).unwrap();
            return res;
        } catch (error) {
            console.log(error)
        }
    }

    const onSubmit = async (data) => {
        if (editingReview) {
            if (imgesBase64.length > 0) {
                const uploadedImages = await uploadImagesToCloudinary(imgesBase64);
                const allImgs = [...getValues("images"), ...uploadedImages];
                setValue("images", allImgs);
            }
            onUpdateReview(getValues(), deletedImgs);
            setImages([]);
            setImagesBase64([]);
            setDeletedImgs([]);
            setUploadKey(prevKey => prevKey + 1);
        }
        else {
            if (imgesBase64.length > 0) {
                const uploadedImages = await uploadImagesToCloudinary(imgesBase64);
                const allImgs = [...getValues("images"), ...uploadedImages];
                setValue("images", allImgs);
            }
            onAddReview(getValues());
            setImages([]);
            setImagesBase64([]);
            setUploadKey(prevKey => prevKey + 1);
        }
    }

    useEffect(() => {
        if (isUploadLoading) {
            messageApi.open({
                key: 'uploading',
                type: 'loading',
                content: 'Đang tải ảnh lên...',
                duration: 0,
            });
        }
        if (isUploadError) {
            messageApi.open({
                key: 'uploading',
                type: 'error',
                content: 'Tải ảnh thất bại!',
                duration: 2,
            });
        }
        if (isSuccess) {
            messageApi.destroy('uploading');
        }
    }, [isUploadLoading, isUploadError, isSuccess]);



    return (
        <Modal
            open={visible}
            onCancel={onCancel}
            width={"50%"}
            footer={null}
            centered
        >
            {contextMessageHolder}
            <p className="text-[18px] font-semibold mb-3">Thêm đánh giá</p>

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                <div className='text-[16px]'>
                    <label className="block font-semibold mb-2 mr-4">
                        Đánh giá
                    </label>
                    <Controller
                        name="rating"
                        control={control}
                        render={({ field }) => (
                            <Rate
                                {...field}
                                allowHalf
                                value={field.value || 4.5}
                                onChange={(value) => {
                                    field.onChange(value);
                                }}
                            />
                        )}
                    />
                </div>
                <FormTextArea
                    label="Nội dung đánh giá"
                    name="comment"
                    register={register}
                    errors={errors}
                    placeholder="Nhập nội dung đánh giá của bạn"
                />
                <UploadImg
                    label="Thêm ảnh"
                    existingImages={watch("images") || []}
                    newImages={images}
                    onImagesChange={handleReviewImagesChange}
                    key={uploadKey}
                />

                <div className="flex justify-end">
                    <button
                        type="submit"
                        disabled={isLoading || isUploadLoading}
                        className="flex items-center gap-2 bg-blue-500 text-white px-4 py-2 rounded
                            disabled:opacity-70 disabled:cursor-not-allowed"
                    >
                        {(isLoading || isUploadLoading) && (
                            <span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></span>
                        )}
                        <span>
                            {editingReview ? "Sửa đánh giá" : "Thêm đánh giá"}
                        </span>
                    </button>
                </div>
            </form>
        </Modal>
    )
}

export default ReviewModal