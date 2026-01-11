import React from 'react';
import { 
    CurrencyDollarIcon, 
    UsersIcon, 
    TicketIcon,
    BuildingOfficeIcon,
    ArrowUpIcon,
    ArrowDownIcon 
} from '@heroicons/react/24/outline';

const StatsCards = ({ stats }) => {
    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND',
            minimumFractionDigits: 0
        }).format(amount);
    };

    const formatNumber = (num) => {
        if (num >= 1000) {
            return (num / 1000).toFixed(1) + 'K';
        }
        return num.toString();
    };

    const formatPercentage = (value) => {
        return `${value.toFixed(2)}%`;
    };

    const cards = [
        {
            title: 'Tổng Doanh Thu',
            value: formatCurrency(stats.totalRevenue),
            icon: CurrencyDollarIcon,
            bgColor: 'bg-orange-50',
            iconColor: 'text-orange-600'
        },
        {
            title: 'Khách Hàng Mới',
            value: stats.newUsersCount.toString(),
            icon: UsersIcon,
            bgColor: 'bg-green-50',
            iconColor: 'text-green-600'
        },
        {
            title: 'Booking Tour',
            value: `${stats.tourBookingsCount || 0}`,
            icon: TicketIcon,
            bgColor: 'bg-blue-50',
            iconColor: 'text-blue-600'
        },
        {
            title: 'Booking Khách Sạn',
            value: `${stats.hotelBookingsCount}`,
            icon: BuildingOfficeIcon,
            bgColor: 'bg-purple-50',
            iconColor: 'text-purple-600'
        },
        {
            title: 'Tổng Người Dùng',
            value: formatNumber(stats.totalUsers),
            icon: UsersIcon,
            bgColor: 'bg-pink-50',
            iconColor: 'text-pink-600'
        }
    ];

    return (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-6">
            {cards.map((card, index) => (
                <div key={index} className={`${card.bgColor} rounded-2xl p-4 border border-gray-100`}>
                    <div className="flex items-center justify-between mb-4">
                        <div className={`p-2 rounded-lg ${card.bgColor}`}>
                            <card.icon className={`h-6 w-6 ${card.iconColor}`} />
                        </div>
                    </div>
                    
                    <div className="space-y-2">
                        <h3 className="text-sm font-medium text-gray-600">{card.title}</h3>
                        <p className="text-2xl font-bold text-gray-900">{card.value}</p>
                        
                    </div>
                </div>
            ))}
        </div>
    );
};

export default StatsCards; 