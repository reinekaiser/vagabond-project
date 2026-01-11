import React, { useState, useEffect } from "react";

import StatsCards from "../../components/Dashboard/StatsCards";
import RevenueChart from "../../components/Dashboard/RevenueChart";
import TopProducts from "../../components/Dashboard/TopProducts";
import RecentBookings from "../../components/Dashboard/RecentBookings";
import TopCustomers from "../../components/Dashboard/TopCustomers";
import { ChevronDownIcon } from "@heroicons/react/24/outline";
import {
    useGetDashboardStatsQuery,
    useGetRecentBookingsQuery,
    useGetRevenueChartQuery,
    useGetTopCustomersQuery,
    useGetTopHotelsQuery,
    useGetTopToursQuery,
} from "../../redux/api/dashboardApiSlice";

const Dashboard = () => {
    const [period, setPeriod] = useState("7");

    const { data: stats, isLoading: loadingStats } = useGetDashboardStatsQuery(period);

    const { data: chartData, isLoading: loadingChart } = useGetRevenueChartQuery(period);

    const { data: topTours, isLoading: loadingTours } = useGetTopToursQuery(3);

    const { data: topHotels, isLoading: loadingHotels } = useGetTopHotelsQuery(3);

    const { data: recentBookings, isLoading: loadingBookings } = useGetRecentBookingsQuery(5);

    const { data: topCustomers, isLoading: loadingCustomers } = useGetTopCustomersQuery(3);

    const loading =
        loadingStats ||
        loadingChart ||
        loadingTours ||
        loadingHotels ||
        loadingBookings ||
        loadingCustomers;

    const periodOptions = [
        { value: "7", label: "7 ngày qua" },
        { value: "30", label: "30 ngày qua" },
        { value: "365", label: "Năm qua" },
    ];

    if (loading) {
        return (
            <div className="flex items-center justify-center min-h-screen">
                <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-500"></div>
            </div>
        );
    }


    return (
        <div className="p-6 bg-gray-50 min-h-screen">
            {/* Header */}
            <div className="flex items-center justify-between mb-8">
                <div>
                    <h1 className="text-3xl font-bold text-gray-900">Chào mừng trở lại, Admin!</h1>
                    <p className="text-gray-600 mt-1">
                        Đây là thông tin hoạt động của hệ thống hôm nay
                    </p>
                </div>

                <div className="flex items-center gap-4">
                    <div className="relative">
                        <select
                            value={period}
                            onChange={(e) => setPeriod(e.target.value)}
                            className="appearance-none bg-white border border-gray-300 rounded-lg px-4 py-2 pr-8 text-sm font-medium text-gray-700 hover:border-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        >
                            {periodOptions.map((option) => (
                                <option key={option.value} value={option.value}>
                                    {option.label}
                                </option>
                            ))}
                        </select>
                        <ChevronDownIcon className="absolute right-2 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400 pointer-events-none" />
                    </div>
                </div>
            </div>

            {/* Stats Cards */}
            {stats && <StatsCards stats={stats} />}

            <div className="mt-8">
                <RevenueChart data={chartData} period={period} />
            </div>

            {/* Bottom Section */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mt-6">
                {/* Recent Bookings - Takes 2 columns */}
                <div className="lg:col-span-2">
                    <RecentBookings bookings={recentBookings} />
                </div>

                {/* Top Customers */}
                <div className="lg:col-span-1 flex flex-col gap-3">
                    <TopProducts tours={topTours} hotels={topHotels} />
                    <TopCustomers customers={topCustomers} />
                </div>
            </div>
        </div>
    );
};

export default Dashboard;
