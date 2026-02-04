package com.ecoride.user_service.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    @Column(name = "mobile_number", unique = true)
    @JsonProperty("mobileNumber") // Postman ke "mobileNumber" se match karega
    private String mobileNumber;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private Double earnings = 0.0;
    private String role; // "RIDER" ya "DRIVER"

    private String vehicleNumber;
    private String licenseImageUrl;
    private Boolean isApproved = false;

    // Custom helper (Lombok handles basic ones, but keeping for clarity)
    public void setApproved(boolean approved) {
        this.isApproved = approved;
    }
}