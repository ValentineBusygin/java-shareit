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
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest(classes = ShareItServer.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class BookingIntegrationTest {

    private final BookingController bookingController;

    private final UserController userController;

    private final ItemController itemController;

    private final EntityManager entityManager;

    private Long count = 0L;

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
