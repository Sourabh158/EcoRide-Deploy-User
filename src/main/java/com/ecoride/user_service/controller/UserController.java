package com.ecoride.user_service.controller;

import com.ecoride.user_service.config.JwtUtil;
import com.ecoride.user_service.model.User;
import com.ecoride.user_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // Admin Dashboard Stats logic
    @GetMapping("/admin/stats")
    public Map<String, Object> getUserStats() {
        List<User> allUsers = userRepository.findAll();

        long activeDrivers = allUsers.stream()
                .filter(u -> "DRIVER".equals(u.getRole()) && Boolean.TRUE.equals(u.getApproved()))
                .count();

        long pendingVerifications = allUsers.stream()
                .filter(u -> "DRIVER".equals(u.getRole()) && !Boolean.TRUE.equals(u.getApproved()))
                .count();

        return Map.of(
                "activeDrivers", activeDrivers,
                "pendingVerifications", pendingVerifications,
                "totalUsers", allUsers.size()
        );
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(@RequestHeader("loggedInUser") String email) {
        // Email normalization added
        return userRepository.findByEmail(email.toLowerCase().trim())
                .map(user -> {
                    user.setPassword(null);
                    return ResponseEntity.ok(user);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/login")
    public String login(@RequestBody User loginRequest) {
        // Normalize email to match DB
        String email = loginRequest.getEmail().toLowerCase().trim();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null && passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return jwtUtil.generateToken(user.getEmail());
        } else {
            return "Error: Invalid Email or Password!";
        }
    }

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        // Normalize email before saving
        String normalizedEmail = user.getEmail().toLowerCase().trim();
        if(userRepository.findByEmail(normalizedEmail).isPresent()) {
            return "Error: Email '" + normalizedEmail + "' is already registered!";
        }

        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return user.getRole() + " registered successfully with ID: " + user.getId();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // FIXED: Ye endpoint 404 de raha tha, ab lowercase match karega
    // UserController.java ke andar is method ko update karein
    @GetMapping("/get-id")
    public Long getUserIdByEmail(@RequestParam String email) {
        // Trim spaces aur convert to lowercase taaki DB se match ho jaye
        String normalizedEmail = email.trim().toLowerCase();
        System.out.println("DEBUG: Searching for normalized email: " + normalizedEmail);

        return userRepository.findByEmail(normalizedEmail)
                .map(User::getId)
                .orElse(null);
    }

    @PutMapping("/{id}/toggle-online")
    public ResponseEntity<String> toggleOnline(@PathVariable Long id, @RequestParam boolean status) {
        return userRepository.findById(id).map(user -> {
            user.setIsOnline(status);
            userRepository.save(user);
            return ResponseEntity.ok("Driver is now " + (status ? "Online" : "Offline"));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/get-role")
    public String getUserRoleByEmail(@RequestParam String email) {
        return userRepository.findByEmail(email.toLowerCase().trim())
                .map(User::getRole)
                .orElse("RIDER");
    }

    @PutMapping("/{id}/update-earnings")
    public void updateEarnings(@PathVariable Long id, @RequestParam Double amount) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getEarnings() == null) {
            user.setEarnings(0.0);
        }

        user.setEarnings(user.getEarnings() + amount);
        userRepository.save(user);
    }

    @PostMapping("/register/driver")
    public ResponseEntity<String> registerDriver(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("vehicleNumber") String vehicleNumber,
            @RequestParam("licenseImage") MultipartFile file) {

        try {
            String uploadDir = "/app/uploads/";
            File dir = new File(uploadDir);

            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            file.transferTo(new File(uploadDir + fileName));

            User driver = new User();
            driver.setName(name);
            driver.setEmail(email.toLowerCase().trim()); // Lowercase for driver too
            driver.setPassword(passwordEncoder.encode(password));
            driver.setRole("DRIVER");
            driver.setVehicleNumber(vehicleNumber);
            driver.setLicenseImageUrl(fileName);
            driver.setApproved(false);

            userRepository.save(driver);
            return ResponseEntity.ok("Driver registration successful! Admin will verify your documents.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/admin/approve-driver/{id}")
    public ResponseEntity<String> approveDriver(@PathVariable Long id) {
        return userRepository.findById(id).map(user -> {
            if (!"DRIVER".equalsIgnoreCase(user.getRole())) {
                return ResponseEntity.badRequest().body("Error: This user is not a Driver!");
            }
            user.setApproved(true);
            userRepository.save(user);
            return ResponseEntity.ok("Driver Approved! Now they can accept rides.");
        }).orElse(ResponseEntity.status(404).body("Driver not found!"));
    }
}