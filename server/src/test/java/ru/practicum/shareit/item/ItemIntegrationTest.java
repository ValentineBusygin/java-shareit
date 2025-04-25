package ru.practicum.shareit.item;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.NotOwnerException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
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
public class ItemIntegrationTest {

    private final ItemController itemController;

    private final UserController userController;

    private final BookingController bookingController;

    private final EntityManager entityManager;

    private static Long count = 0L;

    @Test
    void addItemOk() {
        ItemDto newItem = getItemDto(count);

        UserDto newUser = getUserDto(count);

        newUser = userController.add(newUser);

        ItemDto addedItem = itemController.add(newUser.getId(), newItem);

        TypedQuery<Item> query = entityManager.createQuery("SELECT i FROM Item i WHERE i.id = :id", Item.class);
        Item addedItemDb = query.setParameter("id", addedItem.getId()).getSingleResult();

        assertNotNull(addedItemDb.getId());
        assertEquals(addedItemDb.getName(), newItem.getName(), "Wrong name of item");
        assertEquals(addedItemDb.getDescription(), newItem.getDescription(), "Wrong description of item");
    }

    @Test
    void addItemUserErr() {
        ItemDto newItem = getItemDto(count);

        assertThrows(NotFoundException.class, () -> {
            itemController.add(0L, newItem);
        });
    }

    @Test
    void updateItemOk() {
        ItemDto newItem = getItemDto(count);

        UserDto newUser = getUserDto(count);

        newUser = userController.add(newUser);

        ItemDto addedItem = itemController.add(newUser.getId(), newItem);

        newItem.setName("updatedName");
        newItem.setDescription("updatedDescription");

        ItemDto updatedItem = itemController.update(newUser.getId(), addedItem.getId(), newItem);

        assertEquals(updatedItem.getName(), newItem.getName(), "Wrong name of item");
        assertEquals(updatedItem.getDescription(), newItem.getDescription(), "Wrong description of item");
    }

    @Test
    void updateItemUserErr() {
        ItemDto newItem = getItemDto(count);

        UserDto newUser = getUserDto(count);

        newUser = userController.add(newUser);

        ItemDto addedItem = itemController.add(newUser.getId(), newItem);

        newItem.setName("updatedName");

        assertThrows(NotFoundException.class,
                () -> {
                    itemController.update(0L, addedItem.getId(), newItem);
                });
    }

    @Test
    void updateItemOwnerErr() {
        ItemDto newItem = getItemDto(count);

        UserDto firstUserDto = getUserDto(count);
        UserDto firstUser = userController.add(firstUserDto);

        ItemDto addedItem = itemController.add(firstUser.getId(), newItem);

        UserDto secondUserDto = getUserDto(count);
        UserDto secondUser = userController.add(secondUserDto);

        assertThrows(NotOwnerException.class,
                () -> {
                    itemController.update(secondUser.getId(), addedItem.getId(), newItem);
                });
    }

    @Test
    void updateItemNotExistOk() {
        ItemDto newItem = getItemDto(count);

        UserDto newUser = getUserDto(count);

        newUser = userController.add(newUser);

        ItemDto addedItem = itemController.add(newUser.getId(), newItem);

        newItem.setName("updatedName");
        newItem.setDescription("updatedDescription");

        ItemDto updatedItem = itemController.update(newUser.getId(), 0L, addedItem);

        assertEquals(updatedItem.getName(), addedItem.getName(), "Wrong name of item");
        assertEquals(updatedItem.getDescription(), addedItem.getDescription(), "Wrong description of item");
        assertEquals(updatedItem.getId(), addedItem.getId(), "Wrong id of item");
    }

    @Test
    void updateItemEmptyFieldsOk() {
        UserDto newUser = getUserDto(count);
        newUser = userController.add(newUser);

        ItemDto newItem = getItemDto(count);
        ItemDto addedItem = itemController.add(newUser.getId(), newItem);

        newItem.setName(null);
        newItem.setDescription(null);
        newItem.setAvailable(null);

        ItemDto updatedItem = itemController.update(newUser.getId(), addedItem.getId(), newItem);

        assertEquals(updatedItem.getName(), addedItem.getName(), "Wrong name of item");
        assertEquals(updatedItem.getDescription(), addedItem.getDescription(), "Wrong description of item");
    }

    @Test
    void getItemByIdOk() {
        ItemDto newItem = getItemDto(count);

        UserDto newUser = getUserDto(count);

        newUser = userController.add(newUser);

        ItemDto addedItem = itemController.add(newUser.getId(), newItem);

        ItemDto receivedItem = itemController.findById(newUser.getId(), addedItem.getId());

        assertEquals(addedItem.getId(), receivedItem.getId(), "Wrong id of item");
    }

    @Test
    void getItemByIdUserNotExistErr() {
        assertThrows(NotFoundException.class,
                () -> {
                    itemController.findById(0L, 0L);
                });
    }

    @Test
    void getItemByIdItemNotExistErr() {
        UserDto newUserDto = getUserDto(count);
        UserDto newUser = userController.add(newUserDto);

        assertThrows(NotFoundException.class,
                () -> {
                    itemController.findById(newUser.getId(), 0L);
                });
    }

