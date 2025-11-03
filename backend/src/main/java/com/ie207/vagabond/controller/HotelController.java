package com.ie207.vagabond.controller;

import com.ie207.vagabond.model.Hotel;
import com.ie207.vagabond.model.HotelRoomType;
import com.ie207.vagabond.request.HotelRequest;
import com.ie207.vagabond.request.RoomRequest;
import com.ie207.vagabond.request.RoomTypeRequest;
import com.ie207.vagabond.response.ApiResponse;
import com.ie207.vagabond.response.HotelFilterResponse;
import com.ie207.vagabond.response.HotelSearchSuggestionResponse;
import com.ie207.vagabond.service.CityService;
import com.ie207.vagabond.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotel")
@RequiredArgsConstructor
public class HotelController {
    private final HotelService hotelService;
    private final CityService cityService;

    @GetMapping("/")
    public HotelFilterResponse getHotels(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) List<String> hotelFacilities,
            @RequestParam(required = false) List<String> roomFacilities,
            @RequestParam(required = false) String key,
            @RequestParam(required = false, defaultValue = "") String sort,
            @RequestParam(required = false, defaultValue = "2") int page,
            @RequestParam(required = false, defaultValue = "1") int limit
    ) {
        return hotelService.getHotels(type, minPrice, maxPrice, hotelFacilities, roomFacilities, key, sort, page, limit);
    }

    @GetMapping("/search")
    public ResponseEntity<HotelSearchSuggestionResponse> getHotelSuggestions(
            @RequestParam String key
    ) {
        return ResponseEntity.ok(cityService.getSearchHotelSuggestion(key));
    }

    @GetMapping("/{hotelId}/room-types")
    public ResponseEntity<List<HotelRoomType>> getRoomTypesByHotel(@PathVariable String hotelId) {
        try {
            List<HotelRoomType> roomTypes = hotelService.listRoomTypesByHotel(hotelId);
            return ResponseEntity.ok(roomTypes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(404).body(null);
        }
    }

    @GetMapping("/{hotelId}")
    public ResponseEntity<Hotel> getHotelById(@PathVariable String hotelId) {
        try {
            Hotel hotel = hotelService.getHotelById(hotelId);
            return ResponseEntity.ok(hotel);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    //room
    @PostMapping("/{hotelId}/room-types/{roomTypeId}/rooms")
    public ResponseEntity<HotelRoomType> addRoom (
            @PathVariable String hotelId,
            @PathVariable String roomTypeId,
            @RequestBody RoomRequest room
    ) {
        try {
            HotelRoomType roomType = hotelService.addRoom(hotelId, roomTypeId, room);
            return ResponseEntity.ok(roomType);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @PutMapping("/{hotelId}/room-types/{roomTypeId}/rooms/update/{roomId}")
    public ResponseEntity<HotelRoomType> updateRoom (
            @PathVariable String hotelId,
            @PathVariable String roomTypeId,
            @PathVariable String roomId,
            @RequestBody RoomRequest room
    ){
        try {
            HotelRoomType roomType = hotelService.updateRoom(hotelId, roomTypeId, roomId, room);
            return ResponseEntity.ok(roomType);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @DeleteMapping("/{hotelId}/room-types/{roomTypeId}/rooms/delete/{roomId}")
    public ResponseEntity<ApiResponse> deleteRoom(
            @PathVariable String hotelId,
            @PathVariable String roomTypeId,
            @PathVariable String roomId
    ) {
        try {
            String message = hotelService.deleteRoom(hotelId, roomTypeId, roomId);
            ApiResponse response = new ApiResponse(message, true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse errorResponse = new ApiResponse(e.getMessage(), false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }


    //roomtype
    @PostMapping("/{hotelId}/room-types")
    public ResponseEntity<HotelRoomType> addRoomType (
            @PathVariable String hotelId,
            @RequestBody RoomTypeRequest roomType
    ) {
        try {
            HotelRoomType newRoomType = hotelService.addRoomType(hotelId, roomType);
            return ResponseEntity.ok(newRoomType);
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @PutMapping("/{hotelId}/room-types/update/{roomTypeId}")
    public ResponseEntity<HotelRoomType> updateRoomType (
            @PathVariable String hotelId,
            @PathVariable String roomTypeId,
            @RequestBody RoomTypeRequest roomType
    ) {
        try {
            HotelRoomType updatedRoomType = hotelService.updateRoomType(hotelId, roomTypeId, roomType);
            return ResponseEntity.ok(updatedRoomType);
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @DeleteMapping("/{hotelId}/room-types/delete/{roomTypeId}")
    public ResponseEntity<ApiResponse> deleteRoomType (
            @PathVariable String hotelId,
            @PathVariable String roomTypeId
    ) {
        try {
            String message = hotelService.deleteRoomType(hotelId, roomTypeId);
            return ResponseEntity.ok(new ApiResponse(message, true));
        } catch (Exception e){
            e.printStackTrace();
            ApiResponse errorResponse = new ApiResponse(e.getMessage(), false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    //hotel
    @PostMapping("/")
    public ResponseEntity<Hotel> addHotel (@RequestBody HotelRequest hotel) {
        try {
            Hotel newHotel = hotelService.addHotel(hotel);
            return ResponseEntity.ok(newHotel);
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @PutMapping("/update/{hotelId}")
    public ResponseEntity<Hotel> updateHotel (
            @PathVariable String hotelId, @RequestBody HotelRequest hotel
    ) {
        try {
            Hotel updatedHotel = hotelService.updateHotel(hotelId, hotel);
            return ResponseEntity.ok(updatedHotel);
        } catch (Exception e){
            e.printStackTrace();
            System.err.println(">>> [API] Update failed: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    @DeleteMapping("/delete/{hotelId}")
    public ResponseEntity<ApiResponse> deleteHotel (
            @PathVariable String hotelId
    ) {
        try {
            String message = hotelService.deleteHotel(hotelId);
            return ResponseEntity.ok(new ApiResponse(message, true));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
}
