package com.ie207.vagabond.service;

import com.ie207.vagabond.exception.*;
import com.ie207.vagabond.model.*;
import com.ie207.vagabond.repository.CityRepository;
import com.ie207.vagabond.repository.TicketRepository;
import com.ie207.vagabond.repository.TourRepository;
import com.ie207.vagabond.request.*;
import com.ie207.vagabond.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TourService {
    private final TourRepository tourRepository;
    private final CityRepository cityRepository;
    private final TicketRepository ticketRepository;

    private final MongoTemplate mongoTemplate;

    @Transactional(rollbackFor = Exception.class)
    public Tour createTour(CreateTourRequest request) throws TourException {
        List<Ticket> savedTickets = new ArrayList<>();

        for (CreateTicketRequest ticketRequest : request.getTickets()) {
            Ticket ticket = mapToTicketEntity(ticketRequest);
            Ticket savedTicket = ticketRepository.save(ticket);
            savedTickets.add(savedTicket);
        }

        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new CityNotFoundException("City not found with id: " + request.getCityId()));

        // 3. Tạo tour
        Tour tour = mapToTourEntity(request, city, savedTickets);

        return tourRepository.save(tour);
    }

    @Transactional(rollbackFor = Exception.class)
    public Ticket addTicketToTour(String tourId, CreateTicketRequest request) throws TourException, TourNotFoundException, TicketException {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new TourNotFoundException("Tour không tồn tại với id: " + tourId));

        Ticket savedTicket = ticketRepository.save(mapToTicketEntity(request));

        if (tour.getTickets() == null) {
            tour.setTickets(new ArrayList<>());
        }
        tour.getTickets().add(savedTicket);
        tourRepository.save(tour);

        return savedTicket;
    }

    @Transactional(rollbackFor = Exception.class)
    public DeleteTicketResponse deleteTicketFromTour(String tourId, String ticketId) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new TourNotFoundException("Tour không tồn tại với id: " + tourId));

        if (!containsTicket(tour.getTickets(), ticketId)) {
            throw new TicketNotInTourException("Vé không tồn tại với id: " + ticketId);
        }

        if (!ticketRepository.existsById(ticketId)) {
            throw new TicketNotFoundException("Vé không tồn tại với id: " + ticketId);
        }

        removeTicketFromTour(tour, ticketId);

        ticketRepository.deleteById(ticketId);
        log.info("Đã xóa ticket {} khỏi database", ticketId);

        DeleteTicketResponse response = new DeleteTicketResponse();

        // Check if tour has no tickets left
        if (tour.getTickets().isEmpty()) {
            tourRepository.delete(tour);
            log.info("Đã xóa tour {} vì không còn vé", tourId);
            response.setMessage("Xóa vé và xóa tour vì tour không còn vé");
            response.setTourDeleted(true);
        } else {
            Tour savedTour = tourRepository.save(tour);
            response.setMessage("Xóa vé thành công");
            response.setTourDeleted(false);
            response.setTour(savedTour);
        }

        return response;
    }

    @Transactional
    public Ticket updateTicketInTour(String tourId, String ticketId, UpdateTicketRequest request) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new TourNotFoundException("Tour không tồn tại với id: " + tourId));


        if (!containsTicket(tour.getTickets(), ticketId)) {
            throw new TicketNotInTourException("Vé không có trong tour với id: " + ticketId);
        }

        Ticket existingTicket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Vé không tồn tại với id: " + ticketId));

        Ticket updatedTicket = updateTicketEntity(existingTicket, request);
        Ticket savedTicket = ticketRepository.save(updatedTicket);

        log.info("Đã cập nhật ticket {} trong tour {}", ticketId, tourId);
        return savedTicket;
    }

    @Transactional()
    public DeleteTourResponse deleteTour(String tourId) {
        Tour tour = tourRepository.findById(tourId).
                orElseThrow(() -> new TourNotFoundException("Tour không tồn tại với id: " + tourId));

        List<String> ticketIds = extractTicketId(tour);

        if (!ticketIds.isEmpty()) {
            ticketRepository.deleteAllById(ticketIds);
        }

        tourRepository.deleteById(tourId);

        return DeleteTourResponse.builder()
                .message("Đã xóa tour và các vé liên quan")
                .deletedTourId(tourId)
                .deletedTicketCount(ticketIds.size())
                .build();
    }

    @Transactional
    public Tour updateTour(String tourId, UpdateTourRequest request) {

        Tour existingTour = tourRepository.findById(tourId)
                .orElseThrow(() -> new TourNotFoundException("Tour không tồn tại với id: " + tourId));

        updateTourFromRequest(existingTour, request);

        Tour savedTour = tourRepository.save(existingTour);
        log.info("Đã cập nhật tour {}", tourId);
        return savedTour;
    }

    private void updateTourFromRequest(Tour tour, UpdateTourRequest request) {
        if (request.getName() != null) tour.setName(request.getName());
        if (request.getCategory() != null) tour.setCategory(request.getCategory());
        if (request.getLocation() != null) tour.setLocation(request.getLocation());
        if (request.getDuration() != null) {
            tour.setDuration(request.getDuration());
            tour.calculateDurationInHours();
        }
        if (request.getExperiences() != null) tour.setExperiences(request.getExperiences());
        if (request.getImages() != null) tour.setImages(request.getImages());
        if (request.getLanguageService() != null) tour.setLanguageService(request.getLanguageService());
        if (request.getContact() != null) tour.setContact(request.getContact());
        if (request.getSuitableFor() != null) tour.setSuitableFor(request.getSuitableFor());
        if (request.getAdditionalInformation() != null) tour.setAdditionalInformation(request.getAdditionalInformation());
        if (request.getItinerary() != null) tour.setItinerary(request.getItinerary());
        if (request.getFromPrice() != null) tour.setFromPrice(request.getFromPrice());
        if (request.getAvgRating() != null) tour.setAvgRating(request.getAvgRating());
        if (request.getBookings() != null) tour.setBookings(request.getBookings());
    }


    private boolean containsTicket(List<Ticket> tickets, String ticketId) {
        return tickets.stream().anyMatch(ticket -> ticket.getId().equals(ticketId));
    }

    private void removeTicketFromTour(Tour tour, String ticketId) {
        List<Ticket> updatedTickets = tour.getTickets().stream()
                .filter(ticket -> !ticket.getId().equals(ticketId))
                .toList();
        tour.setTickets(updatedTickets);
    }

    private List<String> extractTicketId(Tour tour) {
        if (tour.getTickets() == null || tour.getTickets().isEmpty()) {
            return new ArrayList<>();
        }

        return tour.getTickets().stream()
                .map(Ticket::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Ticket updateTicketEntity(Ticket existingTicket, UpdateTicketRequest request) {

        if (request.getTitle() != null) {
            existingTicket.setTitle(request.getTitle());
        }
        if (request.getSubtitle() != null) {
            existingTicket.setSubtitle(request.getSubtitle());
        }
        if (request.getDescription() != null) {
            existingTicket.setDescription(request.getDescription());
        }
        if (request.getPrices() != null) {
            existingTicket.setPrices(mapToPriceInfoEntities(request.getPrices()));
        }
        if (request.getMaxQuantity() != null) {
            existingTicket.setMaxQuantity(request.getMaxQuantity());
        }

        if (request.getOverview() != null) {
            existingTicket.setOverview(request.getOverview());
        }
        if (request.getVoucherValidity() != null) {
            existingTicket.setVoucherValidity(request.getVoucherValidity());
        }
        if (request.getRedemptionPolicy() != null) {
            existingTicket.setRedemptionPolicy(mapToRedemptionPolicyEntity(request.getRedemptionPolicy()));
        }
        if (request.getCancellationPolicy() != null) {
            existingTicket.setCancellationPolicy(mapToCancellationPolicyEntity(request.getCancellationPolicy()));
        }
        if (request.getTermsAndConditions() != null) {
            existingTicket.setTermsAndConditions(request.getTermsAndConditions());
        }

        return existingTicket;
    }

    private Ticket mapToTicketEntity(CreateTicketRequest request) {
        return Ticket.builder()
                .title(request.getTitle())
                .subtitle(request.getSubtitle())
                .description(request.getDescription())
                .prices(mapToPriceInfoEntities(request.getPrices()))
                .maxQuantity(request.getMaxQuantity())
                .overview(request.getOverview())
                .voucherValidity(request.getVoucherValidity())
                .redemptionPolicy(mapToRedemptionPolicyEntity(request.getRedemptionPolicy()))
                .cancellationPolicy(mapToCancellationPolicyEntity(request.getCancellationPolicy()))
                .termsAndConditions(request.getTermsAndConditions())
                .build();
    }

    private List<PriceInfo> mapToPriceInfoEntities(List<PriceInfoRequest> priceRequests) {
        if (priceRequests == null) return new ArrayList<>();

        return priceRequests.stream()
                .map(this::mapToPriceInfoEntity)
                .toList();
    }

    private PriceInfo mapToPriceInfoEntity(PriceInfoRequest request) {
        return PriceInfo.builder()
                .priceType(request.getPriceType())
                .notes(request.getNotes())
                .price(request.getPrice())
                .minPerBooking(request.getMinPerBooking())
                .maxPerBooking(request.getMaxPerBooking())
                .build();
    }

    private RedemptionPolicy mapToRedemptionPolicyEntity(RedemptionPolicyRequest request) {
        if (request == null) return null;

        return RedemptionPolicy.builder()
                .method(request.getMethod())
                .location(request.getLocation())
                .build();
    }

    private CancellationPolicy mapToCancellationPolicyEntity(CancellationPolicyRequest request) {
        if (request == null) return null;

        return CancellationPolicy.builder()
                .isReschedule(request.getIsReschedule())
                .reschedulePolicy(request.getReschedulePolicy())
                .isRefund(request.getIsRefund())
                .refundPolicy(mapToRefundPolicyEntity(request.getRefundPolicy()))
                .build();
    }

    private RefundPolicy mapToRefundPolicyEntity(RefundPolicyRequest request) {
        if (request == null) return null;

        return RefundPolicy.builder()
                .refundPercentage(mapToRefundPercentageEntities(request.getRefundPercentage()))
                .description(request.getDescription())
                .build();
    }

    private List<RefundPercentage> mapToRefundPercentageEntities(List<RefundPercentageRequest> requests) {
        if (requests == null) return new ArrayList<>();

        return requests.stream()
                .map(this::mapToRefundPercentageEntity)
                .toList();
    }

    private RefundPercentage mapToRefundPercentageEntity(RefundPercentageRequest request) {
        return RefundPercentage.builder()
                .daysBefore(request.getDaysBefore())
                .percent(request.getPercent())
                .build();
    }

    private Tour mapToTourEntity(CreateTourRequest request, City city, List<Ticket> tickets) {
        Tour tour = new Tour();
        tour.setName(request.getName());
        tour.setCategory(request.getCategory());
        tour.setLocation(request.getLocation());
        tour.setCity(city);
        tour.setDuration(request.getDuration());
        tour.setExperiences(request.getExperiences());
        tour.setLanguageService(request.getLanguageService());
        tour.setContact(request.getContact());
        tour.setSuitableFor(request.getSuitableFor());
        tour.setAdditionalInformation(request.getAdditionalInformation());
        tour.setItinerary(request.getItinerary());
        tour.setImages(request.getImages());
        tour.setTickets(tickets);
        tour.setFromPrice(request.getFromPrice());

        return tour;
    }

    @Transactional
    public SearchSuggestionResponse getSearchSuggestions(String query) {

        List<Document> tourPipeline = Arrays.asList(
                new Document("$search", new Document()
                        .append("index", "search_tour")
                        .append("text", new Document()
                                .append("query", query)
                                .append("path", Arrays.asList("name", "category", "location", "experiences"))
                                .append("fuzzy", new Document())
                        )
                ),
                new Document("$limit", 3),
                new Document("$lookup", new Document()
                        .append("from", "tickets")
                        .append("localField", "tickets")
                        .append("foreignField", "_id")
                        .append("as", "ticketDetails")
                        .append("pipeline", Arrays.asList(
                                new Document("$sort", new Document("createdAt", -1)),
                                new Document("$limit", 3),
                                new Document("$project", new Document()
                                        .append("_id", 1)
                                        .append("title", 1)
                                        .append("prices", 1)
                                )
                        ))
                ),
                new Document("$project", new Document()
                        .append("name", 1)
                        .append("location", 1)
                        .append("images", 1)
                        .append("ticketDetails", 1)
                )
        );


        List<Document> tours = mongoTemplate
                .getCollection("tours")
                .aggregate(tourPipeline)
                .into(new ArrayList<>());

        List<TourSuggestion> tourSuggestions = tours.stream()
                .map(this::convertDocumentToTourSuggestion)
                .collect(Collectors.toList());

        // Search cities (giữ nguyên)
        List<CitySuggestion> citySuggestions = searchCities(query);

        return SearchSuggestionResponse.builder()
                .tours(tourSuggestions)
                .cities(citySuggestions)
                .build();
    }

    private TourSuggestion convertDocumentToTourSuggestion(Document doc) {
        return TourSuggestion.builder()
                .id(doc.getObjectId("_id").toString())
                .name(doc.getString("name"))
                .location(doc.getString("location"))
                .images(doc.getList("images", String.class))
                .ticketDetails(convertDocumentsToTicketDetails(doc.getList("ticketDetails", Document.class)))
                .build();
    }

    private List<TicketDetail> convertDocumentsToTicketDetails(List<Document> ticketDocs) {
        if (ticketDocs == null) return new ArrayList<>();

        return ticketDocs.stream()
                .map(doc -> TicketDetail.builder()
                        .id(doc.getObjectId("_id").toString())
                        .title(doc.getString("title"))
                        .prices(convertDocumentsToPrices(doc.getList("prices", Document.class)))
                        .build())
                .collect(Collectors.toList());
    }

    private List<PriceInfo> convertDocumentsToPrices(List<Document> priceDocs) {
        if (priceDocs == null) return new ArrayList<>();

        return priceDocs.stream()
                .map(doc -> {
                    Object priceValue = doc.get("price");
                    Integer price = (priceValue instanceof Number) ? ((Number) priceValue).intValue() : null;

                    return PriceInfo.builder()
                            .priceType(doc.getString("priceType"))
                            .notes(doc.getString("notes"))
                            .price(price)
                            .minPerBooking(doc.getInteger("minPerBooking"))
                            .maxPerBooking(doc.getInteger("maxPerBooking"))
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<CitySuggestion> searchCities(String query) {
        Pattern pattern = Pattern.compile(query, Pattern.CASE_INSENSITIVE);

        Query searchQuery = Query.query(Criteria.where("name").regex(pattern))
                .with(Sort.by(Sort.Direction.ASC, "name"));

        searchQuery.fields().include("name");

        List<City> cities = mongoTemplate.find(searchQuery, City.class);

        return cities.stream()
                .map(city -> CitySuggestion.builder()
                        .id(city.getId())
                        .name(city.getName())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public SearchResultResponse getSearchResults(String query, Double minPrice, Double maxPrice, String category,
                                                                      String language, String duration, String sort, int page, int pageSize) {
        List<Document> pipeline = buildPipeline(query, minPrice, maxPrice, category, language, duration, sort, page, pageSize);

        Document result = mongoTemplate
                .getCollection("tours")
                .aggregate(pipeline)
                .first();

        assert result != null;
        return mapToSearchResultResponse(result, page, pageSize);
    }

    private List<Document> buildPipeline(String query, Double minPrice, Double maxPrice, String category, String language, String duration, String sort, int page, int pageSize) {
        List<Document> pipeline = new ArrayList<>();

        assert query != null;
        if (query != null & !query.trim().isEmpty()) {
            pipeline.add(new Document("$search", new Document()
                    .append("index", "search_tour")
                    .append("text", new Document()
                            .append("query", query)
                            .append("path", Arrays.asList("name", "category", "location"))
                            .append("fuzzy", new Document())
                    )
                    .append("scoreDetails", true)
            ));
        }

        pipeline.add(new Document("$addFields", new Document()
                .append("searchScore", new Document("$meta", "searchScore"))
        ));

        Document matchStage = buildMatchStage(minPrice, maxPrice, category, language, duration);
        if (!matchStage.isEmpty()) {
            pipeline.add(new Document("$match", matchStage));
        }

        Document sortStage = buildSortStage(sort, query != null);
        if (!sortStage.isEmpty()) {
            pipeline.add(new Document("$sort", sortStage));
        }
        List<Document> dataPipeline = new ArrayList<>();
        dataPipeline.add(new Document("$skip", (page - 1) * pageSize));
        dataPipeline.add(new Document("$limit", pageSize));
        dataPipeline.add(new Document("$project", new Document()
                .append("name", 1)
                .append("location", 1)
                .append("images", 1)
                .append("category", 1)
                .append("languageService", 1)
                .append("duration", 1)
                .append("fromPrice", 1)
        ));

        pipeline.add(new Document("$facet", new Document()
                .append("data", dataPipeline)
                .append("totalCount", Arrays.asList(new Document("$count", "count")))
        ));

        return pipeline;
    }

    private Document buildMatchStage(Double minPrice, Double maxPrice, String category, String language, String duration) {
        Document match = new Document();
        match.append("searchScore", new Document("$gte", 0.8));
        if (minPrice != null || maxPrice != null) {
            Document priceMatch = new Document();
            if (minPrice != null) priceMatch.append("$gte", minPrice);
            if (maxPrice != null) priceMatch.append("$lte", maxPrice);
            match.append("fromPrice", priceMatch);
        }
        if (category != null && !category.trim().isEmpty()) {
            match.append("category", new Document("$in", Arrays.asList(category.split(","))));
        }

        if (language != null && !language.trim().isEmpty()) {
            match.append("languageService", new Document("$in", Arrays.asList(language.split(","))));
        }

        if (duration != null && !duration.trim().isEmpty()) {
            Document durationMatch = buildDurationCriteria(duration);
            match.putAll(durationMatch);
        }

        return match;
    }

    private Document buildDurationCriteria(String duration) {
        Document criteria = new Document();
        switch (duration) {
            case "0-3":
                criteria.append("durationInHours", new Document("$gte", 0).append("$lte", 3));
                break;
            case "3-5":
                criteria.append("durationInHours", new Document("$gte", 3).append("$lte", 5));
                break;
            case "5-7":
                criteria.append("durationInHours", new Document("$gte", 5).append("$lte", 7));
                break;
            case "1-day":
                criteria.append("durationInHours", new Document("$gte", 24).append("$lte", 48));
                break;
            case "2-days-plus":
                criteria.append("durationInHours", new Document("$gte", 24));
                break;
        }
        return criteria;
    }

    private Document buildSortStage(String sort, boolean hasQuery) {
        Document sortStage = new Document();

        if (hasQuery) {
            switch (sort) {
                case "relevance":
                case null:
                case "":
                    sortStage.append("searchScore", -1);
                    break;
                case "new":
                    sortStage.append("createdAt", -1);
                    break;
                case "rating":
                    sortStage.append("avgRating", -1);
                    break;
                case "price":
                    sortStage.append("fromPrice", 1);
                    break;
                default:
                    sortStage.append("searchScore", -1);
                    break;
            }
        } else {
            // Không có search query
            switch (sort) {
                case "new":
                    sortStage.append("createdAt", -1);
                    break;
                case "rating":
                    sortStage.append("avgRating", -1);
                    break;
                case "price":
                    sortStage.append("fromPrice", 1);
                    break;
                default:
                    sortStage.append("createdAt", -1);
                    break;
            }
        }

        return sortStage;
    }

    private SearchResultResponse mapToSearchResultResponse(Document resultDoc, int page, int pageSize) {
        List<Document> data = resultDoc.getList("data", Document.class);
        List<Document> totalCount = resultDoc.getList("totalCount", Document.class);

        long total = 0L;
        if (totalCount != null && !totalCount.isEmpty()) {
            Object countValue = totalCount.getFirst().get("count");
            if (countValue instanceof Number) {
                total = ((Number) countValue).longValue(); // xử lý cả Integer & Long
            }
        }
        int totalPages = (int) Math.ceil((double) total / pageSize);

        List<TourSearchResult> tours = data.stream()
                .map(this::convertDocumentToTourSearchResult)
                .collect(Collectors.toList());

        return SearchResultResponse.builder()
                .tours(tours)
                .totalPages(totalPages)
                .page(page)
                .total(total)
                .build();
    }

    private TourSearchResult convertDocumentToTourSearchResult(Document doc) {
        Integer fromPrice = null;
        Object priceValue = doc.get("fromPrice");
        if (priceValue instanceof Number) {
            fromPrice = ((Number) priceValue).intValue(); // ✅ Hỗ trợ Integer, Long, Double, Decimal128
        }

        return TourSearchResult.builder()
                .id(doc.getObjectId("_id").toString())
                .name(doc.getString("name"))
                .location(doc.getString("location"))
                .images(doc.getList("images", String.class))
                .category(doc.getList("category", String.class))
                .languageService(doc.getList("languageService", String.class))
                .duration(doc.getString("duration"))
                .fromPrice(fromPrice)
                .build();
    }

    @Transactional
    public GetToursResponse getTours(Double minPrice, Double maxPrice, String category, String languageService, String duration, String sort, int page, int pageSize, String cityId) {

        Criteria criteria = new Criteria();

        if (category != null && !category.trim().isEmpty()) {
            criteria.and("category").in(Arrays.asList(category.split(",")));
        }

        if (languageService != null && !languageService.trim().isEmpty()) {
            criteria.and("languageService").in(Arrays.asList(languageService.split(",")));
        }

        if (minPrice != null || maxPrice != null) {
            Criteria priceCriteria = new Criteria("fromPrice");
            if (minPrice != null) {
                priceCriteria.gte(minPrice);
            }
            if (maxPrice != null) {
                priceCriteria.lte(maxPrice);
            }
            criteria.andOperator(priceCriteria);
        }

        if (duration != null && !duration.trim().isEmpty()) {
            Criteria durationCriteria = switch (duration) {
                case "0-3" -> Criteria.where("durationInHours").gte(0).lte(3);
                case "3-5" -> Criteria.where("durationInHours").gte(3).lte(5);
                case "5-7" -> Criteria.where("durationInHours").gte(5).lte(7);
                case "1-day" -> Criteria.where("durationInHours").gte(24).lte(48);
                case "2-days-plus" -> Criteria.where("durationInHours").gte(24);
                default -> new Criteria();
            };
            criteria.andOperator(durationCriteria);
        }

        if (cityId != null) {
            City city = cityRepository.findById(cityId)
                    .orElseThrow(() -> new CityNotFoundException("City not found with id: " + cityId));
            criteria.and("city").is(city);
        }

        Query query = new Query(criteria);

        applySorting(query, sort);

        applyPagination(query, page, pageSize);

        List<Tour> tours = mongoTemplate.find(query, Tour.class);
        long totalTours = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Tour.class);

        int totalPages = (int) Math.ceil((double) totalTours / pageSize);

        return GetToursResponse.builder()
                .totalTours(totalTours)
                .totalPages(totalPages)
                .pageSize(pageSize)
                .currentPage(page)
                .data(tours).build();

    }


    private void applySorting(Query query, String sort) {
        if (sort != null) {
            switch (sort) {
                case "price" -> query.with(Sort.by(Sort.Direction.ASC, "fromPrice"));
                case "rating" -> query.with(Sort.by(Sort.Direction.DESC, "avgRating"));
                case "newest" -> query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
                case "popular" -> query.with(Sort.by(Sort.Direction.DESC, "bookings"));
                default -> query.with(Sort.by(Sort.Direction.DESC, "bookings"));
            }
        }
    }

    private void applyPagination(Query query, Integer page, Integer limit) {
        int pageNumber = (page == null || page < 1) ? 1 : page;
        int pageSize = (limit == null || limit < 1) ? 15 : limit;
        int skip = (pageNumber - 1) * pageSize;

        query.skip(skip);
        query.limit(pageSize);
    }

    public Tour getTour(String tourId) {
        return tourRepository.findById(tourId).orElseThrow(() -> new TourNotFoundException("Tour not found with id: " + tourId));
    }

    public TourStatsResponse getTourStats() {
        long totalCount = tourRepository.count();

        List<TourSummary> topRatedTours = tourRepository.findTop3ByOrderByAvgRatingDesc()
                .stream()
                .map(tour -> TourSummary.builder()
                        .id(tour.getId())
                        .name(tour.getName())
                        .build())
                .collect(Collectors.toList());

        List<TourSummary> cheapestTours = tourRepository.findTop3ByOrderByFromPriceAsc()
                .stream()
                .map(tour -> TourSummary.builder()
                        .id(tour.getId())
                        .name(tour.getName())
                        .build())
                .collect(Collectors.toList());

        List<TourSummary> newestTours = tourRepository.findTop3ByOrderByCreatedAtDesc()
                .stream()
                .map(tour -> TourSummary.builder()
                        .id(tour.getId())
                        .name(tour.getName())
                        .createdAt(tour.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return TourStatsResponse.builder()
                .totalCount(totalCount)
                .topRatedTours(topRatedTours)
                .cheapestTours(cheapestTours)
                .newestTours(newestTours)
                .build();
    }
}
