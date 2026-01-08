import { AUTH_URL, USER_URL } from "../constants";
import { apiSlice } from "./apiSlice";

export const authApiSlice = apiSlice.injectEndpoints({
    endpoints: (builder) => ({
        sendOtp: builder.mutation({
            query: (data) => ({
                url: `${AUTH_URL}/send-otp`,
                method: "POST",
                body: data,
            }),
        }),
        register: builder.mutation({
            query: (data) => ({
                url: `${AUTH_URL}/register`,
                method: "POST",
                body: data,
            }),
        }),
        login: builder.mutation({
            query: (data) => ({
                url: `${AUTH_URL}/login`,
                method: "POST",
                body: data,
            }),
        }),
        logout: builder.mutation({
            query: () => ({
                url: `${AUTH_URL}/logout`,
                method: "POST",
            }),
        }),
        getUser: builder.query({
            query: () => ({
                url: `${AUTH_URL}/`
            }),
            providesTags: ["User"],
        }),
        getAllUsers: builder.query({
            query: (params = {}) => ({
                url: `/api/users/`,
                params
            })
        }),
        updateUser: builder.mutation({
            query: ({ user }) => ({
                url: `${USER_URL}/update`,
                method: "PUT",
                body: user
            }),
            invalidatesTags: ["User"],
        }),
        changePassword: builder.mutation({
            query: ({ oldPassword, newPassword }) => ({
                url: `${USER_URL}/change-password`,
                method: "PUT",
                body: { oldPassword, newPassword },
            }),
        }),
        updateUserAvatar: builder.mutation({
            query: ({ userId, avatar }) => ({
                url: `${USER_URL}/update-avatar`,
                method: "PUT",
                body: { avatar },
            }),
            invalidatesTags: ["User"],
        }),
        deleteUserAvatar: builder.mutation({
            query: () => ({
                url: `${USER_URL}/delete-avatar`,
                method: "DELETE",
            }),
            invalidatesTags: ["User"],
        }),
        sendResetPasswordLink: builder.mutation({
            query: (email) => ({
                url: `${AUTH_URL}/forgot-password`,
                method: "POST",
                body: email,
            })
        }),
        resetPassword: builder.mutation({
            query: ({ token, newPassword }) => ({
                url: `${AUTH_URL}/reset-password`,
                method: "POST",
                body: {
                    token,
                    newPassword,
                },
            }),
        }),
        loginWithGoogle: builder.mutation({
            query: (data) => ({
                url: `${AUTH_URL}/google`, 
                method: "POST",
                body: data, 
            }),
        })
    }),
});

export const {
    useSendOtpMutation,
    useRegisterMutation,
    useLoginMutation,
    useLogoutMutation,
    useGetUserQuery,
    useLazyGetUserQuery,
    useGetAllUsersQuery,
    useUpdateUserMutation,
    useChangePasswordMutation,
    useUpdateUserAvatarMutation,
    useDeleteUserAvatarMutation,
    useSendResetPasswordLinkMutation,
    useResetPasswordMutation,
    useLoginWithGoogleMutation
} = authApiSlice;