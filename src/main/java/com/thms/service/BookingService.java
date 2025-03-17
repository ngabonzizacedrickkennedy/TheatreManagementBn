package com.thms.service;

import com.thms.dto.BookingDTO;
import com.thms.model.Booking;
import com.thms.model.Screening;
import com.thms.model.Seat;
import com.thms.model.User;
import com.thms.repository.BookingRepository;
import com.thms.repository.ScreeningRepository;
import com.thms.repository.SeatRepository;
import com.thms.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;

    public BookingService(BookingRepository bookingRepository, UserRepository userRepository,
                         ScreeningRepository screeningRepository, SeatRepository seatRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.screeningRepository = screeningRepository;
        this.seatRepository = seatRepository;
    }

    @Transactional
    public BookingDTO createBooking(Long screeningId, String username, List<String> selectedSeats, String paymentMethod) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new RuntimeException("Screening not found"));

        // Check if seats are available
        Set<String> bookedSeats = new HashSet<>(bookingRepository.findBookedSeatsByScreeningId(screeningId));
        for (String seat : selectedSeats) {
            if (bookedSeats.contains(seat)) {
                throw new RuntimeException("Seat " + seat + " is already booked");
            }
        }

        // Calculate total price
        double totalPrice = calculateTotalPrice(screeningId, selectedSeats);

        // Create booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setScreening(screening);
        booking.setBookingNumber(generateBookingNumber());
        booking.setBookingTime(LocalDateTime.now());
        booking.setTotalAmount(totalPrice);
        booking.setPaymentStatus(Booking.PaymentStatus.COMPLETED); // Assuming payment is done immediately
        booking.setBookedSeats(new HashSet<>(selectedSeats));

        Booking savedBooking = bookingRepository.save(booking);
        return convertToDTO(savedBooking);
    }

    @Transactional(readOnly = true)
    public List<BookingDTO> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<BookingDTO> getBookingById(Long id) {
        return bookingRepository.findById(id).map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Optional<BookingDTO> getBookingByNumber(String bookingNumber) {
        return bookingRepository.findByBookingNumber(bookingNumber).map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<BookingDTO> getBookingsByUser(Long userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingDTO> getBookingsByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return getBookingsByUser(user.getId());
    }

    @Transactional(readOnly = true)
    public Set<String> getBookedSeatsByScreeningId(Long screeningId) {
        return new HashSet<>(bookingRepository.findBookedSeatsByScreeningId(screeningId));
    }

    @Transactional
    public void cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        // Check if the screening is in the future
        if (booking.getScreening().getStartTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot cancel past bookings");
        }
        
        booking.setPaymentStatus(Booking.PaymentStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public double calculateTotalPrice(Long screeningId, List<String> selectedSeats) {
        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new RuntimeException("Screening not found"));
        
        double basePrice = screening.getBasePrice();
        double totalPrice = 0.0;
        
        // Get all seats from this theatre and screen
        Map<String, Seat> seatMap = seatRepository.findByTheatreIdAndScreenNumber(
                screening.getTheatre().getId(), screening.getScreenNumber()).stream()
                .collect(Collectors.toMap(
                    seat -> seat.getRowName() + seat.getSeatNumber(),
                    seat -> seat
                ));
        
        // Calculate price based on seat type
        for (String seatKey : selectedSeats) {
            Seat seat = seatMap.get(seatKey);
            if (seat != null) {
                totalPrice += basePrice * seat.getPriceMultiplier();
            } else {
                totalPrice += basePrice; // Default to base price if seat not found
            }
        }
        
        return totalPrice;
    }

    private String generateBookingNumber() {
        return "BK" + System.currentTimeMillis() + new Random().nextInt(1000);
    }

    private BookingDTO convertToDTO(Booking booking) {
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setBookingNumber(booking.getBookingNumber());
        dto.setUserId(booking.getUser().getId());
        dto.setUsername(booking.getUser().getUsername());
        dto.setUserEmail(booking.getUser().getEmail());
        dto.setScreeningId(booking.getScreening().getId());
        dto.setMovieTitle(booking.getScreening().getMovie().getTitle());
        dto.setMovieUrl(booking.getScreening().getMovie().getTrailerUrl());
        dto.setTheatreName(booking.getScreening().getTheatre().getName());
        dto.setScreeningTime(booking.getScreening().getStartTime());
        dto.setBookingTime(booking.getBookingTime());
        dto.setTotalAmount(booking.getTotalAmount());
        dto.setPaymentStatus(booking.getPaymentStatus());
        dto.setBookedSeats(booking.getBookedSeats());
        // Payment method isn't stored in the entity, so we don't set it here
        return dto;
    }
    @Transactional(readOnly = true)
    public List<BookingDTO> getBookingsByScreeningId(Long screeningId) {
        return bookingRepository.findByScreeningId(screeningId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}