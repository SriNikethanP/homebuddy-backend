package com.homebuddy.repository;

import com.homebuddy.model.Booking;
import com.homebuddy.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b FROM Booking b WHERE " +
           "(:status IS NULL OR b.status = :status) AND " +
           "(:startDate IS NULL OR b.preferredDateTime >= :startDate) AND " +
           "(:endDate IS NULL OR b.preferredDateTime <= :endDate) " +
           "ORDER BY b.preferredDateTime DESC")
    List<Booking> findBookingsWithFilters(
        @Param("status") BookingStatus status,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
} 