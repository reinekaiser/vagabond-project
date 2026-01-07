package com.ie207.vagabond.service;

import com.ie207.vagabond.model.*;
import com.ie207.vagabond.repository.*;
import com.ie207.vagabond.request.ReviewRequest;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.print.Doc;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final HotelBookingRepository hotelBookingRepository;
    private final HotelRepository hotelRepository;
    private final TourRepository tourRepository;
    private final CloudinaryService cloudinaryService;
    private final MongoTemplate mongoTemplate;
    private final HotelReviewRepository hotelReviewRepository;
    private final TourReviewRepository tourReviewRepository;
    private final TourBookingRepository tourBookingRepository;

    @Transactional
    public Object createReview(ReviewRequest request) {

        if (request.getUserId() == null || request.getUserId().isEmpty()
                || request.getProductId() == null || request.getProductId().isEmpty()
                || request.getBookingId() == null || request.getBookingId().isEmpty()
                || request.getProductType() == null || request.getProductType().isEmpty()
                || request.getRating() <= 0) {
            throw new IllegalArgumentException("Invalid review request");
        }

        String type = request.getProductType().trim().toLowerCase();

        Object savedReview;

        switch (type) {
            case "hotel": {
                if (!hotelRepository.existsById(request.getProductId())) {
                    throw new IllegalArgumentException("Hotel not found");
                }

                HotelReview review = HotelReview.builder()
                        .userId(request.getUserId())
                        .hotelId(request.getProductId())
                        .rating(request.getRating())
                        .comment(request.getComment())
                        .images(request.getImages())
                        .build();

                savedReview = hotelReviewRepository.save(review);

                HotelBooking booking = hotelBookingRepository.findById(request.getBookingId())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid hotel booking"));
                booking.setIsReviewed(true);
                hotelBookingRepository.save(booking);

                break;
            }

            case "tour": {
                if (!tourRepository.existsById(request.getProductId())) {
                    throw new IllegalArgumentException("Tour not found");
                }

                TourReview review = TourReview.builder()
                        .userId(request.getUserId())
                        .tourId(request.getProductId())
                        .rating(request.getRating())
                        .comment(request.getComment())
                        .images(request.getImages())
                        .build();

                savedReview = tourReviewRepository.save(review);

                TourBooking booking = tourBookingRepository.findById(request.getBookingId())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid tour booking"));
                booking.setIsReviewed(true);
                tourBookingRepository.save(booking);
                break;
            }

            default:
                throw new IllegalArgumentException("Invalid productType: must be Hotel or Tour");
        }

        updateAverageRating(type, request.getProductId());

        return savedReview;
    }

    @Transactional
    public Object updateReview(String id, ReviewRequest request) {
        String type = request.getProductType();
        if (type == null) {
            throw new IllegalArgumentException("Product type is required");
        }

        switch (type.toLowerCase()) {
            case "hotel":
                return updateHotelReview(id, request);
            case "tour":
                return updateTourReview(id, request);
            default:
                throw new IllegalArgumentException("Unsupported product type: " + type);
        }
    }

    @Transactional
    public HotelReview updateHotelReview(String id, ReviewRequest request) {
        HotelReview review = hotelReviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid review"));

        if (request.getComment() != null && !request.getComment().equals(review.getComment())) {
            review.setComment(request.getComment());
        }
        if (request.getImages() != null) {
            review.setImages(request.getImages());
        }
        if (request.getRating() != 0 && request.getRating() != review.getRating()) {
            review.setRating(request.getRating());
        }

        hotelReviewRepository.save(review);

        updateAverageRating("Hotel", request.getProductId());

        return review;
    }
    @Transactional
    public TourReview updateTourReview(String id, ReviewRequest request) {
        TourReview review = tourReviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid review"));

        if (request.getComment() != null && !request.getComment().equals(review.getComment())) {
            review.setComment(request.getComment());
        }
        if (request.getImages() != null) {
            review.setImages(request.getImages());
        }
        if (request.getRating() != 0 && request.getRating() != review.getRating()) {
            review.setRating(request.getRating());
        }

        tourReviewRepository.save(review);

        updateAverageRating("Tour", request.getProductId());

        return review;
    }

    @Transactional
    public String deleteReview(String id) throws IOException {
        Optional<HotelReview> hotelOpt = hotelReviewRepository.findById(id);
        if (hotelOpt.isPresent()) {
            HotelReview review = hotelOpt.get();

            if (review.getImages() != null && !review.getImages().isEmpty()) {
                for (String publicId : review.getImages()) {
                    cloudinaryService.deleteImage(publicId);
                }
            }

            hotelReviewRepository.deleteById(id);
            updateAverageRating("Hotel", review.getHotelId());

            return "Hotel review deleted successfully";
        }

        Optional<TourReview> tourOpt = tourReviewRepository.findById(id);
        if (tourOpt.isPresent()) {
            TourReview review = tourOpt.get();

            if (review.getImages() != null && !review.getImages().isEmpty()) {
                for (String publicId : review.getImages()) {
                    cloudinaryService.deleteImage(publicId);
                }
            }

            tourReviewRepository.deleteById(id);
            updateAverageRating("Tour", review.getTourId());

            return "Tour review deleted successfully";
        }

        throw new IllegalArgumentException("Review with ID " + id + " not found");
    }

    @Transactional
    public void updateAverageRating(String productType, String productId) {
        String type = productType.trim().toLowerCase();

        switch (type) {
            case "hotel": {
                List<HotelReview> hotelReviews = hotelReviewRepository.findByHotelId(productId);
                double totalRating = hotelReviews.stream()
                        .mapToDouble(HotelReview::getRating)
                        .sum();
                double avgRating = hotelReviews.isEmpty() ? 0 : totalRating / hotelReviews.size();

                Hotel hotel = hotelRepository.findById(productId)
                        .orElseThrow(() -> new IllegalArgumentException("Hotel not found"));
                hotel.setAverageRating(avgRating);
                hotelRepository.save(hotel);

                break;
            }
            case "tour": {
                List<TourReview> tourReviews = tourReviewRepository.findByTourId(productId);
                double totalRating = tourReviews.stream()
                        .mapToDouble(TourReview::getRating)
                        .sum();
                double avgRating = tourReviews.isEmpty() ? 0 : totalRating / tourReviews.size();

                Tour tour = tourRepository.findById(productId)
                        .orElseThrow(() -> new IllegalArgumentException("Tour not found"));
                tour.setAvgRating(avgRating);
                tourRepository.save(tour);

                break;
            }
            default:
                throw new IllegalArgumentException("Invalid productType: " + productType);
        }
    }

    public List<Document> getHotelOrdersCanReview(String userId) {
        LocalDate today = LocalDate.now();
        List<String> excludedStatuses = Arrays.asList("pending", "cancelled");

        List<Document> pipeline = new ArrayList<>();

        pipeline.add(new Document("$match", new Document("userId", userId)
                .append("bookingStatus", new Document("$nin", excludedStatuses))
                .append("isReviewed", false)
                .append("checkout", new Document("$lte", today))
        ));

        pipeline.add(new Document("$addFields", new Document("hotelObjectId", new Document("$toObjectId", "$hotelId"))));
        pipeline.add(new Document("$lookup", new Document()
                .append("from", "hotels")
                .append("localField", "hotelObjectId")
                .append("foreignField", "_id")
                .append("as", "hotel")
        ));
        pipeline.add(new Document("$unwind", new Document("path", "$hotel").append("preserveNullAndEmptyArrays", true)));

        pipeline.add(new Document("$project", new Document()
                .append("_id", 1)
                .append("hotelId", 1)
                .append("checkin", 1)
                .append("checkout", 1)
                .append("numGuests", 1)
                .append("numRooms", 1)
                .append("totalPrice", 1)
                .append("bookingStatus", 1)
                .append("hotelName", "$hotel.name")
                .append("hotelImg", "$hotel.img")
        ));

        List<Document> bookings = mongoTemplate.getCollection("hotelbookings")
                .aggregate(pipeline)
                .into(new ArrayList<>());

        bookings.forEach(doc -> {
            if (doc.get("_id") instanceof ObjectId) {
                doc.put("_id", ((ObjectId) doc.get("_id")).toHexString());
            }
        });

        return bookings;
    }

    public List<Document> getTourOrdersCanReview(String userId) {
        LocalDate today = LocalDate.now();
        List<String> excludedStatuses = Arrays.asList("pending", "cancelled");

        List<Document> pipeline = new ArrayList<>();

        pipeline.add(new Document("$match", new Document("userId", userId)
                .append("bookingStatus", new Document("$nin", excludedStatuses))
                .append("isReviewed", false)
                .append("useDate", new Document("$lte", today))
        ));

        pipeline.add(new Document("$addFields", new Document("tourObjectId", new Document("$toObjectId", "$tourId"))));
        pipeline.add(new Document("$lookup", new Document()
                .append("from", "tours")
                .append("localField", "tourObjectId")
                .append("foreignField", "_id")
                .append("as", "tour")
        ));
        pipeline.add(new Document("$unwind", new Document("path", "$tour").append("preserveNullAndEmptyArrays", true)));

        pipeline.add(new Document("$project", new Document()
                .append("_id", 1)
                .append("tourId", 1)
                .append("useDate", 1)
                .append("totalPrice", 1)
                .append("bookingStatus", 1)
                .append("tourName", "$tour.name")
                .append("tourImg", "$tour.images")
        ));

        List<Document> bookings = mongoTemplate.getCollection("tourbookings")
                .aggregate(pipeline)
                .into(new ArrayList<>());

        bookings.forEach(doc -> {
            if (doc.get("_id") instanceof ObjectId) {
                doc.put("_id", ((ObjectId) doc.get("_id")).toHexString());
            }
        });

        return bookings;
    }

    public List<Document> getMyReviews(String userId) {
        if (userId == null) throw new IllegalArgumentException("userId is required");

        List<Document> reviews = new ArrayList<>();

        reviews.addAll(getHotelReviewsByUser(userId));
        reviews.addAll(getTourReviewsByUser(userId));

        reviews.forEach(doc -> {
            if (doc.get("_id") instanceof ObjectId) {
                doc.put("_id", ((ObjectId) doc.get("_id")).toHexString());
            }
        });

        return reviews;
    }

    private List<Document> getHotelReviewsByUser(String userId) {
        List<Document> pipeline = new ArrayList<>();
        pipeline.add(new Document("$match", new Document("userId", userId)));
        pipeline.add(new Document("$addFields", new Document("hotelObjectId", new Document("$toObjectId", "$hotelId"))
                .append("userObjectId", new Document("$toObjectId", "$userId"))));

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

        pipeline.add(new Document("$project", new Document()
                .append("_id", 1)
                .append("rating", 1)
                .append("comment", 1)
                .append("images", 1)
                .append("createdAt", 1)
                .append("updatedAt", 1)
                .append("productType", "Hotel")
                .append("userId", 1)
                .append("productId", "$hotelId")
                .append("userFirstName", "$user.firstName")
                .append("userLastName", "$user.lastName")
                .append("userProfilePicture", "$user.avatarUrl")
                .append("productName", "$hotel.name")
                .append("productImage", "$hotel.img")
        ));

        return mongoTemplate.getCollection("hotelreviews")
                .aggregate(pipeline)
                .into(new ArrayList<>());
    }
    private List<Document> getTourReviewsByUser(String userId) {
        List<Document> pipeline = new ArrayList<>();
        pipeline.add(new Document("$match", new Document("userId", userId)));
        pipeline.add(new Document("$addFields", new Document("tourObjectId", new Document("$toObjectId", "$tourId"))
                .append("userObjectId", new Document("$toObjectId", "$userId"))));

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

        pipeline.add(new Document("$project", new Document()
                .append("_id", 1)
                .append("rating", 1)
                .append("comment", 1)
                .append("images", 1)
                .append("createdAt", 1)
                .append("updatedAt", 1)
                .append("productType", "Tour")
                .append("userId", 1)
                .append("productId", "$tourId")
                .append("userFirstName", "$user.firstName")
                .append("userLastName", "$user.lastName")
                .append("userProfilePicture", "$user.avatarUrl")
                .append("productName", "$tour.name")
                .append("productImage", "$tour.images")
        ));

        return mongoTemplate.getCollection("tourreviews")
                .aggregate(pipeline)
                .into(new ArrayList<>());
    }

    public List<Document> getReviewsByProduct(String productId, String productType) {
        if (productId == null || productType == null) {
            throw new IllegalArgumentException("Missing productId or productType");
        }

        String collectionName;
        String idField;
        switch (productType.toLowerCase()) {
            case "hotel":
                collectionName = "hotelreviews";
                idField = "hotelId";
                break;
            case "tour":
                collectionName = "tourreviews";
                idField = "tourId";
                break;
            default:
                throw new IllegalArgumentException("Unsupported productType: " + productType);
        }

        List<Document> pipeline = new ArrayList<>();
        pipeline.add(new Document("$match", new Document()
                .append(idField, productId)
        ));

        pipeline.add(new Document("$addFields", new Document("userObjectId", new Document("$toObjectId", "$userId"))));
        pipeline.add(new Document("$lookup", new Document()
                .append("from", "users")
                .append("localField", "userObjectId")
                .append("foreignField", "_id")
                .append("as", "user")
        ));
        pipeline.add(new Document("$unwind", new Document("path", "$user").append("preserveNullAndEmptyArrays", true)));

        pipeline.add(new Document("$project", new Document()
                .append("_id", 1)
                .append("rating", 1)
                .append("comment", 1)
                .append("images", 1)
                .append("createdAt", 1)
                .append("updatedAt", 1)
                .append("userId", 1)
                .append("userFirstName", "$user.firstName")
                .append("userLastName", "$user.lastName")
                .append("userEmail", "$user.email")
                .append("userAvatar", "$user.avatarUrl")
        ));

        List<Document> reviews = mongoTemplate.getCollection(collectionName)
                .aggregate(pipeline)
                .into(new ArrayList<>());

        reviews.forEach(doc -> {
            if (doc.get("_id") instanceof ObjectId) {
                doc.put("_id", ((ObjectId) doc.get("_id")).toHexString());
            }
        });

        return reviews;
    }


    public List<Document> getReviewByCities(String cityId) {
        List<Tour> tours = tourRepository.findToursByCityId(cityId);

        Map<String, String> tourMap = new HashMap<>();
        List<String> tourIds = new ArrayList<>();

        for (Tour tour : tours) {
            tourMap.put(tour.get_id(), tour.getName());
            tourIds.add(tour.get_id());
        }

        List<Document> pipeline = new ArrayList<>();

        pipeline.add(new Document("$match",
                new Document("tourId", new Document("$in", tourIds))
        ));

        pipeline.add(new Document("$sort", new Document("createdAt", -1)));
        pipeline.add(new Document("$group", new Document()
                .append("_id", "$userId")
                .append("review", new Document("$first", "$$ROOT"))
        ));
        pipeline.add(new Document("$replaceRoot",
                new Document("newRoot", "$review")
        ));

        pipeline.add(new Document("$addFields", new Document("userObjectId", new Document("$toObjectId", "$userId"))));
        pipeline.add(new Document("$lookup", new Document()
                .append("from", "users")
                .append("localField", "userObjectId")
                .append("foreignField", "_id")
                .append("as", "user")
        ));
        pipeline.add(new Document("$unwind", new Document("path", "$user").append("preserveNullAndEmptyArrays", true)));

        pipeline.add(new Document("$project", new Document()
                .append("_id", 1)
                .append("tourId", 1)
                .append("rating", 1)
                .append("comment", 1)
                .append("images", 1)
                .append("createdAt", 1)
                .append("updatedAt", 1)
                .append("userId", 1)
                .append("userFirstName", "$user.firstName")
                .append("userLastName", "$user.lastName")
                .append("userEmail", "$user.email")
                .append("userAvatar", "$user.avatarUrl")
        ));

        List<Document> reviews = mongoTemplate
                .getCollection("tourreviews")
                .aggregate(pipeline)
                .into(new ArrayList<>());

        reviews.forEach(doc -> {
            if (doc.get("_id") instanceof ObjectId) {
                doc.put("_id", ((ObjectId) doc.get("_id")).toHexString());
            }

            String tourId = doc.getString("tourId");
            if (tourId != null) {
                doc.put("tourName", tourMap.get(tourId));
            }
        });

        return reviews;
    }


}
