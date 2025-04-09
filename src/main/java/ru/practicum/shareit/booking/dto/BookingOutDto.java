package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class BookingOutDto {
    private Long id;
    private UserDto booker;
    private ItemDto item;
    private LocalDateTime start;
    private LocalDateTime end;
    private BookingStatus status;
}
