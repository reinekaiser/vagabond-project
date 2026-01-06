import { MESSAGE_URL } from "../constants";
import { apiSlice } from "./apiSlice";

export const messageApiSlice = apiSlice.injectEndpoints({
    endpoints: (builder) => ({
        getUsers: builder.query({
            query: () => {
                return {
                    url: `${MESSAGE_URL}/users`
                }
            },
            providesTags: ['UserToChat']
        }),
        getUserToChat: builder.query({
            query: () => ({
                url: `${MESSAGE_URL}/users/chat`
            })
        }),
        getMessages: builder.query({
            query: (id) => ({
                url: `${MESSAGE_URL}/history/${id}`
            }),
        }),
        markMessagesAsRead: builder.mutation({
            query: (id) => ({
                url: `${MESSAGE_URL}/read/${id}`,
                method: "PUT"
            })
        }),
        sendMessage: builder.mutation({
            query: ({ id, text }) => ({
                    url: `${MESSAGE_URL}/send/${id}`,
                    method: "POST",
                    body: { text },
                }),
            invalidatesTags: ['UserToChat']
        })
    }),
});

export const {
    useGetUsersQuery,
    useGetMessagesQuery,
    useGetUserToChatQuery,
    useMarkMessagesAsReadMutation,
    useSendMessageMutation
} = messageApiSlice