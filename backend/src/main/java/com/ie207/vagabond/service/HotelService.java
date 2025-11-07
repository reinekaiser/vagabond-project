package com.ie207.vagabond.service;

import com.ie207.vagabond.model.*;
import com.ie207.vagabond.repository.CityRepository;
import com.ie207.vagabond.repository.HotelFacilityRepository;
import com.ie207.vagabond.repository.HotelRepository;
import com.ie207.vagabond.repository.HotelRoomTypeRepository;
import com.ie207.vagabond.request.HotelRequest;
import com.ie207.vagabond.request.RoomRequest;
import com.ie207.vagabond.request.RoomTypeRequest;
import com.ie207.vagabond.response.HotelFilterResponse;
import com.ie207.vagabond.response.HotelSearchSuggestionResponse;
import com.mongodb.DBRef;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelService {
    private final HotelRepository hotelRepository;
    private final HotelRoomTypeRepository hotelRoomTypeRepository;
    private final MongoTemplate mongoTemplate;
    private final CloudinaryService cloudinaryService;
    private final CityRepository cityRepository;
    private final HotelFacilityRepository hotelFacilityRepository;

    //    search & filter hotel
    public List<City> searchCities (String keyword) {
        Pattern pattern = Pattern.compile(keyword, Pattern.CASE_INSENSITIVE);
        Query query = new Query();
        query.addCriteria(Criteria.where("name").regex(pattern));
        query.fields().include("name");
        List<City> cities = mongoTemplate.find(query, City.class);
        return cities;
    }

    public List<Hotel> searchHotels(String keyword) {
        List<Document> pipeline = Arrays.asList(
                new Document("$search", new Document()
                        .append("index", "search_hotel")
                        .append("text", new Document()
                                .append("query", keyword)
                                .append("path", Arrays.asList("name", "address", "description"))
                                .append("fuzzy", new Document())
                        )
                ),
                new Document("$limit", 3),
                new Document("$project", new Document()
                        .append("name", 1)
                        .append("address", 1)
                        .append("city", 1)
                )
        );

        List<Document> results = mongoTemplate
                .getCollection("hotels")
                .aggregate(pipeline)
                .into(new ArrayList<>());

        List<Hotel> hotels = new ArrayList<>();
        for (Document doc : results) {
            Hotel hotel = new Hotel();
            hotel.set_id(doc.getObjectId("_id").toString());
            hotel.setName(doc.getString("name"));
            hotel.setAddress(doc.getString("address"));

            DBRef cityRef = (DBRef) doc.get("city");
            if (cityRef != null) {
                City city = mongoTemplate.findById(cityRef.getId(), City.class);
                hotel.setCity(city);
            }
            hotels.add(hotel);
        }
        return hotels;
    }

    @Transactional
    public HotelSearchSuggestionResponse getHotelSearchSuggestion (String keyword) {
        List<City> cities = searchCities(keyword);
        List<Hotel> hotels = searchHotels(keyword);
        HotelSearchSuggestionResponse response = new HotelSearchSuggestionResponse();
        response.setCities(cities);
        response.setHotels(hotels);
        return response;
    }

    @Transactional
    public HotelFilterResponse getSearchResults (
            String type, Double minPrice, Double maxPrice,
            List<String> hotelFacilities, List<String> roomFacilities,
            String key, String sort, int page, int limit
    ) {
        List<Document> pipeline = new ArrayList<>();

        if (key != null && !key.isEmpty() && type != null) {
            switch (type) {
                case "hotel":
                    pipeline.add(
                            new Document("$match", new Document()
                                    .append("name", key)
                            )
                    );
                    break;
                case "city":
                    City city = mongoTemplate.findOne(
                            Query.query(Criteria.where("name").is(key)),
                            City.class
                    );
                    if (city != null) {
                        pipeline.add(new Document("$match",
                                new Document("city.$id", new ObjectId(city.get_id()))
                        ));
                    }
                    break;
            };
        }

        List<Document> orConditions = new ArrayList<>();

        if (hotelFacilities != null && !hotelFacilities.isEmpty()) {
            List<ObjectId> facilityIds = hotelFacilities.stream()
                    .map(ObjectId::new)
                    .collect(Collectors.toList());
            orConditions.add(
                    new Document("serviceFacilities.$id",
                            new Document("$in", facilityIds))
            );
        }

        if (roomFacilities != null && !roomFacilities.isEmpty()) {
            List<ObjectId> matchedRoomTypeIds = mongoTemplate
                    .getCollection("hotelroomtypes")
                    .find(new Document("roomFacilities", new Document("$in", roomFacilities)))
                    .map(doc -> doc.getObjectId("_id"))
                    .into(new ArrayList<>());
            orConditions.add(
                    new Document("roomTypes.$id", new Document("$in", matchedRoomTypeIds))
            );
        }

        if (!orConditions.isEmpty()) {
            pipeline.add(new Document("$match", new Document("$or", orConditions)));
        }

        if (minPrice != null || maxPrice != null) {
            double min = minPrice != null ? minPrice : 0;
            double max = maxPrice != null ? maxPrice : Double.MAX_VALUE;
            List<ObjectId> matchedRoomTypeIds = mongoTemplate
                    .getCollection("hotelroomtypes")
                    .find(new Document("rooms.price",
                            new Document("$gte", min).append("$lte", max)))
                    .map(doc -> doc.getObjectId("_id"))
                    .into(new ArrayList<>());
            pipeline.add(new Document("$match",
                    new Document("roomTypes.$id", new Document("$in", matchedRoomTypeIds))
            ));
        }

        if (sort != null) {
            switch (sort){
                case "newest":
                    pipeline.add(new Document("$sort", new Document("createdAt", -1)));
                    break;
                default:
                    pipeline.add(new Document("$sort", new Document("createdAt", 1)));
                    break;
            }
        }

        int currentPage = Math.max(page, 1);
        int skip = (currentPage - 1) * limit;

        pipeline.add(new Document("$facet", new Document()
                .append("metadata", Arrays.asList(
                        new Document("$count", "total")
                ))
                .append("data", Arrays.asList(
                        new Document("$skip", skip),
                        new Document("$limit", limit)
                ))
        ));

        List<Document> aggregated = mongoTemplate
                .getCollection("hotels")
                .aggregate(pipeline)
                .into(new ArrayList<>());

        List<Hotel> hotels = new ArrayList<>();
        long totalHotels = 0;

        if (!aggregated.isEmpty()) {
            Document doc = aggregated.get(0);
            List<Document> metadata = (List<Document>) doc.get("metadata");
            if (!metadata.isEmpty()) {
                totalHotels = metadata.get(0).getInteger("total", 0);
            }
            List<Document> data = (List<Document>) doc.get("data");
            for (Document d : data) {
                hotels.add(mapDocumentToHotel(d));
            }
        }

        int totalPages = (int) Math.ceil((double) totalHotels / limit);

        return new HotelFilterResponse(
                (int) totalHotels,
                totalPages,
                currentPage,
                limit,
                hotels
        );
    }

    private Hotel mapDocumentToHotel(Document doc) {
        Hotel hotel = new Hotel();

        hotel.set_id(doc.getObjectId("_id").toString());
        hotel.setName(doc.getString("name"));
        hotel.setAddress(doc.getString("address"));
        hotel.setLat(doc.getDouble("lat") != null ? doc.getDouble("lat") : 0.0);
        hotel.setLng(doc.getDouble("lng") != null ? doc.getDouble("lng") : 0.0);
        hotel.setDescription(doc.getString("description"));
        hotel.setAverageRating(doc.getDouble("averageRating") != null ? doc.getDouble("averageRating") : 0);
        hotel.setRooms(doc.getInteger("rooms") != null ? doc.getInteger("rooms") : 0);
        hotel.setFromPrice(doc.getDouble("fromPrice") != null ? doc.getDouble("fromPrice") : 0.0);
        hotel.setCreatedAt(doc.getDate("createdAt") != null ? doc.getDate("createdAt") : null);
        hotel.setUpdatedAt(doc.getDate("updatedAt") != null ? doc.getDate("updatedAt") : null);

        List<String> imgList = (List<String>) doc.get("img");
        if (imgList != null) hotel.setImg(imgList);

        DBRef cityRef = (DBRef) doc.get("city");
        if (cityRef != null) {
            City city = mongoTemplate.findById(cityRef.getId(), City.class);
            hotel.setCity(city);
        }

        List<DBRef> facilityRefs = (List<DBRef>) doc.get("serviceFacilities");
        if (facilityRefs != null) {
            List<HotelFacility> facilities = new ArrayList<>();
            for (DBRef ref : facilityRefs) {
                HotelFacility facility = mongoTemplate.findById(ref.getId(), HotelFacility.class);
                if (facility != null) facilities.add(facility);
            }
            hotel.setServiceFacilities(facilities);
        }

//        List<DBRef> roomTypeRefs = (List<DBRef>) doc.get("roomTypes");
//        if (roomTypeRefs != null) {
//            List<HotelRoomType> roomTypes = new ArrayList<>();
//            for (DBRef ref : roomTypeRefs) {
//                HotelRoomType roomType = mongoTemplate.findById(ref.getId(), HotelRoomType.class);
//                if (roomType != null) roomTypes.add(roomType);
//            }
//            hotel.setRoomTypes(roomTypes);
//        }

        return hotel;
    }



    //get hotel
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

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new Exception("Hotel not found"));

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

        if (addRoomRequest.getPrice() < hotel.getFromPrice()) {
            hotel.setFromPrice(addRoomRequest.getPrice());
            hotelRepository.save(hotel);
        }

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

        if (updateRoomRequest.getPrice() < hotel.getFromPrice()) {
            hotel.setFromPrice(updateRoomRequest.getPrice());
            hotelRepository.save(hotel);
        }
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

        Room deletedRoom = roomOpt.get();
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

        if (deletedRoom.getPrice() <= hotel.getFromPrice()) {
            double newMinPrice = hotel.getRoomTypes().stream()
                    .flatMap(rt -> rt.getRooms().stream())
                    .mapToDouble(Room::getPrice)
                    .min()
                    .orElse(0.0);
            hotel.setFromPrice(newMinPrice);
            hotelRepository.save(hotel);
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

        double minPrice = Double.MAX_VALUE;
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
            if (room.getPrice() < minPrice) {
                minPrice = room.getPrice();
            }
        }
        newRoomType.setRooms(roomList);
        hotelRoomTypeRepository.save(newRoomType);
        hotel.getRoomTypes().add(newRoomType);
        if (hotel.getFromPrice() == 0 || minPrice < hotel.getFromPrice()) {
            hotel.setFromPrice(minPrice);
        }
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

        double minPrice = Double.MAX_VALUE;
        for (HotelRoomType rt : hotel.getRoomTypes()) {
            if (rt.getRooms() != null && !rt.getRooms().isEmpty()) {
                for (Room room : rt.getRooms()) {
                    if (room.getPrice() < minPrice) {
                        minPrice = room.getPrice();
                    }
                }
            }
        }
        hotel.setFromPrice(minPrice == Double.MAX_VALUE ? 0.0 : minPrice);
        hotelRepository.save(hotel);
        return "Xóa loại phòng thành công";
    }

    //hotel
    @Transactional
    public Hotel addHotel (HotelRequest hotel) throws Exception {
        if (hotel.getName() == null || hotel.getName().isEmpty()
                || hotel.getAddress() == null || hotel.getAddress().isEmpty()
                || hotel.getCityId() == null || hotel.getCityId().isEmpty()
                || hotel.getDescription() == null || hotel.getDescription().isEmpty()
                || hotel.getRooms() <= 0
                || hotel.getRoomTypes() == null || hotel.getRoomTypes().isEmpty()) {
            throw new IllegalArgumentException("Thiếu thông tin bắt buộc hoặc chưa có loại phòng");
        }

        City city = cityRepository.findById(hotel.getCityId())
                .orElseThrow(() -> new Exception("City not found"));

        double minPrice = Double.MAX_VALUE;
        List<HotelRoomType> roomTypeList = new ArrayList<>();
        for (RoomTypeRequest roomTypeRequest : hotel.getRoomTypes()) {
            if (roomTypeRequest.getRooms() == null || roomTypeRequest.getRooms().isEmpty()) {
                throw new IllegalArgumentException("Loại phòng " + roomTypeRequest.getName() + " chưa có phòng nào");
            }
            HotelRoomType roomType = new HotelRoomType();
            roomType.setName(roomTypeRequest.getName());
            roomType.setArea(roomTypeRequest.getArea());
            roomType.setView(roomTypeRequest.getView());
            roomType.setRoomFacilities(roomTypeRequest.getRoomFacilities());
            roomType.setImg(roomTypeRequest.getImg());

            List<Room> roomList = new ArrayList<>();
            for (Room room : roomTypeRequest.getRooms()) {
                Room newRoom = new Room(
                        room.getBedType(),
                        room.getServeBreakfast(),
                        room.getMaxOfGuest(),
                        room.getNumberOfRoom(),
                        room.getPrice(),
                        room.getCancellationPolicy()
                );
                roomList.add(newRoom);
                if (room.getPrice() > 0 && room.getPrice() < minPrice) {
                    minPrice = room.getPrice();
                }
            }
            roomType.setRooms(roomList);
            hotelRoomTypeRepository.save(roomType);
            roomTypeList.add(roomType);
        }

        Hotel newHotel = new Hotel();
        newHotel.setName(hotel.getName());
        newHotel.setDescription(hotel.getDescription());
        newHotel.setAddress(hotel.getAddress());
        newHotel.setCity(city);
        newHotel.setLat(hotel.getLat());
        newHotel.setLng(hotel.getLng());
        newHotel.setRooms(hotel.getRooms());
        newHotel.setPolicies(hotel.getPolicies());
        newHotel.setImg(hotel.getImg());
        newHotel.setRoomTypes(roomTypeList);
        newHotel.setFromPrice(minPrice);

        if (hotel.getServiceFacilities() != null && !hotel.getServiceFacilities().isEmpty()) {
            List<HotelFacility> facilities = hotelFacilityRepository.findAllById(hotel.getServiceFacilities());
            newHotel.setServiceFacilities(facilities);
        }
        hotelRepository.save(newHotel);

        return newHotel;
    };

    @Transactional
    public Hotel updateHotel (String hotelId, HotelRequest hotel) throws Exception {
        Hotel updateHotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new Exception("Hotel not found"));

        if (hotel.getCityId() != null || !hotel.getCityId().isEmpty()){
            City city = cityRepository.findById(hotel.getCityId())
                    .orElseThrow(() -> new Exception("City not found"));
            updateHotel.setCity(city);
        }

        if (hotel.getName() != null && !hotel.getName().isEmpty()) updateHotel.setName(hotel.getName());
        if (hotel.getDescription() != null && !hotel.getDescription().isEmpty()) updateHotel.setDescription(hotel.getDescription());
        if (hotel.getAddress() != null && !hotel.getAddress().isEmpty()) updateHotel.setAddress(hotel.getAddress());
        if (hotel.getImg() != null && !hotel.getImg().isEmpty()) updateHotel.setImg(hotel.getImg());
        if (hotel.getRooms() > 0) updateHotel.setRooms(hotel.getRooms());
        if (hotel.getLat() != 0) updateHotel.setLat(hotel.getLat());
        if (hotel.getLng() != 0) updateHotel.setLng(hotel.getLng());
        if (hotel.getPolicies() != null) updateHotel.setPolicies(hotel.getPolicies());

        if (hotel.getServiceFacilities() != null && !hotel.getServiceFacilities().isEmpty()) {
            updateHotel.setServiceFacilities(hotelFacilityRepository.findAllById(hotel.getServiceFacilities()));
        }

        hotelRepository.save(updateHotel);
        return updateHotel;
    }

    @Transactional
    public String deleteHotel (String hotelId) throws Exception {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new Exception("Hotel not found"));

        if (hotel.getImg() != null && !hotel.getImg().isEmpty()) {
            for (String publicId : hotel.getImg()) {
                try {
                    cloudinaryService.deleteImage(publicId);
                } catch (Exception e) {
                    System.err.println("Failed to delete image from Cloudinary: " + publicId);
                }
            }
        }

        if (hotel.getRoomTypes() != null && !hotel.getRoomTypes().isEmpty()) {
            for (HotelRoomType roomType : hotel.getRoomTypes()) {
                if (roomType.getImg() != null && !roomType.getImg().isEmpty()) {
                    for (String publicId : roomType.getImg()) {
                        try {
                            cloudinaryService.deleteImage(publicId);
                        } catch (Exception e) {
                            System.err.println("Failed to delete image from Cloudinary: " + publicId);
                        }
                    }
                }
                try {
                    hotelRoomTypeRepository.deleteById(roomType.get_id());
                } catch (Exception e) {
                    System.err.println("Không xoá được roomType: " + roomType.get_id());
                }
            }
        }

        hotelRepository.deleteById(hotelId);
        return "Xóa khách sạn thành công";
    }

    @Transactional
    public List<Map<String, Object>> getAvailableRooms(
            String hotelId,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            int numRooms,
            int numAdults
    ) throws Exception {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new Exception("Hotel not found"));

        List<Map<String, Object>> result = new ArrayList<>();
        for (HotelRoomType roomType : hotel.getRoomTypes()) {
            List<Map<String, Object>> availableRooms = new ArrayList<>();
            for (Room room : roomType.getRooms()){
//                List<HotelBooking> bookings = hotelBookingRepository
//                        .findByHotelIdAndRoomTypeIdAndRoomIdAndCheckinBeforeAndCheckoutAfterAndBookingStatusIn(
//                                hotelId,
//                                roomType.get_id(),
//                                room.get_id(),
//                                checkOutDate,
//                                checkInDate,
//                                Arrays.asList("pending", "confirmed")
//                        );
                int roomsBooked = 0;
                int roomsAvailable = room.getNumberOfRoom() - roomsBooked;
                long numNights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
                if (roomsAvailable >= numRooms) {
                    double totalPrice = room.getPrice() * numNights * numRooms;

                    Map<String, Object> roomInfo = new HashMap<>();
                    roomInfo.put("_id", room.get_id());
                    roomInfo.put("checkin", checkInDate);
                    roomInfo.put("checkout", checkOutDate);
                    roomInfo.put("totalNights", numNights);
                    roomInfo.put("totalRooms", numRooms);
                    roomInfo.put("totalPrice", totalPrice);
                    roomInfo.put("numRoomsAvailable", roomsAvailable);
                    roomInfo.put("bedType", room.getBedType());
                    roomInfo.put("serveBreakfast", room.getServeBreakfast());
                    roomInfo.put("maxOfGuest", room.getMaxOfGuest());
                    roomInfo.put("numberOfRoom", room.getNumberOfRoom());
                    roomInfo.put("price", room.getPrice());
                    roomInfo.put("cancellationPolicy", room.getCancellationPolicy());

                    availableRooms.add(roomInfo);
                }
            }
            if (!availableRooms.isEmpty()) {
                Map<String, Object> roomTypeInfo = new HashMap<>();
                roomTypeInfo.put("_id", roomType.get_id());
                roomTypeInfo.put("name", roomType.getName());
                roomTypeInfo.put("img", roomType.getImg());
                roomTypeInfo.put("area", roomType.getArea());
                roomTypeInfo.put("view", roomType.getView());
                roomTypeInfo.put("roomFacilities", roomType.getRoomFacilities());
                roomTypeInfo.put("rooms", availableRooms);

                result.add(roomTypeInfo);
            }
        }
        return result;
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
