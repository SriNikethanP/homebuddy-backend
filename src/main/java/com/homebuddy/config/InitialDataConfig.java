package com.homebuddy.config;

import com.homebuddy.entity.Admin;
import com.homebuddy.repository.AdminRepository;
import com.homebuddy.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class InitialDataConfig {

    private static final Logger logger = LoggerFactory.getLogger(InitialDataConfig.class);
    private final AdminService adminService;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initializeData() {
        return args -> {
            try {
                logger.info("Checking database connection and admin accounts...");
                long adminCount = adminRepository.count();
                logger.info("Current admin count: {}", adminCount);

                // Skip super admin creation if any admin exists
                if (adminCount > 0) {
                    logger.info("Admin accounts already exist in the database. Skipping super admin creation.");
                    return;
                }

                // Only create super admin if no admin exists
                logger.info("No admin accounts found. Creating super admin account...");
                Admin superAdmin = new Admin();
                superAdmin.setUsername("lokesh");
                superAdmin.setPassword(passwordEncoder.encode("dhagratwar6893"));
                superAdmin.setEmail("home.buddy6893@gmail.com");
                superAdmin.setRole(Admin.AdminRole.SUPER_ADMIN);
                superAdmin.setActive(true);
                
                Admin createdAdmin = adminService.createSuperAdmin(superAdmin);
                logger.info("Super admin account created successfully!");
                logger.info("Username: lokesh");
                logger.info("Password: dhagratwar6893");
                logger.info("Admin ID: {}", createdAdmin.getId());
            } catch (Exception e) {
                logger.error("Error during initialization: ", e);
                throw e;
            }
        };
    }
} 