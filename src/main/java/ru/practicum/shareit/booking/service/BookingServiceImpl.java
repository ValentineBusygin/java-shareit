package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.NotOwnerException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    private static final String ITEM_NOT_FOUND = "Предмет с id %d не найден";
    private static final String USER_NOT_FOUND = "Пользователь с id %d не найден";
    private static final String BOOKING_NOT_FOUND = "Бронирование с id %d не найдено";

    @Override
    @Transactional(rollbackFor = NotFoundException.class)
    public BookingOutDto create(BookingInDto bookingInDto, Long userId) {
        Item item = itemRepository.findById(bookingInDto.getItemId()).orElseThrow(
                () -> new NotFoundException(String.format(ITEM_NOT_FOUND, bookingInDto.getItemId())));

        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException(String.format(USER_NOT_FOUND, userId)));

        Booking booking = Booking.builder()
                .start(bookingInDto.getStart())
                .end(bookingInDto.getEnd())
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();

        validate(booking);

        bookingRepository.save(booking);

        return BookingMapper.toBookingOutDto(booking);
    }

    @Override
    @Transactional(rollbackFor = {NotFoundException.class, NotOwnerException.class})
    public BookingOutDto update(Long userId, Long bookingId, boolean approved) {
        userRepository.findById(userId).orElseThrow(
                () -> new NotOwnerException(String.format(USER_NOT_FOUND, userId)));

        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new NotFoundException(String.format(BOOKING_NOT_FOUND, bookingId)));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotOwnerException("Пользователь не является владельцем предмета и не может управлять бронированием");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        bookingRepository.save(booking);

        return BookingMapper.toBookingOutDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingOutDto getById(Long userId, Long bookingId) {
        userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException(String.format(USER_NOT_FOUND, userId)));

        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new NotFoundException(String.format(BOOKING_NOT_FOUND, bookingId)));

        if (!booking.getItem().getOwner().getId().equals(userId)
                && !booking.getBooker().getId().equals(userId)) {
            throw new NotOwnerException("Пользователь не является владельцем предмета или автором бронирования");
        }

        return BookingMapper.toBookingOutDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingOutDto> getAll0fUserByState(Long userId, BookingState state) {
        userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException(String.format(USER_NOT_FOUND, userId)));

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByBookerIdOrderByStartDesc(userId);
            case WAITING -> bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingState.WAITING);
            case REJECTED -> bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingState.REJECTED);
            case PAST -> bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now());
            case CURRENT ->
                    bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, LocalDateTime.now(), LocalDateTime.now());
            case FUTURE -> bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now());
        };

        return bookings
                .stream()
                .map(BookingMapper::toBookingOutDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingOutDto> getAll0fOwnerByState(Long userId, BookingState state) {
        userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException(String.format(USER_NOT_FOUND, userId)));

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByItemOwnerIdOrderByStartDesc(userId);
            case WAITING -> bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingState.WAITING);
            case REJECTED ->
                    bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingState.REJECTED);
            case PAST -> bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now());
            case CURRENT ->
                    bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, LocalDateTime.now(), LocalDateTime.now());
            case FUTURE ->
                    bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now());
        };

        return bookings
                .stream()
                .map(BookingMapper::toBookingOutDto)
                .toList();
    }

    private void validate(Booking booking) {
        if (!booking.getItem().getAvailable()) {
            throw new ValidationException("Предмет не доступен для бронирования");
        }
        if (booking.getEnd().isBefore(booking.getStart())) {
            throw new ValidationException("Дата окончания бронирования не может быть раньше даты начала бронирования");
        }
        if (booking.getEnd().isEqual(booking.getStart())) {
            throw new ValidationException("Дата окончания бронирования не может быть равна дате начала бронирования");
        }
        if (booking.getEnd().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата окончания бронирования не может быть раньше текущей даты");
        }
        if (booking.getEnd().isEqual(LocalDateTime.now())) {
            throw new ValidationException("Дата окончания бронирования не может быть равна текущей дате");
        }
        if (booking.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата начала бронирования не может быть раньше текущей даты");
        }
    }
}