    @Test
    void getItemByIdUserErr() {
        ItemDto newItem = getItemDto(count);
    }

    @Test
    void findAllByUserOk() {
        UserDto newUser = getUserDto(count);
        newUser = userController.add(newUser);

        UserDto newUser2 = getUserDto(count);
        newUser2 = userController.add(newUser2);

        ItemDto newItem = getItemDto(count);
        ItemDto addedItem = itemController.add(newUser.getId(), newItem);

        ItemDto newItem2 = getItemDto(count);
        ItemDto addedItem2 = itemController.add(newUser.getId(), newItem2);

        ItemDto newItem3 = getItemDto(count);
        ItemDto addedItem3 = itemController.add(newUser2.getId(), newItem3);

        List<ItemDto> items = itemController.findAll(newUser.getId());

        assertEquals(2, items.size(), "Wrong number of items");
        assertEquals(addedItem.getId(), items.getFirst().getId(), "Wrong id of item");

        items = itemController.findAll(newUser2.getId());
        assertEquals(1, items.size(), "Wrong number of items");
        assertEquals(addedItem3.getId(), items.getFirst().getId(), "Wrong id of item");
    }

    @Test
    void findAllByUserNotExistErr() {
        assertThrows(NotFoundException.class,
                () -> {
                    itemController.findAll(0L);
                });
    }

    @Test
    void addCommentOk() throws InterruptedException {
        UserDto newUser = getUserDto(count);
        newUser = userController.add(newUser);

        ItemDto newItem = getItemDto(count);
        ItemDto addedItem = itemController.add(newUser.getId(), newItem);

        CommentDto newComment = getCommentDto(count);

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

        CommentDto addedComment = itemController.addComment(addedItem.getId(), requesterUser.getId(), newComment);

        assertEquals(addedComment.getText(), newComment.getText(), "Wrong text of comment");
    }

    @Test
    void itemBookingsExtendingOk() throws InterruptedException {
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

        bookingInDto = BookingInDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(addedItem.getId())
                .build();

        BookingOutDto secondBookingDto = bookingController.add(requesterUser.getId(), bookingInDto);

        sleep(2000);

        bookingController.approveBooking(newUser.getId(), bookingDto.getId(), true);

        ItemDto receivedItem = itemController.findById(requesterUser.getId(), addedItem.getId());
        assertNull(receivedItem.getNextBooking(), "Next booking available not for owner");
        assertNull(receivedItem.getLastBooking(), "Last booking available not for owner");

        receivedItem = itemController.findById(newUser.getId(), addedItem.getId());
        assertNotNull(receivedItem.getNextBooking(), "Next booking not available for owner");
        assertNotNull(receivedItem.getLastBooking(), "Last booking not available for owner");

        List<ItemDto> items = itemController.findAll(newUser.getId());
        assertNotNull(items.getFirst().getNextBooking(), "Next booking not available for owner");
        assertNotNull(items.getFirst().getLastBooking(), "Last booking not available for owner");
    }

    @Test
    void addCommentUserNotExistErr() {
        CommentDto newComment = getCommentDto(count);

        assertThrows(NotFoundException.class,
                () -> {
                    itemController.addComment(0L, 0L, newComment);
                });
    }

    @Test
    void addCommentItemNotExistErr() {
        UserDto newUserDto = getUserDto(count);
        UserDto newUser = userController.add(newUserDto);

        CommentDto newComment = getCommentDto(count);

        assertThrows(NotFoundException.class,
                () -> {
                    itemController.addComment(0L, newUser.getId(), newComment);
                });
    }

    @Test
    void addCommentBookingNotExistErr() {
        UserDto newUserDto = getUserDto(count);
        UserDto addedUserDto = userController.add(newUserDto);

        ItemDto newItemDto = getItemDto(count);
        ItemDto addedItem = itemController.add(addedUserDto.getId(), newItemDto);

        CommentDto newComment = getCommentDto(count);

        assertThrows(NotOwnerException.class,
                () -> {
                    itemController.addComment(addedItem.getId(), addedUserDto.getId(), newComment);
                });
    }

    @Test
    void searchItemsOk() {
        UserDto newUser = getUserDto(count);
        newUser = userController.add(newUser);

        ItemDto newItem = getItemDto(count);
        ItemDto addedItem = itemController.add(newUser.getId(), newItem);

        List<ItemDto> items = itemController.search(newUser.getId(), newItem.getName());

        assertEquals(1, items.size(), "Wrong number of items");
        assertEquals(addedItem.getId(), items.getFirst().getId(), "Wrong id of item");
    }

    @Test
    void searchItemsUserNotExistErr() {
        assertThrows(NotFoundException.class,
                () -> {
                    itemController.search(0L, "");
                });
    }

    @Test
    void searchItemsEmptyTextOk() {
        UserDto newUser = getUserDto(count);
        newUser = userController.add(newUser);

        List<ItemDto> items = itemController.search(newUser.getId(), "");
        assertEquals(0, items.size(), "Wrong number of items");
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

    private CommentDto getCommentDto(Long counter) {
        count++;

        return CommentDto.builder()
                .id(counter)
                .text("testCommentText-" + counter)
                .build();
    }
}
