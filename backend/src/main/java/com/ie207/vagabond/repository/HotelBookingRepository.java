package com.ie207.vagabond.repository;

import com.ie207.vagabond.model.HotelBooking;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
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
}
