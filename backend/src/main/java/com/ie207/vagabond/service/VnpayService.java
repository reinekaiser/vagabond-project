package com.ie207.vagabond.service;

import com.ie207.vagabond.model.Hotel;
import com.ie207.vagabond.model.HotelBooking;
import com.ie207.vagabond.model.Tour;
import com.ie207.vagabond.model.TourBooking;
import com.ie207.vagabond.repository.HotelBookingRepository;
import com.ie207.vagabond.repository.HotelRepository;
import com.ie207.vagabond.repository.TourBookingRepository;
import com.ie207.vagabond.request.HotelBookingRequest;
import com.ie207.vagabond.request.HotelPaypalOrderRequest;
import com.ie207.vagabond.request.TourBookingRequest;
import com.ie207.vagabond.request.TourPayPalOrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VnpayService {

    @Value("${vnpay.tmn-code}")
    private String tmnCode;

    @Value("${vnpay.hash-secret}")
    private String hashSecret;

    private String payUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private String CLIENT_URL = "http://localhost:5174";

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final HotelBookingRepository hotelBookingRepository;
    private final HotelRepository hotelRepository;
    private final TourBookingRepository tourBookingRepository;

    public static String hashAllFields(Map<String, String> fields, String secretKey) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames); // sắp xếp theo ASCII
        StringBuilder hashData = new StringBuilder();
        for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext(); ) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName)
                        .append("=")
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    hashData.append("&");
                }
            }
        }
        return hmacSHA512(secretKey, hashData.toString());
    }

    public static String hmacSHA512(final String key, final String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    @Transactional
    public String createHotelVnpayOrder(HotelPaypalOrderRequest req) {
        long vnpAmount = Math.round(req.getAmount() * 100);
        System.out.println(String.valueOf(vnpAmount));
        String returnUrl = CLIENT_URL + "/hotel-checkout-success";

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", String.valueOf(vnpAmount));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", UUID.randomUUID().toString());
        params.put("vnp_OrderInfo", "Thanh toan cho booking " + req.getHotelId());
        params.put("vnp_OrderType", "hotel");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", "127.0.0.1");
        params.put("vnp_CreateDate",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        String vnpSecureHash = hashAllFields(params, hashSecret);
        params.put("vnp_SecureHash", vnpSecureHash);

        StringBuilder url = new StringBuilder(payUrl + "?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            url.append(entry.getKey())
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                    .append("&");
        }
        url.deleteCharAt(url.length() - 1);

        return url.toString();
    }

    @Transactional
    public HotelBooking captureHotelVnpayOrder(HotelBookingRequest request) {
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

        hotelBooking.setBookingStatus("pending");
        hotelBookingRepository.save(hotelBooking);

        return hotelBooking;
    }

    @Transactional
    public String createCancelUrl(HotelBookingRequest request){
        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new RuntimeException("Hotel not found"));
        String location = hotel.getCity().getName();

        String cancelUrl = UriComponentsBuilder
                .fromHttpUrl(CLIENT_URL + "/hotels/" + request.getHotelId())
                .queryParam("location", location)
                .queryParam("checkIn", request.getCheckin())
                .queryParam("checkOut", request.getCheckout())
                .queryParam("rooms", request.getNumRooms())
                .queryParam("adults", request.getNumGuests())
                .queryParam("roomTypeId", request.getRoomTypeId())
                .queryParam("roomId", request.getRoomId())
                .toUriString();

        return cancelUrl;
    }

    @Transactional
    public String createTourVnpayOrder(TourPayPalOrderRequest req){
        long vnpAmount = Math.round(req.getAmount() * 100);
        String returnUrl = CLIENT_URL + "/tour-checkout-success";

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", String.valueOf(vnpAmount));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", UUID.randomUUID().toString());
        params.put("vnp_OrderInfo", "Thanh toan cho booking " + req.getTourId());
        params.put("vnp_OrderType", "tour");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", "127.0.0.1");
        params.put("vnp_CreateDate",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        String vnpSecureHash = hashAllFields(params, hashSecret);
        params.put("vnp_SecureHash", vnpSecureHash);

        StringBuilder url = new StringBuilder(payUrl + "?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            url.append(entry.getKey())
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                    .append("&");
        }
        url.deleteCharAt(url.length() - 1);

        return url.toString();
    }

    @Transactional
    public TourBooking captureTourVnpayOrder(TourBookingRequest request){
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

        tourBooking.setBookingStatus("pending");

        tourBookingRepository.save(tourBooking);
        return tourBooking;
    }

    public String createTourCancelUrl(TourBookingRequest request){
        String cancelUrl = UriComponentsBuilder
                .fromHttpUrl(CLIENT_URL + "/tour/" + request.getTourId())
                .toUriString();

        return cancelUrl;
    }
}

