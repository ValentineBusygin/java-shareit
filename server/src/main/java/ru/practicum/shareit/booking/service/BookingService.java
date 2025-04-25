package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.List;

public interface BookingService {
    BookingOutDto create(BookingInDto bookingInDto, Long userId);

    BookingOutDto update(Long userId, Long bookingId, boolean approved);

    BookingOutDto getById(Long userId, Long bookingId);

    List<BookingOutDto> getAll0fUserByState(Long userId, BookingState state);

    List<BookingOutDto> getAll0fOwnerByState(Long userId, BookingState state);
}
