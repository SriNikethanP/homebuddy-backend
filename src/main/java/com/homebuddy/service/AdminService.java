package com.homebuddy.service;

import com.homebuddy.entity.Admin;
import com.homebuddy.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Admin> getAllAdmins() {
        logger.info("Retrieving all admin accounts");
        return adminRepository.findAll();
    }

    private boolean isPasswordEncoded(String password) {
        return password != null && (password.startsWith("$2a$") || password.startsWith("$2b$"));
    }

    public Admin createAdmin(Admin admin) {
        logger.info("Attempting to create new admin: {}", admin.getUsername());
        // Check if the current user is a super admin
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Admin currentAdmin = adminRepository.findByUsername(currentUsername)
                .orElseThrow(() -> {
                    logger.error("Not authenticated - no current admin found");
                    return new RuntimeException("Not authenticated");
                });

        if (currentAdmin.getRole() != Admin.AdminRole.SUPER_ADMIN) {
            logger.error("Unauthorized attempt to create admin by non-super admin: {}", currentUsername);
            throw new RuntimeException("Only super admin can create new admins");
        }

        if (adminRepository.findByUsername(admin.getUsername()).isPresent()) {
            logger.error("Username already exists: {}", admin.getUsername());
            throw new RuntimeException("Username already exists");
        }
        if (adminRepository.findByEmail(admin.getEmail()).isPresent()) {
            logger.error("Email already exists: {}", admin.getEmail());
            throw new RuntimeException("Email already exists");
        }
        
        // Check if the password is already encoded
        if (!isPasswordEncoded(admin.getPassword())) {
            logger.info("Encoding password for admin");
            admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        } else {
            logger.info("Password already encoded, skipping encoding");
        }
        
        admin.setActive(false); // New admins are inactive by default
        Admin savedAdmin = adminRepository.save(admin);
        logger.info("Admin created successfully: {}", savedAdmin.getUsername());
        return savedAdmin;
    }

    public Admin createSuperAdmin(Admin admin) {
        logger.info("Attempting to create super admin: {}", admin.getUsername());
        if (adminRepository.count() > 0) {
            logger.error("Super admin already exists");
            throw new RuntimeException("Super admin already exists");
        }
        admin.setRole(Admin.AdminRole.SUPER_ADMIN);
        
        // Check if the password is already encoded
        if (!isPasswordEncoded(admin.getPassword())) {
            logger.info("Encoding password for super admin");
            admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        } else {
            logger.info("Password already encoded, skipping encoding");
        }
        
        admin.setActive(true); // Super admin is active by default
        Admin savedAdmin = adminRepository.save(admin);
        logger.info("Super admin created successfully: {}", savedAdmin.getUsername());
        return savedAdmin;
    }

    public Admin activateAdmin(Long adminId) {
        logger.info("Attempting to activate admin with ID: {}", adminId);
        // Check if the current user is a super admin
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Admin currentAdmin = adminRepository.findByUsername(currentUsername)
                .orElseThrow(() -> {
                    logger.error("Not authenticated - no current admin found");
                    return new RuntimeException("Not authenticated");
                });

        if (currentAdmin.getRole() != Admin.AdminRole.SUPER_ADMIN) {
            logger.error("Unauthorized attempt to activate admin by non-super admin: {}", currentUsername);
            throw new RuntimeException("Only super admin can activate admins");
        }

        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> {
                    logger.error("Admin not found with ID: {}", adminId);
                    return new RuntimeException("Admin not found");
                });
        admin.setActive(true);
        Admin savedAdmin = adminRepository.save(admin);
        logger.info("Admin activated successfully: {}", savedAdmin.getUsername());
        return savedAdmin;
    }

    public Admin deactivateAdmin(Long adminId) {
        logger.info("Attempting to deactivate admin with ID: {}", adminId);
        // Check if the current user is a super admin
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Admin currentAdmin = adminRepository.findByUsername(currentUsername)
                .orElseThrow(() -> {
                    logger.error("Not authenticated - no current admin found");
                    return new RuntimeException("Not authenticated");
                });

        if (currentAdmin.getRole() != Admin.AdminRole.SUPER_ADMIN) {
            logger.error("Unauthorized attempt to deactivate admin by non-super admin: {}", currentUsername);
            throw new RuntimeException("Only super admin can deactivate admins");
        }

        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> {
                    logger.error("Admin not found with ID: {}", adminId);
                    return new RuntimeException("Admin not found");
                });
        admin.setActive(false);
        Admin savedAdmin = adminRepository.save(admin);
        logger.info("Admin deactivated successfully: {}", savedAdmin.getUsername());
        return savedAdmin;
    }

    public Admin updateAdminPassword(Long adminId, String newPassword) {
        logger.info("Attempting to update password for admin with ID: {}", adminId);
        
        // Check if the current user is a super admin
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Admin currentAdmin = adminRepository.findByUsername(currentUsername)
                .orElseThrow(() -> {
                    logger.error("Not authenticated - no current admin found");
                    return new RuntimeException("Not authenticated");
                });

        if (currentAdmin.getRole() != Admin.AdminRole.SUPER_ADMIN) {
            logger.error("Unauthorized attempt to update password by non-super admin: {}", currentUsername);
            throw new RuntimeException("Only super admin can update passwords");
        }

        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> {
                    logger.error("Admin not found with ID: {}", adminId);
                    return new RuntimeException("Admin not found");
                });
        
        // Always encode the new password
        admin.setPassword(passwordEncoder.encode(newPassword));
        Admin savedAdmin = adminRepository.save(admin);
        logger.info("Password updated successfully for admin: {}", savedAdmin.getUsername());
        return savedAdmin;
    }

    @Transactional
    public void updatePassword(Long adminId, String newPassword) {
        Admin admin = adminRepository.findById(adminId)
            .orElseThrow(() -> new RuntimeException("Admin not found"));
        
        admin.setPassword(passwordEncoder.encode(newPassword));
        adminRepository.save(admin);
    }
} 