import { apiSlice } from "./apiSlice";
import { TOUR_BOOKING_URL, PAYPAL_URL, STRIPE_URL, PAYOS_URL, VNPAY_URL } from "../constants";

export const tourBookingSlice = apiSlice.injectEndpoints({
    endpoints: (builder) => ({
        capturePaypalOrderAndSaveTourBooking: builder.mutation({
            query: (bookingData) => ({
                url: `${PAYPAL_URL}/capture-tour-booking`,
                method: "POST",
                body: bookingData,
            }),
        }),
        createPaypalOrder: builder.mutation({
            query: (paypalOrder) => ({
                url: `${PAYPAL_URL}/create-tour-booking`,
                method: "POST",
                body: paypalOrder,
            }),
        }),
        createTourCheckoutSession: builder.mutation({
            query: (bookingData) => ({
                url: `${STRIPE_URL}/create-tour-checkout-session`,
                method: "POST",
                body: bookingData,
            }),
        }),
        getStripeBookingStatus: builder.query({
            query: (sessionId) => `${STRIPE_URL}/booking-status?session_id=${sessionId}`,
        }),
        getMyTourBookings: builder.query({
            query: () => ({
                url: `${TOUR_BOOKING_URL}/my`,
            }),
        }),
        cancelTourBooking: builder.mutation({
            query: (id) => ({
                url: `${TOUR_BOOKING_URL}/${id}/cancel`,
                method: "PUT",
            }),
        }),
        updateTourBookingStatus: builder.mutation({
            query: ({ id, bookingStatus }) => ({
                url: `${TOUR_BOOKING_URL}/${id}/status`,
                method: "PUT",
                body: bookingStatus,
            }),
        }),
        getTourBookings: builder.query({
            query: (params = {}) => ({
                url: `${TOUR_BOOKING_URL}/bookings`,
                params,
            }),
        }),
        createTourPayOSLink: builder.mutation({
            query: (bookingData) => ({
                url: `${PAYOS_URL}/create-tour-checkout-link`,
                method: "POST",
                body: bookingData,
            })
        }),
        saveTourBooking: builder.mutation({
            query: ({ orderCode, bookingData }) => ({
                url: `${PAYOS_URL}/save-tour-booking`,
                method: "POST",
                body: {
                    orderCode,
                    bookingData,
                },
            })
        }),
        createTourVnpayOrder: builder.mutation({
            query: (vnpayOrder) => ({
                url: `${VNPAY_URL}/create-tour-order`,
                method: "POST",
                body: vnpayOrder,
            })
        }),
        captureTourVnpayOrder: builder.mutation({
            query: ({ allParams, request }) => ({
                url: `${VNPAY_URL}/capture-tour-booking`,
                method: "POST",
                params: allParams, 
                body: request,
            })
        })
    }),
});

export const {
    useCapturePaypalOrderAndSaveTourBookingMutation,
    useCreatePaypalOrderMutation,
    useCreateTourCheckoutSessionMutation,
    useLazyGetStripeBookingStatusQuery,
    useGetMyTourBookingsQuery,
    useCancelTourBookingMutation,
    useUpdateTourBookingStatusMutation,
    useGetTourBookingsQuery,
    useCreateTourPayOSLinkMutation,
    useSaveTourBookingMutation,
    useCreateTourVnpayOrderMutation,
    useCaptureTourVnpayOrderMutation,
} = tourBookingSlice;
