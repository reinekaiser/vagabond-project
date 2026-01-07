package com.ie207.vagabond.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.ie207.vagabond.model.HotelBooking;
import com.ie207.vagabond.model.TourBooking;
import com.ie207.vagabond.repository.HotelBookingRepository;
import com.ie207.vagabond.repository.TourBookingRepository;
import com.ie207.vagabond.request.HotelBookingRequest;
import com.ie207.vagabond.request.HotelPaypalOrderRequest;
import com.ie207.vagabond.request.TourBookingRequest;
import com.ie207.vagabond.request.TourPayPalOrderRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class PaypalService {

    @Value("${paypal.client-id}")
    private String clientId;

    @Value("${paypal.client-secret}")
    private String clientSecret;

    private String CLIENT_URL = "http://localhost:5174";

    private final RestTemplate restTemplate = new RestTemplate();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final HotelBookingRepository hotelBookingRepository;
    private final TourBookingRepository tourBookingRepository;

    public String getAccessToken() {
        String auth = Base64.encodeBase64String((clientId + ":" + clientSecret).getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(auth);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> res = restTemplate.exchange(
                "https://api-m.sandbox.paypal.com/v1/oauth2/token",
                HttpMethod.POST,
                request,
                Map.class
        );

        return (String) res.getBody().get("access_token");
    }

    public String createOrder(Double amount, String returnUrl, String cancelUrl) {
        try {
            String accessToken = getAccessToken();
            if (accessToken == null) {
                throw new RuntimeException("Không thể lấy Access Token từ PayPal.");
            }
            BigDecimal usd = BigDecimal.valueOf(amount)
                    .divide(BigDecimal.valueOf(25911.5), 2, RoundingMode.HALF_UP);
            Map<String, Object> purchaseUnit = Map.of(
                    "amount", Map.of(
                            "currency_code", "USD",
                            "value", usd
                    )
            );

            Map<String, Object> payload = Map.of(
                    "intent", "CAPTURE",
                    "purchase_units", List.of(purchaseUnit),
                    "application_context", Map.of(
                            "return_url", returnUrl,
                            "cancel_url", cancelUrl
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(
                    "https://api-m.sandbox.paypal.com/v2/checkout/orders",
                    HttpMethod.POST,
                    entity,
                    JsonNode.class
            );
            JsonNode responseBody = responseEntity.getBody();
            if (responseBody == null || !responseBody.has("links")) {
                throw new RuntimeException("Không nhận được links từ PayPal");
            }
            for (JsonNode link : responseBody.get("links")) {
                if ("approve".equals(link.path("rel").asText())) {
                    return link.path("href").asText();
                }
            }
            return null;
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> captureOrder(String orderID) {
        String accessToken = getAccessToken();
        if (accessToken == null) {
            throw new RuntimeException("Không thể lấy Access Token từ PayPal.");
        }

        String url = "https://api-m.sandbox.paypal.com/v2/checkout/orders/" + orderID + "/capture";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<Map> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
        );
        return responseEntity.getBody();
    }

    @Transactional
    public String createHotelPaypalOrder(HotelPaypalOrderRequest request) {
        if (request.getAmount() == null || request.getAmount() < 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        String cancelUrl = UriComponentsBuilder
                .fromHttpUrl(CLIENT_URL + "/hotels/" + request.getHotelId())
                .queryParam("location", request.getLocation())
                .queryParam("checkIn", request.getCheckIn())
                .queryParam("checkOut", request.getCheckOut())
                .queryParam("rooms", request.getRooms().toString())
                .queryParam("adults", request.getAdults().toString())
                .queryParam("roomTypeId", request.getRoomTypeId())
                .queryParam("roomId", request.getRoomId())
                .toUriString();

        String returnUrl = CLIENT_URL + "/hotel-checkout-success";
        String approvalUrl = createOrder(request.getAmount(), returnUrl, cancelUrl);
        return approvalUrl;
    }

    public HotelBooking captureHotelPaypalOrder(HotelBookingRequest request) {
        Map<String, Object> captureData = captureOrder(request.getOrderID());

        if (captureData == null || !captureData.containsKey("status")) {
            throw new RuntimeException("Không nhận được trạng thái từ PayPal");
        }

        LocalDate checkin = LocalDate.parse(request.getCheckin(), formatter);
        LocalDate checkout = LocalDate.parse(request.getCheckout(), formatter);

        HotelBooking hotelBooking = new HotelBooking();
        hotelBooking.setUserId(request.getUserId());
        hotelBooking.setHotelId(request.getHotelId());
        hotelBooking.setRoomTypeId(request.getRoomTypeId());
        hotelBooking.setRoomId(request.getRoomId());

        hotelBooking.setName(request.getName());
        hotelBooking.setEmail(request.getEmail());
        hotelBooking.setPhone(request.getPhone());

        hotelBooking.setCheckin(checkin);
        hotelBooking.setCheckout(checkout);

        hotelBooking.setNumGuests(request.getNumGuests());
        hotelBooking.setNumRooms(request.getNumRooms());

        hotelBooking.setPaymentMethod(request.getPaymentMethod());
        hotelBooking.setTotalPrice(request.getTotalPrice());

        if ("COMPLETED".equals(captureData.get("status"))) {
            hotelBooking.setBookingStatus("pending");
        } else {
            hotelBooking.setBookingStatus("failed");
            hotelBookingRepository.save(hotelBooking);
            throw new RuntimeException("Thanh toán chưa hoàn tất");
        }

        hotelBookingRepository.save(hotelBooking);

        return hotelBooking;
    }

    @Transactional
    public String createTourPaypalOrder(TourPayPalOrderRequest request) {
        if (request.getAmount() == null || request.getAmount() < 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        String cancelUrl = UriComponentsBuilder
                .fromHttpUrl(CLIENT_URL + "/tour/" + request.getTourId())
                .toUriString();

        String returnUrl = CLIENT_URL + "/tour-checkout-success";
        String approvalUrl = createOrder(request.getAmount(), returnUrl, cancelUrl);
        return approvalUrl;
    }

    @Transactional
    public TourBooking captureTourPaypalOrder(TourBookingRequest request) {
        Map<String, Object> captureData = captureOrder(request.getOrderID());

        if (captureData == null || !captureData.containsKey("status")) {
            throw new RuntimeException("Không nhận được trạng thái từ PayPal");
        }

        LocalDate useDate = Instant.parse(request.getUseDate())
                .atZone(ZoneId.systemDefault()).toLocalDate();

        TourBooking tourBooking = new TourBooking();
        tourBooking.setUserId(request.getUserId());
        tourBooking.setTourId(request.getTourId());
        tourBooking.setTicketId(request.getTicketId());

        tourBooking.setTourImg(request.getTourImg());
        tourBooking.setName(request.getName());
        tourBooking.setEmail(request.getEmail());
        tourBooking.setPhone(request.getPhone());
        tourBooking.setUseDate(useDate);
        tourBooking.setPaymentMethod(request.getPaymentMethod());
        tourBooking.setTotalPrice(request.getTotalPrice());

        if ("COMPLETED".equals(captureData.get("status"))) {
            tourBooking.setBookingStatus("pending");
        } else {
            tourBooking.setBookingStatus("failed");
            tourBookingRepository.save(tourBooking);
            throw new RuntimeException("Thanh toán chưa hoàn tất");
        }

        tourBookingRepository.save(tourBooking);

        return tourBooking;
    }

}
