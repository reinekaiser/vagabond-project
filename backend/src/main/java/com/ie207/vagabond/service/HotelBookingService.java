package com.ie207.vagabond.service;

import com.ie207.vagabond.model.HotelBooking;
import com.ie207.vagabond.model.HotelRoomType;
import com.ie207.vagabond.repository.HotelBookingRepository;
import com.ie207.vagabond.repository.HotelRepository;
import com.ie207.vagabond.repository.HotelRoomTypeRepository;
import com.ie207.vagabond.repository.UserRepository;
import com.ie207.vagabond.request.HotelBookingRequest;
import com.ie207.vagabond.response.GetHotelBookingResponse;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelBookingService {
    private final HotelBookingRepository hotelBookingRepository;
    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final HotelRoomTypeRepository hotelRoomTypeRepository;
    private final MongoTemplate mongoTemplate;
    private final HotelRoomTypeRepository roomTypeRepository;

    public List<Map<String, Object>> getMyBookings(String userId) {
        List<HotelBooking> bookings = hotelBookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<Map<String, Object>> result = bookings.stream().map(b -> {
            Map<String, Object> map = new HashMap<>();
            map.put("_id", b.get_id());
            map.put("userId", b.getUserId());
            map.put("hotelId", b.getHotelId());
            map.put("roomTypeId", b.getRoomTypeId());
            map.put("roomId", b.getRoomId());
            map.put("name", b.getName());
            map.put("email", b.getEmail());
            map.put("phone", b.getPhone());
            map.put("checkin", b.getCheckin());
            map.put("checkout", b.getCheckout());
            map.put("numGuests", b.getNumGuests());
            map.put("numRooms", b.getNumRooms());
            map.put("paymentMethod", b.getPaymentMethod());
            map.put("totalPrice", b.getTotalPrice());
            map.put("bookingStatus", b.getBookingStatus());
            map.put("isReviewed", b.getIsReviewed());
            map.put("createdAt", b.getCreatedAt());
            map.put("updatedAt", b.getUpdatedAt());

            hotelRepository.findById(b.getHotelId()).ifPresent(h -> {
                map.put("hotelName", h.getName());
                map.put("hotelImg", h.getImg());
            });

            roomTypeRepository.findById(b.getRoomTypeId()).ifPresent(r -> {
                map.put("roomTypeName", r.getName());
            });

            return map;
        }).collect(Collectors.toList());

        return result;
    }


    @Transactional
    public HotelBooking updateHotelBookingStatus(String id, String status) {
        HotelBooking booking = hotelBookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking không tồn tại"));

        booking.setBookingStatus(status);
        return hotelBookingRepository.save(booking);
    }

    @Transactional
    public HotelBooking cancelHotelBooking(String id) {
        HotelBooking booking = hotelBookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking không tồn tại"));
        booking.setBookingStatus("canceled");
        return hotelBookingRepository.save(booking);
    }

    @Transactional
    public String deleteHotelBooking(String id) {
        if (!hotelBookingRepository.existsById(id)) {
            throw new IllegalArgumentException("Booking không tồn tại");
        }

        hotelBookingRepository.deleteById(id);
        return "Hotel booking deleted";
    }

    @Transactional
    public Map<String, Object> getHotelBookings(String bookingStatus, int page, int limit) {
        int pageNumber = page > 0 ? page : 1;
        int pageSize = limit > 0 ? limit : 5;
        int skip = (pageNumber - 1) * pageSize;

        List<Document> pipeline = new ArrayList<>();

        if (bookingStatus != null && !bookingStatus.equals("all")) {
            pipeline.add(new Document("$match", new Document("bookingStatus", bookingStatus)));
        }
        pipeline.add(new Document("$addFields", new Document()
                .append("userObjectId", new Document("$toObjectId", "$userId"))
                .append("hotelObjectId", new Document("$toObjectId", "$hotelId"))
                .append("roomTypeObjectId", new Document("$toObjectId", "$roomTypeId"))
        ));
        pipeline.add(new Document("$lookup", new Document()
                .append("from", "users")
                .append("localField", "userObjectId")
                .append("foreignField", "_id")
                .append("as", "user")));
        pipeline.add(new Document("$unwind", new Document("path", "$user").append("preserveNullAndEmptyArrays", true)));

        pipeline.add(new Document("$lookup", new Document()
                .append("from", "hotels")
                .append("localField", "hotelObjectId")
                .append("foreignField", "_id")
                .append("as", "hotel")));
        pipeline.add(new Document("$unwind", new Document("path", "$hotel").append("preserveNullAndEmptyArrays", true)));

        pipeline.add(new Document("$lookup", new Document()
                .append("from", "hotelroomtypes")
                .append("localField", "roomTypeObjectId")
                .append("foreignField", "_id")
                .append("as", "roomType")));
        pipeline.add(new Document("$unwind", new Document("path", "$roomType").append("preserveNullAndEmptyArrays", true)));

        pipeline.add(new Document("$skip", skip));
        pipeline.add(new Document("$limit", pageSize));

        pipeline.add(new Document("$project", new Document()
                .append("bookingStatus", 1)
                .append("checkin", 1)
                .append("checkout", 1)
                .append("numGuests", 1)
                .append("numRooms", 1)
                .append("totalPrice", 1)
                .append("userFirstName", "$user.firstName")
                .append("userLastName", "$user.lastName")
                .append("hotelName", "$hotel.name")
                .append("roomTypeName", "$roomType.name")
        ));

        List<Document> bookings = mongoTemplate
                .getCollection("hotelbookings")
                .aggregate(pipeline)
                .into(new ArrayList<>());
        bookings.forEach(doc -> {
            if (doc.get("_id") instanceof ObjectId) {
                doc.put("_id", ((ObjectId) doc.get("_id")).toHexString());
            }
        });

        Document matchCount = new Document();
        if (bookingStatus != null && !bookingStatus.equals("all")) {
            matchCount.append("bookingStatus", bookingStatus);
        }
        long total = mongoTemplate.getCollection("hotelbookings").countDocuments(matchCount);

        Map<String, Object> response = new HashMap<>();
        response.put("bookings", bookings);
        response.put("total", total);
        response.put("currentPage", pageNumber);
        response.put("pageSize", pageSize);
        response.put("totalPages", (int) Math.ceil((double) total / pageSize));

        return response;
    }

}
