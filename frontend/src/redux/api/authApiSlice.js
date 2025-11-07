import { AUTH_URL } from "../constants";
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
    }),
});

export const { 
    useSendOtpMutation, 
    useRegisterMutation, 
    useLoginMutation,
    useLogoutMutation,
    useGetUserQuery,
    useLazyGetUserQuery,
    useGetAllUsersQuery
} = authApiSlice;