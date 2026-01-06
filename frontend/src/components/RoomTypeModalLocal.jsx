import React, { useState, useEffect } from "react";
import { Modal, TreeSelect, message } from "antd";
import { useForm, Controller } from "react-hook-form";
import FormInput from "./FormInput";
import FormSelect from "./FormSelect";
import UploadImg from "./UploadImg";
import { ROOM_FACILITIES_OPTIONS, VIEW_OPTIONS } from "../constants/hotel";

const RoomTypeModalLocal = ({
    isIncludedRoom,
    visible,
    onCancel,
    onAddRoomType,
    onUpdateRoomType,
    editingRoomType,
}) => {
    const [uploadKey, setUploadKey] = useState(0);
    const [roomTypeImgs, setRoomTypeImages] = useState([]);
    const [deletedImgs, setDeletedImgs] = useState([]);

    const {
        register,
        handleSubmit,
        control,
        reset,
        formState: { errors },
        setValue,
        getValues,
        watch,
    } = useForm({
        defaultValues: editingRoomType || {
            name: "",
            img: [],
            area: "",
            view: "",
            roomFacilities: [],
            rooms: [],
        },
    });

    const treeData = [
        {
            title: "Chọn tất cả",
            value: "all",
            key: "all",
            children: ROOM_FACILITIES_OPTIONS.map((item) => ({
                title: item.label,
                value: item.value,
                key: item.value,
            })),
        },
    ];

    const handleRoomTypeImagesChange = ({ newImages, deletedExisting }) => {
        if (newImages) setRoomTypeImages(newImages);
        if (deletedExisting && deletedExisting.length > 0) {
            setDeletedImgs((prev) => [...prev, ...deletedExisting]);
            setValue("img", getValues("img").filter(id => !deletedExisting.includes(id)));
        }
    };

    const onSubmit = (data) => {
        const roomTypeData = {
            ...data,
            newImages: roomTypeImgs.map((img) => img.base64), 
            deletedImages: deletedImgs,                      
        };

        if (editingRoomType) {
            onUpdateRoomType(roomTypeData);
        } else {
            onAddRoomType(roomTypeData);
        }

        setRoomTypeImages([]);
        setDeletedImgs([]);
        setUploadKey((prev) => prev + 1);
        reset();
    };

    return (
        <Modal
            open={visible}
            onCancel={onCancel}
            width={"50%"}
            footer={null}
            centered
            styles={{
                content: {
                    padding: 0,
                    overflow: "hidden",
                },
            }}
        >
            <p className="px-5 pb-2 pt-3 text-[18px] font-semibold">
                {editingRoomType ? "Sửa loại phòng" : "Thêm loại phòng"}
            </p>

            <form
                onSubmit={handleSubmit(onSubmit)}
                className="overflow-y-auto max-h-[80vh] px-5 pb-4"
            >
                <FormInput
                    label={"Tên loại phòng"}
                    name={"name"}
                    register={register}
                    errors={errors}
                    placeholder={"Nhập tên loại phòng"}
                    className="mb-3"
                    validationRules={{ required: "Tên là bắt buộc" }}
                />
                <FormInput
                    label={"Diện tích (m²)"}
                    type="number"
                    name={"area"}
                    register={register}
                    errors={errors}
                    placeholder={"Nhập diện tích phòng"}
                    className="mb-3"
                    validationRules={{ required: "Diện tích là bắt buộc" }}
                />
                <FormSelect
                    label={"Hướng phòng"}
                    name={"view"}
                    control={control}
                    placeholder={"Chọn tầm nhìn"}
                    isMultiple={false}
                    options={VIEW_OPTIONS}
                    errors={errors}
                    className="mb-3"
                />
                <div className="flex-1 mb-6">
                    <label className="block font-medium mb-2 mr-4">
                        Cơ sở vật chất <span className="text-red-500">*</span>
                    </label>
                    <Controller
                        name="roomFacilities"
                        control={control}
                        rules={{ required: "Cơ sở vật chất là bắt buộc" }}
                        render={({ field }) => (
                            <TreeSelect
                                {...field}
                                placeholder="Chọn cơ sở vật chất và dịch vụ"
                                className={`w-full ${errors["roomFacilities"] ? "border-red-500" : ""
                                    } border rounded border-gray-300`}
                                size="large"
                                treeCheckable={true}
                                treeData={treeData}
                                showCheckedStrategy={TreeSelect.SHOW_CHILD}
                                treeDefaultExpandAll
                                multiple
                                allowClear
                                onChange={(value) => field.onChange(value)}
                            />
                        )}
                    />
                    {errors["roomFacilities"] && (
                        <p className="text-red-500 text-sm mt-1">
                            {errors["roomFacilities"]?.message}
                        </p>
                    )}
                </div>

                <UploadImg
                    label="Ảnh loại phòng"
                    existingImages={watch("img") || []}
                    newImages={roomTypeImgs}
                    onImagesChange={handleRoomTypeImagesChange}
                    key={uploadKey}
                />

                <div className="flex justify-end gap-4 mt-4">
                    <button
                        type="submit"
                        className="bg-blue-500 text-white p-2 rounded"
                    >
                        {editingRoomType ? "Lưu thay đổi" : "Thêm loại phòng"}
                    </button>
                </div>
            </form>
        </Modal>
    );
};

export default RoomTypeModalLocal;