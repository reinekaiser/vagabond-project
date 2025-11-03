package com.ie207.vagabond.service;

import com.ie207.vagabond.model.Hotel;
import com.ie207.vagabond.model.HotelRoomType;
import com.ie207.vagabond.model.Room;
import com.ie207.vagabond.repository.HotelRepository;
import com.ie207.vagabond.repository.HotelRoomTypeRepository;
import com.ie207.vagabond.request.RoomRequest;
import com.ie207.vagabond.request.RoomTypeRequest;
import com.ie207.vagabond.response.HotelFilterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelService {
    private final HotelRepository hotelRepository;
    private final HotelRoomTypeRepository hotelRoomTypeRepository;
    private MongoTemplate mongoTemplate;
    private final CloudinaryService cloudinaryService;

//    get hotel
    public Hotel getHotelById(String hotelId) throws Exception {
        Optional<Hotel> hotel = hotelRepository.findById(hotelId);
        if (hotel.isEmpty())
            throw new Exception("Hotel not found");
        return hotel.get();
    }

    public List<HotelRoomType> listRoomTypesByHotel(String hotelId) throws Exception {
        Optional<Hotel> hotelOpt = hotelRepository.findById(hotelId);
        if (hotelOpt.isEmpty())
            throw new Exception("Hotel not found");

        Hotel hotel = hotelOpt.get();
        return hotel.getRoomTypes();
    }

    public HotelFilterResponse getHotels(
            String type, Double minPrice, Double maxPrice,
            List<String> hotelFacilities, List<String> roomFacilities,
            String key, String sort, int page, int limit
    ) {
        List<Hotel> hotels = hotelRepository.findAll();

        if (key != null && !key.isEmpty() && type != null) {
            switch (type.toLowerCase()) {
                case "city" -> {
                    hotels = hotels.stream()
                            .filter(h -> h.getCity() != null && h.getCity().getName() != null &&
                                    h.getCity().getName().equals(key))
                            .toList();
                }
                case "hotel" -> {
                    hotels = hotels.stream()
                            .filter(h -> h.getName() != null &&
                                    h.getName().contains(key))
                            .toList();
                }
            }
        }

        if (hotelFacilities != null && !hotelFacilities.isEmpty()) {
            hotels = hotels.stream()
                    .filter(h -> h.getServiceFacilities().stream()
                            .anyMatch(f -> hotelFacilities.contains(f.get_id()))
                    )
                    .collect(Collectors.toList());
        }

        if (roomFacilities != null && !roomFacilities.isEmpty()) {
            hotels = hotels.stream()
                    .filter(h -> h.getRoomTypes().stream()
                            .anyMatch(rt -> rt.getRoomFacilities().stream()
                                    .anyMatch(roomFacilities::contains)
                            )
                    )
                    .collect(Collectors.toList());
        }

        if (minPrice != null || maxPrice != null) {
            double min = minPrice != null ? minPrice : 0;
            double max = maxPrice != null ? maxPrice : Double.MAX_VALUE;

            hotels = hotels.stream()
                    .filter(h -> h.getRoomTypes().stream()
                            .anyMatch(rt -> rt.getRooms().stream()
                                    .anyMatch(r -> r.getPrice() >= min && r.getPrice() <= max)
                            )
                    )
                    .collect(Collectors.toList());
        }

        if (sort != null) {
            switch (sort) {
                case "priceAsc":
                    hotels.sort(Comparator.comparingDouble(this::getMinPrice));
                    break;
                case "priceDesc":
                    hotels.sort((a, b) -> Double.compare(getMinPrice(b), getMinPrice(a)));
                    break;
                case "newest":
                    hotels.sort(Comparator.comparing(Hotel::getCreatedAt).reversed());
                    break;
            }
        }

        int pageNumber = Math.max(page, 1);
        int pageSize = limit;
        int skip = (pageNumber - 1) * pageSize;
        int toIndex = Math.min(skip + pageSize, hotels.size());
        List<Hotel> paginated = skip >= hotels.size() ? new ArrayList<>() : hotels.subList(skip, toIndex);

        return new HotelFilterResponse(
                hotels.size(),
                (int) Math.ceil((double) hotels.size() / pageSize),
                pageNumber,
                pageSize,
                paginated
        );
    }

//    room
    @Transactional
    public HotelRoomType addRoom (String hotelId, String roomTypeId, RoomRequest addRoomRequest) throws Exception {
        if (addRoomRequest.getBedType().isEmpty() || addRoomRequest.getServeBreakfast().isEmpty() ||
                addRoomRequest.getMaxOfGuest() <= 0 || addRoomRequest.getNumberOfRoom() <= 0 ||
                addRoomRequest.getPrice() <=0
        ){
            throw new Exception("Invalid request");
        };

        HotelRoomType roomType = hotelRoomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new Exception("Hotel room type not found"));

        Room newRoom = new Room(
            addRoomRequest.getBedType(),
                addRoomRequest.getServeBreakfast(),
                addRoomRequest.getMaxOfGuest(),
                addRoomRequest.getNumberOfRoom(),
                addRoomRequest.getPrice(),
                addRoomRequest.getCancellationPolicy()
        );

        roomType.getRooms().add(newRoom);
        hotelRoomTypeRepository.save(roomType);
        return roomType;
    }

    @Transactional
    public HotelRoomType updateRoom (String hotelId, String roomTypeId, String roomId, RoomRequest updateRoomRequest) throws Exception {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new Exception("Hotel not found"));
        HotelRoomType hotelRoomType = hotelRoomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new Exception("Hotel room type not found"));
        Room room = hotelRoomType.getRooms().stream()
                .filter(r -> r.get_id().equals(roomId))
                .findFirst().orElseThrow(() -> new Exception("Room not found"));

        Optional.ofNullable(updateRoomRequest.getBedType()).ifPresent(room::setBedType);
        Optional.ofNullable(updateRoomRequest.getServeBreakfast()).ifPresent(room::setServeBreakfast);
        Optional.ofNullable(updateRoomRequest.getMaxOfGuest()).ifPresent(room::setMaxOfGuest);
        Optional.ofNullable(updateRoomRequest.getNumberOfRoom()).ifPresent(room::setNumberOfRoom);
        Optional.ofNullable(updateRoomRequest.getPrice()).ifPresent(room::setPrice);
        Optional.ofNullable(updateRoomRequest.getCancellationPolicy()).ifPresent(room::setCancellationPolicy);

        hotelRoomTypeRepository.save(hotelRoomType);
        return hotelRoomType;
    }

    @Transactional
    public String deleteRoom (String hotelId, String roomTypeId, String roomId) throws Exception {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new Exception("Hotel not found"));
        HotelRoomType roomType = hotelRoomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new Exception("Hotel room type not found"));
        Optional<Room> roomOpt = roomType.getRooms().stream()
                .filter(r -> r.get_id().equals(roomId))
                .findFirst();
        if (roomOpt.isEmpty()) {
            throw new Exception("Room not found");
        }

        roomType.getRooms().removeIf(r -> r.get_id().equals(roomId));
        hotelRoomTypeRepository.save(roomType);

        if (roomType.getRooms().isEmpty()){
            if (roomType.getImg() != null && !roomType.getImg().isEmpty()) {
                for (String publicId : roomType.getImg()) {
                    cloudinaryService.deleteImage(publicId);
                }
            }
            hotel.getRoomTypes().removeIf(rt -> rt.get_id().equals(roomTypeId));
            hotelRepository.save(hotel);
            hotelRoomTypeRepository.deleteById(roomTypeId);
        }
        boolean isHotelDeleted = checkAndDeleteHotel(hotelId);
        if (isHotelDeleted) {
            return "Xóa phòng, loại phòng và khách sạn (do không còn loại phòng nào)";
        }

        return "Xóa phòng thành công";
    }

    //roomtype
    @Transactional
    public HotelRoomType addRoomType (String hotelId, RoomTypeRequest roomType) throws Exception {
        if (roomType.getName() == null || roomType.getRooms() == null) {
            throw new Exception("Invalid request");
        }
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new Exception("Hotel not found"));

        HotelRoomType newRoomType = new HotelRoomType();
        newRoomType.setName(roomType.getName());
        newRoomType.setImg(roomType.getImg());
        newRoomType.setArea(roomType.getArea());
        newRoomType.setView(roomType.getView());
        newRoomType.setRoomFacilities(roomType.getRoomFacilities());

        List<Room> roomList = new ArrayList<>();
        for (Room room : roomType.getRooms()) {
            Room newRoom = new Room(
                    room.getBedType(),
                    room.getServeBreakfast(),
                    room.getMaxOfGuest(),
                    room.getNumberOfRoom(),
                    room.getPrice(),
                    room.getCancellationPolicy()
            );
            roomList.add(newRoom);
        }
        newRoomType.setRooms(roomList);
        hotelRoomTypeRepository.save(newRoomType);
        hotel.getRoomTypes().add(newRoomType);
        hotelRepository.save(hotel);

        return newRoomType;
    }

    @Transactional
    public HotelRoomType updateRoomType (String hotelId, String roomTypeId, RoomTypeRequest roomType) throws Exception {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new Exception("Hotel not found"));
        HotelRoomType updateRoomType = hotelRoomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new Exception("Hotel room type not found"));

        updateRoomType.setName(roomType.getName() != null ? roomType.getName() : updateRoomType.getName());
        updateRoomType.setImg(roomType.getImg() != null ? roomType.getImg() : updateRoomType.getImg());
        updateRoomType.setArea(roomType.getArea() != null ? roomType.getArea() : updateRoomType.getArea());
        updateRoomType.setView(roomType.getView() != null ? roomType.getView() : updateRoomType.getView());
        updateRoomType.setRoomFacilities(roomType.getRoomFacilities() != null
                ? roomType.getRoomFacilities()
                : updateRoomType.getRoomFacilities());
        hotelRoomTypeRepository.save(updateRoomType);

        return updateRoomType;
    }

    @Transactional
    public String deleteRoomType (String hotelId, String roomTypeId) throws Exception {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new Exception("Hotel not found"));
        HotelRoomType roomType = hotelRoomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new Exception("Hotel room type not found"));

        if (roomType.getImg() != null && !roomType.getImg().isEmpty()) {
            for (String publicId : roomType.getImg()) {
                try {
                    cloudinaryService.deleteImage(publicId);
                } catch (Exception e) {
                    System.err.println("Failed to delete image from Cloudinary: " + publicId);
                }
            }
        }

        if (hotel.getRoomTypes() != null && !hotel.getRoomTypes().isEmpty()) {
            hotel.getRoomTypes().removeIf(rt -> rt.get_id().equals(roomTypeId));
        }
        hotelRepository.save(hotel);
        hotelRoomTypeRepository.deleteById(roomTypeId);

        boolean isHotelDeleted = checkAndDeleteHotel(hotelId);
        if (isHotelDeleted) {
            return "Xóa loại phòng và khách sạn vì khách sạn không còn phòng";
        }
        return "Xóa loại phòng thành công";
    }

    public boolean checkAndDeleteHotel(String hotelId) throws IOException {
        Hotel hotel = hotelRepository.findById(hotelId).orElse(null);
        if (hotel != null && (hotel.getRoomTypes() == null || hotel.getRoomTypes().isEmpty())) {
            if (hotel.getImg() != null && !hotel.getImg().isEmpty()) {
                for (String publicId : hotel.getImg()) {
                    cloudinaryService.deleteImage(publicId);
                }
            }

            hotelRepository.deleteById(hotelId);
            return true;
        }
        return false;
    }

    private double getMinPrice(Hotel hotel) {
        return hotel.getRoomTypes().stream()
                .flatMap(rt -> rt.getRooms().stream())
                .mapToDouble(r -> r.getPrice())
                .min()
                .orElse(Double.MAX_VALUE);
    }
}
