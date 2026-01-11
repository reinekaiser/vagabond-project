import { DASHBOARD_URL } from "../constants";
import { apiSlice } from "./apiSlice";

export const dashboardApiSlice = apiSlice.injectEndpoints({
    endpoints: (buider) => ({
        getDashboardStats: buider.query({
            query: (period = 7) => ({
                url: `${DASHBOARD_URL}/stats`,
                params: { period },
            }),
        }),
        getRevenueChart: buider.query({
            query: (period = 7) => ({
                url: `${DASHBOARD_URL}/revenue-chart`,
                params: { period },
            }),
        }),
        getTopTours: buider.query({
            query: (limit = 5) => ({
                url: `${DASHBOARD_URL}/top-tours`,
                params: { limit },
            }),
        }),
        getTopHotels: buider.query({
            query: (limit = 5) => ({
                url: `${DASHBOARD_URL}/top-hotels`,
                params: { limit },
            }),
        }),
        getRecentBookings: buider.query({
            query: (limit = 10) => ({
                url: `${DASHBOARD_URL}/recent-bookings`,
                params: { limit },
            }),
        }),
        getTopCustomers: buider.query({
            query: (limit = 5) => ({
                url: `${DASHBOARD_URL}/top-customers`,
                params: { limit },
            }),
        }),
    }),
});

export const {
    useGetDashboardStatsQuery,
    useGetRevenueChartQuery,
    useGetTopToursQuery,
    useGetTopHotelsQuery,
    useGetRecentBookingsQuery,
    useGetTopCustomersQuery
} = dashboardApiSlice;
