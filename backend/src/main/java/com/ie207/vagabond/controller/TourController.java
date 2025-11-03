package com.ie207.vagabond.controller;

import com.ie207.vagabond.exception.TicketNotFoundException;
import com.ie207.vagabond.exception.TicketNotInTourException;
import com.ie207.vagabond.exception.TourException;
import com.ie207.vagabond.exception.TourNotFoundException;
import com.ie207.vagabond.model.Ticket;
import com.ie207.vagabond.model.Tour;
import com.ie207.vagabond.request.CreateTicketRequest;
import com.ie207.vagabond.request.CreateTourRequest;
import com.ie207.vagabond.request.UpdateTicketRequest;
import com.ie207.vagabond.request.UpdateTourRequest;
import com.ie207.vagabond.response.*;
import com.ie207.vagabond.service.TourService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tours")
@RequiredArgsConstructor
public class TourController {
    private final TourService tourService;

    @PostMapping()
    public ResponseEntity<Tour> createTour(@RequestBody CreateTourRequest request) throws TourException {
        Tour tour = tourService.createTour(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(tour);
    }

    @PostMapping("/{tourId}/tickets")
    public ResponseEntity<Ticket> addTicketToTour(@PathVariable String tourId, @RequestBody CreateTicketRequest request)
            throws TourException, TourNotFoundException {
        Ticket ticket = tourService.addTicketToTour(tourId,request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
    }

    @DeleteMapping("/{tourId}/tickets/{ticketId}")
    public ResponseEntity<DeleteTicketResponse> deleteTicketFromTour(@PathVariable String tourId, @PathVariable String ticketId)
            throws TourNotFoundException, TourException, TicketNotFoundException, TicketNotInTourException {
        DeleteTicketResponse response = tourService.deleteTicketFromTour(tourId,ticketId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{tourId}/tickets/{ticketId}")
    public ResponseEntity<?> updateTicketInTour(@PathVariable String tourId, @PathVariable String ticketId, @RequestBody UpdateTicketRequest request) {
        Ticket updatedTicket = tourService.updateTicketInTour(tourId, ticketId, request);
        return ResponseEntity.ok(updatedTicket);
    }

    @DeleteMapping("/{tourId}")
    public ResponseEntity<DeleteTourResponse> deleteTour(@PathVariable String tourId) throws TourNotFoundException {
        DeleteTourResponse response = tourService.deleteTour(tourId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{tourId}")
    public ResponseEntity<?> updateTour(
            @PathVariable String tourId,
            @RequestBody UpdateTourRequest request) {
        Tour updatedTour = tourService.updateTour(tourId, request);
        return ResponseEntity.ok(updatedTour);
    }

    @GetMapping("/suggestions")
    public ResponseEntity<?> getSearchSuggestions(@RequestParam String q) {
        SearchSuggestionResponse response = tourService.getSearchSuggestions(q);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/results")
    public ResponseEntity<?> getSearchResults(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String duration,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int pageSize)  {

        SearchResultResponse response = tourService.getSearchResults(
                query, minPrice, maxPrice, category, language, duration, sort, page, pageSize);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/")
    public ResponseEntity<GetToursResponse> getTours(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String languageService,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String duration,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "15") Integer limit,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String cityId) {
        GetToursResponse response = tourService.getTours(minPrice,  maxPrice, category, languageService, duration, sort, page, limit, cityId);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/{tourId}")
    public ResponseEntity<Tour> getTour(@PathVariable String tourId) {
        Tour tour = tourService.getTour(tourId);
        return ResponseEntity.ok(tour);
    }
    @GetMapping("/stats")
    public ResponseEntity<TourStatsResponse> getTourStats() {
        TourStatsResponse response = tourService.getTourStats();
        return ResponseEntity.ok(response);
    }
}
