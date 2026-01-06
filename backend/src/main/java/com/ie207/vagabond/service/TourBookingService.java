package com.ie207.vagabond.service;


import com.ie207.vagabond.model.TourBooking;
import com.ie207.vagabond.repository.TicketRepository;
import com.ie207.vagabond.repository.TourBookingRepository;
import com.ie207.vagabond.repository.TourRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TourBookingService {
    private final TourBookingRepository tourBookingRepository;
    private final TourRepository tourRepository;
    private final MongoTemplate mongoTemplate;
    private final TicketRepository ticketRepository;


    public List<Map<String, Object>> getMyTourBookings(String userId) {
        List<TourBooking> bookings = tourBookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<Map<String, Object>> result = bookings.stream().map(b-> {
            Map<String, Object> map = new HashMap<>();
            map.put("_id", b.get_id());
            map.put("userId", b.getUserId());
            map.put("tourId", b.getTourId());
            map.put("ticketId", b.getTicketId());
            map.put("tourImg", b.getTourImg());
            map.put("useDate", b.getUseDate());
            map.put("totalPrice", b.getTotalPrice());
            map.put("bookingStatus", b.getBookingStatus());

            tourRepository.findById(b.getTourId()).ifPresent(t-> {
                map.put("name", t.getName());
            });

            ticketRepository.findById(b.getTicketId()).ifPresent(t-> {
                map.put("title", t.getTitle());
            });

            return map;
        }).collect(Collectors.toList());

        return result;
    }

    @Transactional
    public TourBooking cancelTourBooking(String id) {
        TourBooking tourBooking = tourBookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking không tồn tại"));
        tourBooking.setBookingStatus("canceled");
        return tourBookingRepository.save(tourBooking);
    }

    @Transactional
    public Map<String, Object> getTourBookings(String bookingStatus, int page, int limit) {
        int pageNumber = page > 0 ? page : 1;
        int pageSize = limit > 0 ? limit : 5;
        int skip = (pageNumber - 1) * pageSize;

        List<Document> pipeline = new ArrayList<>();
        if (bookingStatus != null && !bookingStatus.equals("all")) {
            pipeline.add(new Document("$match", new Document("bookingStatus", bookingStatus)));
        }
        pipeline.add(new Document("$addFields", new Document()
                .append("userObjectId", new Document("$toObjectId", "$userId"))
                .append("tourObjectId", new Document("$toObjectId", "$tourId"))
                .append("ticketObjectId", new Document("$toObjectId", "$ticketId"))
        ));

        pipeline.add(new Document("$lookup", new Document()
                .append("from", "users")
                .append("localField", "userObjectId")
                .append("foreignField", "_id")
                .append("as", "user")));
        pipeline.add(new Document("$unwind", new Document("path", "$user").append("preserveNullAndEmptyArrays", true)));

        pipeline.add(new Document("$lookup", new Document()
                .append("from", "tours")
                .append("localField", "tourObjectId")
                .append("foreignField", "_id")
                .append("as", "tour")));
        pipeline.add(new Document("$unwind", new Document("path", "$tour").append("preserveNullAndEmptyArrays", true)));

        pipeline.add(new Document("$lookup", new Document()
                .append("from", "tickets")
                .append("localField", "ticketObjectId")
                .append("foreignField", "_id")
                .append("as", "ticket")));
        pipeline.add(new Document("$unwind", new Document("path", "$ticket").append("preserveNullAndEmptyArrays", true)));

        pipeline.add(new Document("$skip", skip));
        pipeline.add(new Document("$limit", pageSize));

        pipeline.add(new Document("$project", new Document()
                .append("bookingStatus", 1)
                .append("totalPrice", 1)
                .append("useDate", 1)
                .append("userFirstName", "$user.firstName")
                .append("userLastName", "$user.lastName")
                .append("tourName", "$tour.name")
                .append("ticketName", "$ticket.title")
        ));

        List<Document> bookings = mongoTemplate
                .getCollection("tourbookings")
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
        long total = mongoTemplate.getCollection("tourbookings").countDocuments(matchCount);

        Map<String, Object> response = new HashMap<>();
        response.put("bookings", bookings);
        response.put("total", total);
        response.put("currentPage", pageNumber);
        response.put("pageSize", pageSize);
        response.put("totalPages", (int) Math.ceil((double) total / pageSize));
        return response;
    }

    @Transactional
    public TourBooking updateTourBookingStatus(String id, String status) {
        TourBooking booking = tourBookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TourBooking not found"));
        booking.setBookingStatus(status);
        return tourBookingRepository.save(booking);
    }

}
