package com.ecoride.user_service.repository;

import com.ecoride.user_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Ise Optional banayein taaki Controller mein .map() chal sake
    Optional<User> findByEmail(String email);

    Optional<User> findByMobileNumber(String mobileNumber);
}