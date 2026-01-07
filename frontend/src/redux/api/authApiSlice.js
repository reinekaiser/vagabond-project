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
            })
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
            })
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
            })
        }),
        deleteUserAvatar: builder.mutation({
            query: () => ({
                url: `${USER_URL}/delete-avatar`,
                method: "DELETE",
            })
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
    useDeleteUserAvatarMutation
} = authApiSlice;