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
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
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
    void getItemByIdOk() {
        ItemDto newItem = getItemDto(count);

        UserDto newUser = getUserDto(count);

        newUser = userController.add(newUser);

        ItemDto addedItem = itemController.add(newUser.getId(), newItem);

        ItemDto receivedItem = itemController.findById(newUser.getId(), addedItem.getId());

        assertEquals(addedItem.getId(), receivedItem.getId(), "Wrong id of item");
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
    void searchItemsOk() {
        UserDto newUser = getUserDto(count);
        newUser = userController.add(newUser);

        ItemDto newItem = getItemDto(count);
        ItemDto addedItem = itemController.add(newUser.getId(), newItem);

        List<ItemDto> items = itemController.search(newUser.getId(), newItem.getName());

        assertEquals(1, items.size(), "Wrong number of items");
        assertEquals(addedItem.getId(), items.getFirst().getId(), "Wrong id of item");
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
