package com.ie207.vagabond.repository;

import com.ie207.vagabond.dto.RevenueAggregationResult;
import com.ie207.vagabond.model.HotelBooking;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public interface HotelBookingRepository extends MongoRepository<HotelBooking, String> {
    List<HotelBooking> findByUserIdOrderByCreatedAtDesc(String userId);
    List<HotelBooking> findByHotelIdAndRoomTypeIdAndRoomIdAndCheckinBeforeAndCheckoutAfterAndBookingStatusIn(
            String hotelId,
            String roomTypeId,
            String roomId,
            LocalDate checkinBefore,
            LocalDate checkoutAfter,
            List<String> bookingStatus
    );
    List<HotelBooking> findByUserIdAndBookingStatusNotInAndIsReviewedIsFalseAndCheckoutLessThanEqual(
            String userId,
            List<String> excludedStatuses,
            LocalDate checkoutDate
    );

    long countByCreatedAtAfter(LocalDateTime date);

    long countByBookingStatusAndCreatedAtAfter(String bookingStatus, LocalDateTime date);

    @Aggregation(pipeline = {
            "{ $match: { bookingStatus: 'confirmed', createdAt: { $gte: ?0 } } }",
            "{ $group: { _id: null, total: { $sum: '$totalPrice' } } }"
    })
    Double sumTotalPriceByConfirmedStatusAndCreatedAtAfter(LocalDateTime date);

    @Aggregation(pipeline = {
            "{ $match: { bookingStatus: 'confirmed', createdAt: { $gte: ?0 } } }",
            "{ $group: { " +
                    "    _id: { " +
                    "        year: { $year: '$createdAt' }, " +
                    "        month: { $month: '$createdAt' }, " +
                    "        day: { $dayOfMonth: '$createdAt' } " +
                    "    }, " +
                    "    revenue: { $sum: '$totalPrice' }, " +
                    "    bookings: { $sum: 1 } " +
                    "} }",
            "{ $sort: { '_id.year': 1, '_id.month': 1, '_id.day': 1 } }"
    })
    List<RevenueAggregationResult> getDailyRevenue(LocalDateTime startDate);

    @Query(value = "{}", sort = "{ 'createdAt': -1}")
    List<HotelBooking> findRecentBookings(Pageable pageable);
}
