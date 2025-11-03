package com.ie207.vagabond.repository;

import com.ie207.vagabond.model.Ticket;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TicketRepository extends MongoRepository<Ticket, String> {
    boolean existsById(String id);

}
