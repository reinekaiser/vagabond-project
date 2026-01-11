package com.ie207.vagabond.service;

import com.ie207.vagabond.dto.CustomerData;
import com.ie207.vagabond.dto.RevenueAggregationResult;
import com.ie207.vagabond.model.*;
import com.ie207.vagabond.repository.*;
import com.ie207.vagabond.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConvertOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final TourBookingRepository tourBookingRepository;
    private final HotelBookingRepository hotelBookingRepository;
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;
    private final TourRepository tourRepository;
    private final HotelRepository hotelRepository;

    public DashboardStatsResponse getDashboardStats(int periodDays) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(periodDays);

        Double tourRevenue = tourBookingRepository.sumTotalPriceByConfirmedStatusAndCreatedAtAfter(startDate);
        Double hotelRevenue = hotelBookingRepository.sumTotalPriceByConfirmedStatusAndCreatedAtAfter(startDate);

        double totalRevenue = 0.0;
        if (tourRevenue != null) totalRevenue += tourRevenue;
        if (hotelRevenue != null) totalRevenue += hotelRevenue;

        long tourBookingsCount = tourBookingRepository.countByCreatedAtAfter(startDate);
        long hotelBookingsCount = hotelBookingRepository.countByCreatedAtAfter(startDate);

        long totalBookings = tourBookingsCount + hotelBookingsCount;

        long newUsersCount = userRepository.countByCreatedAtAfter(startDate);

        long totalUsers = userRepository.count();

        long completedTourBookings = tourBookingRepository.countByBookingStatusAndCreatedAtAfter("confirmed", startDate);
        long completedHotelBookings = hotelBookingRepository.countByBookingStatusAndCreatedAtAfter("confirmed", startDate);

        long completedBookings = completedTourBookings + completedHotelBookings;

        double completionRate = 0.0;
        if (totalBookings > 0) {
            completionRate = ((double) totalBookings / totalUsers) * 100;
        }

        double avgBookingValue = 0.0;
        if (totalBookings > 0 && totalRevenue > 0) {
            avgBookingValue = totalRevenue / totalBookings;
        }

        double roundedCompletionRate = Math.round(completionRate * 100.0) / 100.0;
        double roundedAvgBookingValue = Math.round(avgBookingValue * 100.0) / 100.0;

        return new DashboardStatsResponse(
                totalRevenue,
                tourBookingsCount,
                hotelBookingsCount,
                totalBookings,
                newUsersCount,
                totalUsers,
                roundedCompletionRate,
                roundedAvgBookingValue
        );
    }

    public List<RevenueChartResponse> getRevenueChart(int periodDays) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(periodDays);

        List<RevenueAggregationResult> tourDailyRevenue = tourBookingRepository.getDailyRevenue(startDate);
        List<RevenueAggregationResult> hotelDailyRevenue = hotelBookingRepository.getDailyRevenue(startDate);

        Map<String, RevenueChartResponse> chartData = new HashMap<>();

        if (tourDailyRevenue != null) {
            for (RevenueAggregationResult item: tourDailyRevenue) {
                String dateKey = item.get_id().toDateString();
                chartData.put(dateKey, RevenueChartResponse.builder()
                        .date(dateKey)
                        .tourRevenue(item.getRevenue())
                        .tourBookings(item.getBookings())
                        .hotelRevenue(0.0)
                        .hotelBookings(0)
                        .build()
                );
            }
        }

        if (hotelDailyRevenue != null) {
            for (RevenueAggregationResult item: hotelDailyRevenue) {
                String dateKey = item.get_id().toDateString();
                if (chartData.containsKey(dateKey)) {
                    RevenueChartResponse existing = chartData.get(dateKey);
                    existing.setHotelRevenue(item.getRevenue());
                    existing.setHotelBookings(item.getBookings());
                } else {
                    chartData.put(dateKey, RevenueChartResponse.builder()
                            .date(dateKey)
                            .tourRevenue(0.0)
                            .tourBookings(0)
                            .hotelRevenue(item.getRevenue())
                            .hotelBookings(item.getBookings())
                            .build()
                    );
                }
            }
        }

        return chartData.values().stream()
                .sorted(Comparator.comparing(RevenueChartResponse::getDate))
                .collect(Collectors.toList());
    }

    public List<TopTourResponse> getTopTours(int limit) {
        int validatedLimit = Math.min(Math.max(limit, 1), 50);

        System.out.println(validatedLimit);
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group("tourId")
                        .sum("totalPrice").as("totalRevenue")
                        .count().as("totalBookings"),
                Aggregation.project()
                        .and("_id").as("tourId")
                        .and("totalRevenue").as("totalRevenue")
                        .and("totalBookings").as("totalBookings")
                        .and(ConvertOperators.ToObjectId.toObjectId("$_id")).as("tourIdObj"),
                Aggregation.lookup("tours", "tourIdObj", "_id", "tourDetails"),
                Aggregation.unwind("tourDetails", true),
                Aggregation.project()
                        .and("tourId").as("_id")
                        .and("tourDetails.name").as("name")
                        .and("tourDetails.location").as("location")
                        .and("tourDetails.images").as("images")
                        .and("totalBookings").as("totalBookings")
                        .and("totalRevenue").as("totalRevenue"),
                Aggregation.sort(Sort.Direction.DESC, "totalBookings"),
                Aggregation.limit(validatedLimit)
        );

        AggregationResults<TopTourResponse> results = mongoTemplate.aggregate(
                aggregation,
                "tourbookings",
                TopTourResponse.class
        );

        return results.getMappedResults();
    }

    public List<TopHotelResponse> getTopHotels(int limit) {
        int validatedLimit = Math.min(Math.max(limit, 1), 50);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group("hotelId")
                        .sum("totalPrice").as("totalRevenue")
                        .count().as("totalBookings"),
                Aggregation.project()
                        .and("_id").as("hotelId")
                        .and("totalRevenue").as("totalRevenue")
                        .and("totalBookings").as("totalBookings")
                        .and(ConvertOperators.ToObjectId.toObjectId("$_id")).as("hotelIdObj"),
                Aggregation.lookup("hotels", "hotelIdObj", "_id", "hotelDetails"),
                Aggregation.unwind("hotelDetails"),
                Aggregation.project()
                        .and("hotelId").as("_id")
                        .and("hotelDetails.name").as("name")
                        .and("hotelDetails.address").as("address")
                        .and("hotelDetails.img").as("img")
                        .and("totalBookings").as("totalBookings")
                        .and("totalRevenue").as("totalRevenue"),
                Aggregation.sort(Sort.Direction.DESC, "totalBookings"),
                Aggregation.limit(validatedLimit)
        );

        AggregationResults<TopHotelResponse> results = mongoTemplate.aggregate(
                aggregation,
                "hotelbookings",
                TopHotelResponse.class
        );

        return results.getMappedResults();
    }

    public List<RecentBookingResponse> getRecentBookings(int limit) {
        int validatedLimit = Math.min(Math.max(limit, 1), 50);

        Pageable pageable = PageRequest.of(0, validatedLimit, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<TourBooking> tourBookings = tourBookingRepository.findRecentBookings(pageable);
        List<HotelBooking> hoteBookings = hotelBookingRepository.findRecentBookings(pageable);

        List<RecentBookingResponse> tourBookingResponses = convertTourBookings(tourBookings);
        List<RecentBookingResponse> hotelBookingResponses = convertHotelBookings(hoteBookings);

        List<RecentBookingResponse> allBookings = new ArrayList<>();
        allBookings.addAll(tourBookingResponses);
        allBookings.addAll((hotelBookingResponses));

        return allBookings.stream()
                .sorted(Comparator.comparing(RecentBookingResponse::getCreatedAt).reversed())
                .limit(validatedLimit)
                .collect(Collectors.toList());
    }

    private List<RecentBookingResponse> convertTourBookings (List<TourBooking> tourBookings) {
        return tourBookings.stream()
                .map(booking -> {
                    User user = null;
                    if (booking.getUserId() != null) {
                        user = userRepository.findById(booking.getUserId()).orElse(null);
                    }

                    Tour tour = null;
                    if (booking.getTourId() != null) {
                        tour = tourRepository.findById(booking.getTourId()).orElse(null);
                    }

                    String customerName;
                    if (user != null) {
                        customerName = String.format("%s %s",
                                user.getFirstName() != null ? user.getFirstName() : "",
                                user.getLastName() != null ? user.getLastName() : ""
                        ).trim();
                    } else {
                        customerName = "";
                    }

                    String customerEmail;
                    if (user != null) {
                        customerEmail = user.getEmail();
                    } else {
                        customerEmail = "";
                    }
                    return new RecentBookingResponse(
                            booking.get_id(),
                            "tour",
                            customerName,
                            customerEmail,
                            tour != null ? tour.getName() : "",
                            tour != null ? tour.getLocation() : "",
                            booking.getTotalPrice(),
                            booking.getBookingStatus(),
                            booking.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());
    }

    private List<RecentBookingResponse> convertHotelBookings (List<HotelBooking> hotelBookings) {
        return hotelBookings.stream()
                .map(booking -> {
                    User user = null;
                    if (booking.getUserId() != null) {
                        user = userRepository.findById(booking.getUserId()).orElse(null);
                    }

                    Hotel hotel = null;
                    if (booking.getHotelId() != null) {
                        hotel = hotelRepository.findById(booking.getHotelId()).orElse(null);
                    }

                    String customerName;
                    if (user != null) {
                        customerName = String.format("%s %s",
                                user.getFirstName() != null ? user.getFirstName() : "",
                                user.getLastName() != null ? user.getLastName() : ""
                        ).trim();
                    } else {
                        customerName = "";
                    }

                    String customerEmail;
                    if (user != null) {
                        customerEmail = user.getEmail();
                    } else {
                        customerEmail = "";
                    }
                    return new RecentBookingResponse(
                            booking.get_id(),
                            "hotel",
                            customerName,
                            customerEmail,
                            hotel != null ? hotel.getName() : "",
                            hotel != null ? hotel.getAddress() : "",
                            booking.getTotalPrice(),
                            booking.getBookingStatus(),
                            booking.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());
    }

    public List<TopCustomerResponse> getTopCustomers(int limit) {
        List<TopCustomerResponse> results = new ArrayList<>();

        Aggregation tourAggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("userId").ne(null)),
                Aggregation.group("userId")
                        .sum("totalPrice").as("tourSpent")
                        .count().as("tourBookings"),
                Aggregation.project()
                        .and("_id").as("userId")
                        .and("tourSpent").as("tourSpent")
                        .and("tourBookings").as("tourBookings")
        );

        Aggregation hotelAggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("userId").ne(null)),
                Aggregation.group("userId")
                        .sum("totalPrice").as("hotelSpent")
                        .count().as("hotelBookings"),
                Aggregation.project()
                        .and("_id").as("userId")
                        .and("hotelSpent").as("hotelSpent")
                        .and("hotelBookings").as("hotelBookings")
        );

        Map<String, CustomerData> customerMap = new HashMap<>();

        AggregationResults<Map> tourResults = mongoTemplate.aggregate(
                tourAggregation, "tourbookings", Map.class);

        AggregationResults<Map> hotelResults = mongoTemplate.aggregate(hotelAggregation, "hotelbookings", Map.class);

        for (Map result: tourResults.getMappedResults()) {
            String userId = (String) result.get("userId");
            if (userId != null) {
                double tourSpent = result.get("tourSpent") != null ?
                        ((Number) result.get("tourSpent")).doubleValue() : 0;
                int tourBookings = result.get("tourBookings") != null ?
                        ((Number) result.get("tourBookings")).intValue() : 0;

                customerMap.put(userId, new CustomerData(userId, tourBookings, tourSpent));
            }
        }

        for (Map result : hotelResults.getMappedResults()) {
            String userId = (String) result.get("userId");
            if (userId != null) {
                double hotelSpent = result.get("hotelSpent") != null ?
                        ((Number) result.get("hotelSpent")).doubleValue() : 0;
                int hotelBookings = result.get("hotelBookings") != null ?
                        ((Number) result.get("hotelBookings")).intValue() : 0;

                CustomerData existing = customerMap.get("userId");
                if (existing != null) {
                    existing.addBookings(hotelBookings);
                    existing.addSpent(hotelSpent);
                } else {
                    customerMap.put(userId, new CustomerData(userId, hotelBookings, hotelSpent));
                }
            }
        }

        // Nếu không có dữ liệu, trả về list rỗng
        if (customerMap.isEmpty()) {
            return results;
        }

        List<CustomerData> topCustomers = customerMap.values().stream()
                .sorted((a, b) -> Double.compare(b.getTotalSpent(), a.getTotalSpent()))
                .limit(limit)
                .toList();

        List<String> userIds = topCustomers.stream().map(CustomerData::getUserId).toList();
        Map<String, User> userMap = new HashMap<>();
        List<User> users = userRepository.findAllById(userIds);
        for (User user: users) {
            if (user != null) {
                userMap.put(user.get_id(), user);
            }
        }

        for (CustomerData customerData : topCustomers) {
            User user = userMap.get(customerData.getUserId());

            if (user != null) {
                TopCustomerResponse dto = new TopCustomerResponse();
                dto.setUserId(customerData.getUserId());
                dto.setFirstName(user.getFirstName() != null ? user.getFirstName() : "");
                dto.setLastName(user.getLastName() != null ? user.getLastName() : "");
                dto.setEmail(user.getEmail() != null ? user.getEmail() : "");
                dto.setAvatar(user.getAvatarUrl() != null ? user.getAvatarUrl() : "");
                dto.setTotalBookings(customerData.getTotalBookings());
                dto.setTotalSpent(Math.round(customerData.getTotalSpent() * 100.0) / 100.0); // Làm tròn 2 số
                results.add(dto);
            }
        }

        results.sort((a, b) -> Double.compare(b.getTotalSpent(), a.getTotalSpent()));

        // Giới hạn lại theo limit
        if (results.size() > limit) {
            results = results.subList(0, limit);
        }

        return results;
    }
}
