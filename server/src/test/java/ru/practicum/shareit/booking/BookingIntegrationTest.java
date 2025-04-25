package ru.practicum.shareit.booking;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.NotOwnerException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class BookingIntegrationTest {

    private final BookingController bookingController;

    private final UserController userController;

    private final ItemController itemController;

    private final EntityManager entityManager;

    private static Long count = 0L;

    @Test
    void createBookingOk() {
        UserDto newUser = getUserDto(count);
        newUser = userController.add(newUser);

        ItemDto newItem = getItemDto(count);
        ItemDto addedItem = itemController.add(newUser.getId(), newItem);

        UserDto requesterUser = getUserDto(count);
        requesterUser = userController.add(requesterUser);

        BookingInDto bookingInDto = BookingInDto.builder()
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusSeconds(2))
                .itemId(addedItem.getId())
                .build();

        BookingOutDto bookingDto = bookingController.add(requesterUser.getId(), bookingInDto);

        TypedQuery<Booking> query = entityManager.createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
        Booking addedBookingDb = query.setParameter("id", bookingDto.getId()).getSingleResult();

        assertNotNull(addedBookingDb);
        assertEquals(addedBookingDb.getId(), bookingDto.getId());
        assertEquals(addedBookingDb.getStart(), bookingDto.getStart());
        assertEquals(addedBookingDb.getEnd(), bookingDto.getEnd());
    }

    @Test
    void createBookingWrongItemErr() {
        BookingInDto bookingInDto = BookingInDto.builder()
                .itemId(1L)
                .build();

        assertThrows(NotFoundException.class, () -> bookingController.add(0L, bookingInDto));
    }

    @Test
    void createBookingWrongUserErr() {
        UserDto newUser = getUserDto(count);
        UserDto addedUser = userController.add(newUser);

        ItemDto newItem = getItemDto(count);
        ItemDto addedItem = itemController.add(addedUser.getId(), newItem);

        BookingInDto bookingInDto = BookingInDto.builder()
                .itemId(addedItem.getId())
                .build();

        assertThrows(NotFoundException.class, () -> bookingController.add(0L, bookingInDto));
    }

    @Test
    void createBookingValidationErr() {
        UserDto newUser = getUserDto(count);
        UserDto addedUser = userController.add(newUser);

        ItemDto newItem = getItemDto(count);
        ItemDto availableItem = itemController.add(addedUser.getId(), newItem);

        newItem.setAvailable(false);
        ItemDto unavailableItem = itemController.add(addedUser.getId(), newItem);

        newUser = getUserDto(count);
        UserDto requesterUser = userController.add(newUser);

        BookingInDto unavailableItemBookingInDto = BookingInDto.builder()
                .itemId(unavailableItem.getId())
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusSeconds(2))
                .build();

        assertThrows(ValidationException.class,
                () -> bookingController.add(requesterUser.getId(), unavailableItemBookingInDto));

        BookingInDto startBeforeEndBookingInDto = BookingInDto.builder()
                .itemId(availableItem.getId())
                .start(LocalDateTime.now().plusSeconds(4))
                .end(LocalDateTime.now().plusSeconds(2))
                .build();

        assertThrows(ValidationException.class,
                () -> bookingController.add(requesterUser.getId(), startBeforeEndBookingInDto));

        LocalDateTime dateTimeNow = LocalDateTime.now();
        BookingInDto startEqEndBookingInDto = BookingInDto.builder()
                .itemId(availableItem.getId())
                .start(dateTimeNow.plusSeconds(1))
                .end(dateTimeNow.plusSeconds(1))
                .build();

        assertThrows(ValidationException.class,
                () -> bookingController.add(requesterUser.getId(), startEqEndBookingInDto));

        BookingInDto endBeforeNowBookingInDto = BookingInDto.builder()
                .itemId(availableItem.getId())
                .start(LocalDateTime.now().minusSeconds(4))
                .end(LocalDateTime.now().minusSeconds(2))
                .build();

        assertThrows(ValidationException.class,
                () -> bookingController.add(requesterUser.getId(), endBeforeNowBookingInDto));

        BookingInDto endNowBookingInDto = BookingInDto.builder()
                .itemId(availableItem.getId())
                .start(LocalDateTime.now().minusSeconds(4))
                .end(LocalDateTime.now())
                .build();

        assertThrows(ValidationException.class,
                () -> bookingController.add(requesterUser.getId(), endNowBookingInDto));

        BookingInDto startBeforeNowBookingInDto = BookingInDto.builder()
                .itemId(availableItem.getId())
                .start(LocalDateTime.now().minusSeconds(4))
                .end(LocalDateTime.now().plusSeconds(2))
                .build();

        assertThrows(ValidationException.class,
                () -> bookingController.add(requesterUser.getId(), startBeforeNowBookingInDto));
    }

    @Test
    void approveBookingOk() throws InterruptedException {

        UserDto newUser = getUserDto(count);
        newUser = userController.add(newUser);

        ItemDto newItem = getItemDto(count);
        ItemDto addedItem = itemController.add(newUser.getId(), newItem);

        UserDto requesterUser = getUserDto(count);
        requesterUser = userController.add(requesterUser);

        BookingInDto bookingInDto = BookingInDto.builder()
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusSeconds(2))
                .itemId(addedItem.getId())
                .build();

        BookingOutDto bookingDto = bookingController.add(requesterUser.getId(), bookingInDto);

        sleep(2000);

        bookingController.approveBooking(newUser.getId(), bookingDto.getId(), true);

        bookingDto = bookingController.getBookingById(requesterUser.getId(), bookingDto.getId());

        assertEquals(BookingStatus.APPROVED, bookingDto.getStatus());
    }

    @Test
    void rejectBookingOk() throws InterruptedException {

        UserDto newUser = getUserDto(count);
        newUser = userController.add(newUser);

        ItemDto newItem = getItemDto(count);
        ItemDto addedItem = itemController.add(newUser.getId(), newItem);

        UserDto requesterUser = getUserDto(count);
        requesterUser = userController.add(requesterUser);

        BookingInDto bookingInDto = BookingInDto.builder()
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusSeconds(2))
                .itemId(addedItem.getId())
                .build();

        BookingOutDto bookingDto = bookingController.add(requesterUser.getId(), bookingInDto);

        sleep(2000);

        bookingController.approveBooking(newUser.getId(), bookingDto.getId(), false);

        bookingDto = bookingController.getBookingById(requesterUser.getId(), bookingDto.getId());

        assertEquals(BookingStatus.REJECTED, bookingDto.getStatus());
    }

    @Test
    void approveBookingWrongUserErr() {
        assertThrows(NotOwnerException.class, () -> {
            bookingController.approveBooking(0L, 0L, true);
        });
    }

    @Test
    void approveBookingWrongBookingErr() {
        UserDto newUser = getUserDto(count);
        UserDto addedUser = userController.add(newUser);

        assertThrows(NotFoundException.class, () -> {
            bookingController.approveBooking(addedUser.getId(), 0L, true);
        });
    }

    @Test
    void approveBookingNotOwnerErr() throws InterruptedException {
        UserDto newUser = getUserDto(count);
        UserDto addedUser = userController.add(newUser);

        ItemDto newItem = getItemDto(count);
        ItemDto addedItem = itemController.add(addedUser.getId(), newItem);

        newUser = getUserDto(count);
        UserDto requesterUser = userController.add(newUser);

        BookingInDto bookingInDto = BookingInDto.builder()
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusSeconds(2))
                .itemId(addedItem.getId())
                .build();

        BookingOutDto bookingDto = bookingController.add(requesterUser.getId(), bookingInDto);

        sleep(2000);

        assertThrows(NotOwnerException.class,
                () -> {
                    bookingController.approveBooking(requesterUser.getId(),
                            bookingDto.getId(), true);
                });
    }

    @Test
    void getBookingByIdOk() {
        UserDto newUser = getUserDto(count);
        UserDto addedUser = userController.add(newUser);

        ItemDto newItem = getItemDto(count);
        ItemDto addedItem = itemController.add(addedUser.getId(), newItem);

        newUser = getUserDto(count);
        UserDto requesterUser = userController.add(newUser);

        BookingInDto bookingInDto = BookingInDto.builder()
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusSeconds(2))
                .itemId(addedItem.getId())
                .build();

        BookingOutDto bookingDto = bookingController.add(requesterUser.getId(), bookingInDto);

        BookingOutDto receivedBookingDto = bookingController.getBookingById(addedUser.getId(), bookingDto.getId());
        assertEquals(bookingDto.getId(), receivedBookingDto.getId());

        receivedBookingDto = bookingController.getBookingById(requesterUser.getId(), bookingDto.getId());
        assertEquals(bookingDto.getId(), receivedBookingDto.getId());
    }

    @Test
    void getBookingByIdWrongUserErr() {
        assertThrows(NotFoundException.class, () -> {
            bookingController.getBookingById(0L, 0L);
        });
    }

    @Test
    void getBookingByIdWrongBookingErr() {
        UserDto newUser = getUserDto(count);
        UserDto addedUser = userController.add(newUser);
        assertThrows(NotFoundException.class, () -> {
            bookingController.getBookingById(addedUser.getId(), 0L);
        });
    }

    @Test
    void getBookingByIdNotOwnerOrBookerErr() {

        UserDto newUser = getUserDto(count);
        UserDto addedUser = userController.add(newUser);

        ItemDto newItem = getItemDto(count);
        ItemDto addedItem = itemController.add(addedUser.getId(), newItem);

        newUser = getUserDto(count);
        UserDto requesterUser = userController.add(newUser);

        BookingInDto bookingInDto = BookingInDto.builder()
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusSeconds(2))
                .itemId(addedItem.getId())
                .build();

        BookingOutDto bookingDto = bookingController.add(requesterUser.getId(), bookingInDto);

        newUser = getUserDto(count);
        UserDto otherUser = userController.add(newUser);

        assertThrows(NotOwnerException.class, () -> {
            bookingController.getBookingById(otherUser.getId(), bookingDto.getId());
        });

    }

    @Test
    void getAllBookingsUserOk() {
        UserDto newUser = getUserDto(count);
        newUser = userController.add(newUser);

        ItemDto newItem = getItemDto(count);
        ItemDto addedItem = itemController.add(newUser.getId(), newItem);

        UserDto requesterUser = getUserDto(count);
        requesterUser = userController.add(requesterUser);

        BookingInDto bookingInDto = BookingInDto.builder()
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusSeconds(2))
                .itemId(addedItem.getId())
                .build();

        BookingOutDto bookingDto = bookingController.add(requesterUser.getId(), bookingInDto);

        UserDto secondRequesterUser = getUserDto(count);
        secondRequesterUser = userController.add(secondRequesterUser);
        BookingInDto secondBookingInDto = BookingInDto.builder()
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusSeconds(2))
                .itemId(addedItem.getId())
                .build();

        BookingOutDto secondBookingDto = bookingController.add(secondRequesterUser.getId(), secondBookingInDto);

        List<BookingOutDto> bookingDtos = bookingController.getAllBookingsByUserIdAndState(requesterUser.getId(), BookingState.ALL);
        assertEquals(1, bookingDtos.size());

        bookingDtos = bookingController.getAllBookingsByUserIdAndState(requesterUser.getId(), BookingState.REJECTED);
        assertEquals(0, bookingDtos.size());


        bookingDtos = bookingController.getAllBookingsByUserIdAndState(requesterUser.getId(), BookingState.WAITING);
        assertEquals(1, bookingDtos.size());

        bookingDtos = bookingController.getAllBookingsByUserIdAndState(requesterUser.getId(), BookingState.PAST);
        assertEquals(0, bookingDtos.size());

        bookingDtos = bookingController.getAllBookingsByUserIdAndState(requesterUser.getId(), BookingState.CURRENT);
        assertEquals(0, bookingDtos.size());

        bookingDtos = bookingController.getAllBookingsByUserIdAndState(requesterUser.getId(), BookingState.FUTURE);
        assertEquals(1, bookingDtos.size());
    }

    @Test
    void getAllBookingsUserWrongUserErr() {
        assertThrows(NotFoundException.class, () -> {
            bookingController.getAllBookingsByUserIdAndState(0L, BookingState.ALL);
        });
    }

    @Test
    void getAllBookingsOwnerOk() {
        UserDto newUser = getUserDto(count);
        newUser = userController.add(newUser);

        ItemDto newItem = getItemDto(count);
        ItemDto addedItem = itemController.add(newUser.getId(), newItem);

        UserDto requesterUser = getUserDto(count);
        requesterUser = userController.add(requesterUser);

        BookingInDto bookingInDto = BookingInDto.builder()
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusSeconds(2))
                .itemId(addedItem.getId())
                .build();

        BookingOutDto bookingDto = bookingController.add(requesterUser.getId(), bookingInDto);

        UserDto secondRequesterUser = getUserDto(count);
        secondRequesterUser = userController.add(secondRequesterUser);
        BookingInDto secondBookingInDto = BookingInDto.builder()
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusSeconds(2))
                .itemId(addedItem.getId())
                .build();

        BookingOutDto secondBookingDto = bookingController.add(secondRequesterUser.getId(), secondBookingInDto);

        List<BookingOutDto> bookingDtos = bookingController.getAllBookingsByOwnerIdAndState(newUser.getId(), BookingState.ALL);
        assertEquals(2, bookingDtos.size());

        bookingDtos = bookingController.getAllBookingsByOwnerIdAndState(newUser.getId(), BookingState.REJECTED);
        assertEquals(0, bookingDtos.size());

        bookingDtos = bookingController.getAllBookingsByOwnerIdAndState(newUser.getId(), BookingState.WAITING);
        assertEquals(2, bookingDtos.size());

        bookingDtos = bookingController.getAllBookingsByOwnerIdAndState(requesterUser.getId(), BookingState.PAST);
        assertEquals(0, bookingDtos.size());

        bookingDtos = bookingController.getAllBookingsByOwnerIdAndState(requesterUser.getId(), BookingState.CURRENT);
        assertEquals(0, bookingDtos.size());

        bookingDtos = bookingController.getAllBookingsByOwnerIdAndState(requesterUser.getId(), BookingState.FUTURE);
        assertEquals(0, bookingDtos.size());
    }

    @Test
    void getAllBookingsOwnerWrongUserErr() {
        assertThrows(NotFoundException.class, () -> {
            bookingController.getAllBookingsByOwnerIdAndState(0L, BookingState.ALL);
        });
    }

    private ItemDto getItemDto(Long counter) {
        count++;

        return new ItemDto(counter,
                "testItemName-" + counter,
                "testItemDescription" + counter,
                true,
                null,
                null,
                null,
                null);
    }

    private UserDto getUserDto(Long counter) {
        count++;

        return new UserDto(counter,
                "testUserName-" + counter,
                "testUserEmail-" + counter);
    }
}
