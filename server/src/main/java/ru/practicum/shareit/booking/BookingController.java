package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    public static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final BookingService bookingService;

    @PostMapping
    public BookingOutDto add(@RequestHeader(USER_ID_HEADER) Long userId,
                             @RequestBody BookingInDto bookingInDto) {

        return bookingService.create(bookingInDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingOutDto approveBooking(@RequestHeader(USER_ID_HEADER) Long userId,
                                        @PathVariable Long bookingId,
                                        @RequestParam(name = "approved") Boolean approved) {
        return bookingService.update(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingOutDto getBookingById(@RequestHeader(USER_ID_HEADER) Long userId,
                                        @PathVariable Long bookingId) {
        return bookingService.getById(userId, bookingId);
    }

    @GetMapping()
    public List<BookingOutDto> getAllBookingsByUserIdAndState(@RequestHeader(USER_ID_HEADER) Long userId,
                                                              @RequestParam(name = "state", defaultValue = "ALL") BookingState state) {
        return bookingService.getAll0fUserByState(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingOutDto> getAllBookingsByOwnerIdAndState(@RequestHeader(USER_ID_HEADER) Long userId,
                                                               @RequestParam(name = "state", defaultValue = "ALL") BookingState state) {
        return bookingService.getAll0fOwnerByState(userId, state);
    }
}
