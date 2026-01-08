import React, { useRef, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import {
    useDeleteUserAvatarMutation,
    useUpdateUserAvatarMutation,
} from "../redux/api/authApiSlice";
import { setCredentials } from "../redux/features/authSlice";
import { Modal } from "antd";
import { CameraIcon } from "@heroicons/react/24/outline";

const UpdateAvatar = () => {
    const fileInputRef = useRef(null);
    const dispatch = useDispatch();
    const user = useSelector((state) => state.auth.user);
    const [isDeleteModalVisible, setDeleteModalVisible] = useState(false);

    const [updateAvatar, { isLoading: uploading }] =
        useUpdateUserAvatarMutation();
    const [deleteAvatar, { isLoading: deleting }] =
        useDeleteUserAvatarMutation();

    if (!user) return null;

    const getInitial = () => {
        if (user.firstName) return user.firstName.charAt(0).toUpperCase();
        if (user.lastName) return user.lastName.charAt(0).toUpperCase();
        return "?";
    };

    const toBase64 = (file) =>
        new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.readAsDataURL(file);
            reader.onload = () => resolve(reader.result);
            reader.onerror = reject;
        });

    const handleFileChange = async (e) => {
        const file = e.target.files?.[0];
        if (!file) return;

        if (!file.type.startsWith("image/")) {
            alert("Please select an image file");
            return;
        }

        try {
            const base64 = await toBase64(file);
            const res = await updateAvatar({
                userId: user._id,
                avatar: base64,
            }).unwrap();
            dispatch(setCredentials(res));
        } catch (err) {
            console.error("Update avatar failed:", err);
        }
    };

    const handleDeleteAvatar = async () => {
        try {
            const res = await deleteAvatar().unwrap();
            dispatch(setCredentials(res));
            setDeleteModalVisible(false);
        } catch (err) {
            console.error("Delete avatar failed:", err);
        }
    };

    return (
        <div className="flex items-center gap-6">
            <div className="relative group">
                <div
                    className="w-24 h-24 rounded-full border-4 border-white/30 shadow-lg
                        overflow-hidden flex items-center justify-center
                        text-blue-500 text-4xl font-semibold
                        transition-transform group-hover:scale-105"
                >
                    {user.avatarUrl ? (
                        <img
                            src={user.avatarUrl}
                            alt={user.lastName}
                            className="w-full h-full object-cover"
                        />
                    ) : (
                        <span>{getInitial()}</span>
                    )}
                </div>

                <label
                    className="absolute bottom-0 right-0 bg-white/90 hover:bg-white
                        text-gray-700 p-2 rounded-full cursor-pointer shadow-lg
                        transition-all duration-200 hover:scale-110"
                >
                    <input
                        ref={fileInputRef}
                        type="file"
                        accept="image/*"
                        onChange={handleFileChange}
                        className="hidden"
                        disabled={uploading}
                    />

                    {uploading ? (
                        <div className="w-5 h-5 border-2 border-gray-400 border-t-transparent rounded-full animate-spin" />
                    ) : (
                        <CameraIcon className="w-5 h-5" />
                    )}
                </label>
            </div>

            {user.avatarUrl && (
                <button
                    onClick={() => setDeleteModalVisible(true)}
                    disabled={deleting}
                    className="px-4 py-2 rounded-md bg-red-500 text-white text-sm
                        hover:bg-red-600 disabled:opacity-50"
                >
                    Xoá ảnh
                </button>
            )}

            <Modal
                title="Xác nhận xoá ảnh"
                open={isDeleteModalVisible}
                onOk={handleDeleteAvatar}
                onCancel={() => setDeleteModalVisible(false)}
                okText="Xoá"
                cancelText="Huỷ"
                okButtonProps={{ danger: true }}
            >
                <p>Bạn có chắc chắn muốn xoá ảnh đại diện này?</p>
            </Modal>
        </div>
    );
};

export default UpdateAvatar;