package com.homebuddy.controller;

import com.homebuddy.entity.Admin;
import com.homebuddy.service.AdminService;
import com.homebuddy.security.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthenticationManager authenticationManager;
    private final AdminService adminService;
    private final JwtTokenUtil jwtTokenUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        logger.info("Login attempt for user: {}", loginRequest.get("username"));
        
        // Validate request
        if (loginRequest.get("username") == null || loginRequest.get("password") == null) {
            logger.error("Login failed: Missing username or password");
            return ResponseEntity.badRequest().body("Username and password are required");
        }
        
        try {
            // Attempt authentication
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.get("username"),
                    loginRequest.get("password")
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            Admin admin = (Admin) authentication.getPrincipal();

            if (!admin.isEnabled()) {
                logger.error("Login failed: Account is not active for user: {}", admin.getUsername());
                return ResponseEntity.status(403).body("Account is not active. Please contact the administrator.");
            }

            // Generate JWT token
            String jwt = jwtTokenUtil.generateToken(admin);
            
            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("username", admin.getUsername());
            response.put("role", admin.getRole().name());
            
            logger.info("Login successful for user: {}", admin.getUsername());
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            logger.error("Login failed: Invalid credentials for user: {}", loginRequest.get("username"));
            return ResponseEntity.status(401).body("Invalid username or password");
        } catch (DisabledException e) {
            logger.error("Login failed: Account disabled for user: {}", loginRequest.get("username"));
            return ResponseEntity.status(403).body("Account is disabled. Please contact the administrator.");
        } catch (Exception e) {
            logger.error("Login error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("An error occurred during login. Please try again later.");
        }
    }

    @GetMapping("/check-admins")
    public ResponseEntity<?> checkAdmins() {
        try {
            List<Admin> admins = adminService.getAllAdmins();
            Map<String, Object> response = new HashMap<>();
            response.put("count", admins.size());
            response.put("admins", admins.stream().map(admin -> {
                Map<String, Object> adminInfo = new HashMap<>();
                adminInfo.put("id", admin.getId());
                adminInfo.put("username", admin.getUsername());
                adminInfo.put("email", admin.getEmail());
                adminInfo.put("role", admin.getRole().name());
                adminInfo.put("active", admin.isEnabled());
                return adminInfo;
            }).toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error checking admins: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error checking admin accounts: " + e.getMessage());
        }
    }

    @PostMapping("/register/super-admin")
    public ResponseEntity<?> registerSuperAdmin(@RequestBody Admin admin) {
        try {
            Admin createdAdmin = adminService.createSuperAdmin(admin);
            return ResponseEntity.ok(createdAdmin);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/register/admin")
    public ResponseEntity<?> registerAdmin(@RequestBody Admin admin) {
        try {
            Admin createdAdmin = adminService.createAdmin(admin);
            return ResponseEntity.ok(createdAdmin);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/activate/{adminId}")
    public ResponseEntity<?> activateAdmin(@PathVariable Long adminId) {
        try {
            Admin activatedAdmin = adminService.activateAdmin(adminId);
            return ResponseEntity.ok(activatedAdmin);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/deactivate/{adminId}")
    public ResponseEntity<?> deactivateAdmin(@PathVariable Long adminId) {
        try {
            Admin deactivatedAdmin = adminService.deactivateAdmin(adminId);
            return ResponseEntity.ok(deactivatedAdmin);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/update-password/{adminId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updatePassword(@PathVariable Long adminId, @RequestBody String newPassword) {
        try {
            adminService.updatePassword(adminId, newPassword);
            return ResponseEntity.ok().body("Password updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 