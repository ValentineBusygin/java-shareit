package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Booking {
    private Long id;
    private Long userId;
    private Long itemId;
    private LocalDateTime start;
    private LocalDateTime end;
    private BookingState status;
}
