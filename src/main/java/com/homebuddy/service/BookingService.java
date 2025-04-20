package com.homebuddy.service;

import com.homebuddy.model.Booking;
import com.homebuddy.model.BookingStatus;
import com.homebuddy.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;

    @Transactional
    public Booking createBooking(Booking booking) {
        booking.setStatus(BookingStatus.NOT_CALLED);
        return bookingRepository.save(booking);
    }

    public List<Booking> getAllBookings(String dateRange, String status) {
        BookingStatus bookingStatus = status.equals("all") ? null : BookingStatus.valueOf(status);
        LocalDateTime startDate = null;
        LocalDateTime endDate = LocalDateTime.now();

        switch (dateRange) {
            case "today":
                startDate = LocalDateTime.now().withHour(0).withMinute(0);
                break;
            case "week":
                startDate = LocalDateTime.now().minusWeeks(1);
                break;
            case "month":
                startDate = LocalDateTime.now().minusMonths(1);
                break;
            default:
                startDate = null; // For "all" option
        }

        return bookingRepository.findBookingsWithFilters(bookingStatus, startDate, endDate);
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    @Transactional
    public Booking updateBookingStatus(Long id, String status) {
        Booking booking = getBookingById(id);
        booking.setStatus(BookingStatus.valueOf(status));
        return bookingRepository.save(booking);
    }
} 