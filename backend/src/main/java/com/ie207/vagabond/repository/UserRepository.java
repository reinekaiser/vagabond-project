package com.ie207.vagabond.repository;

import com.ie207.vagabond.model.User;
import com.ie207.vagabond.model.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findById(String id);
    Optional<User> findByEmail(String email);

    Page<User> findAllByRole(Role role, Pageable pageable);
}
