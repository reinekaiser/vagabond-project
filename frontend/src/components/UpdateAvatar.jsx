import React, { useRef, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useDeleteUserAvatarMutation, useUpdateUserAvatarMutation } from "../redux/api/authApiSlice";
import { setCredentials } from "../redux/features/authSlice";
import { Modal } from "antd";

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
            dispatch(setCredentials(res))
        } catch (err) {
            console.error("Update avatar failed:", err);
        }
    };

    const showDeleteReviewModal = () => {
        setDeleteModalVisible(true);
    };
    const handleDeleteAvatar = async () => {
        if (!user.avatarUrl) return;

        try {
            const res = await deleteAvatar().unwrap();
            dispatch(setCredentials(res))
            setDeleteModalVisible(false)
        } catch (err) {
            console.error("Delete avatar failed:", err);
        }
    };

    return (
        <div className="flex items-center gap-6">
            {/* Avatar */}
            <div className="relative w-20 h-20 rounded-full bg-gray-200 overflow-hidden flex items-center justify-center text-4xl font-semibold text-gray-700">
                {user.avatarUrl ? (
                    <img
                        src={user.avatarUrl}
                        alt="Avatar"
                        className="w-full h-full object-cover"
                    />
                ) : (
                    <span>{getInitial()}</span>
                )}
            </div>

            {/* Actions */}
            <div className="flex gap-3">
                <button
                    onClick={() => fileInputRef.current.click()}
                    disabled={uploading}
                    className="flex items-center gap-2 px-3 py-1 rounded-md bg-blue-600 text-white text-sm
                            hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                    {uploading && (
                        <span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                    )}

                    <span>{uploading ? "Uploading..." : "Update avatar"}</span>
                </button>

                {user.avatarUrl && (
                    <button
                        onClick={showDeleteReviewModal}
                        disabled={deleting}
                        className="px-3 py-1 rounded-md bg-red-500 text-white text-sm hover:bg-red-600"
                    >
                        {"Delete"}
                    </button>
                )}
            </div>

            {/* Hidden input */}
            <input
                ref={fileInputRef}
                type="file"
                accept="image/*"
                hidden
                onChange={handleFileChange}
            />

            <Modal
                title="Xác nhận xoá đánh giá"
                open={isDeleteModalVisible}
                onOk={handleDeleteAvatar}
                onCancel={() => setDeleteModalVisible(false)}
                okText="Xoá"
                cancelText="Hủy"
                okButtonProps={{ danger: true }}
            >
                <p>Bạn có chắc chắn muốn xoá ảnh này?</p>
            </Modal>
        </div>
    );
};

export default UpdateAvatar;