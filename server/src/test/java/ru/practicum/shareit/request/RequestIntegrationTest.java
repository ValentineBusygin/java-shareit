package ru.practicum.shareit.request;

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
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class RequestIntegrationTest {

    private final UserController userController;

    private final ItemRequestController itemRequestController;

    private final EntityManager entityManager;

    private static Long count = 0L;

    @Test
    void createRequestOk() {
        UserDto userDto = getUserDto(count);
        userDto = userController.add(userDto);

        ItemRequestInDto itemRequestInDto = getItemRequestInDto(count);

        ItemRequestDto itemRequestDto = itemRequestController.createRequest(userDto.getId(), itemRequestInDto);

        assertNotNull(itemRequestDto.getId());
        assertEquals(itemRequestInDto.getDescription(), itemRequestDto.getDescription());

        TypedQuery<ItemRequest> query = entityManager.createQuery("SELECT ir FROM ItemRequest ir WHERE ir.id = :id", ItemRequest.class);
        ItemRequest addedItemRequestDb = query.setParameter("id", itemRequestDto.getId()).getSingleResult();

        assertNotNull(addedItemRequestDb.getId());
        assertEquals(itemRequestInDto.getDescription(), addedItemRequestDb.getDescription());
    }

    @Test
    void getRequestByIdOk() {
        UserDto userDto = getUserDto(count);
        userDto = userController.add(userDto);

        ItemRequestInDto itemRequestInDto = getItemRequestInDto(count);

        ItemRequestDto itemRequestDto = itemRequestController.createRequest(userDto.getId(), itemRequestInDto);
        Long requestId = itemRequestDto.getId();

        itemRequestDto = itemRequestController.getRequestById(userDto.getId(), requestId);

        assertNotNull(itemRequestDto.getId());
        assertEquals(requestId, itemRequestDto.getId());
        assertEquals(itemRequestInDto.getDescription(), itemRequestDto.getDescription());
    }

    @Test
    void getAllRequestOk() {
        UserDto userDto = getUserDto(count);
        userDto = userController.add(userDto);

        ItemRequestInDto itemRequestInDto = getItemRequestInDto(count);
        ItemRequestDto itemRequestDto = itemRequestController.createRequest(userDto.getId(), itemRequestInDto);

        assertEquals(1, itemRequestController.getAllRequests(userDto.getId()).size());
        assertEquals(itemRequestDto.getDescription(), itemRequestController.getAllRequests(userDto.getId()).getFirst().getDescription());

        itemRequestInDto = getItemRequestInDto(count);
        itemRequestDto = itemRequestController.createRequest(userDto.getId(), itemRequestInDto);
        assertEquals(2, itemRequestController.getAllRequests(userDto.getId()).size());
    }

    @Test
    void getAllRequestByUserIdOk() {
        UserDto userDto = getUserDto(count);
        userDto = userController.add(userDto);

        ItemRequestInDto itemRequestInDto = getItemRequestInDto(count);
        ItemRequestDto itemRequestDto = itemRequestController.createRequest(userDto.getId(), itemRequestInDto);

        assertEquals(1, itemRequestController.getRequests(userDto.getId()).size());
        assertEquals(itemRequestDto.getDescription(), itemRequestController.getRequests(userDto.getId()).getFirst().getDescription());

        UserDto secondUserDto = getUserDto(count);
        secondUserDto = userController.add(secondUserDto);
        itemRequestInDto = getItemRequestInDto(count);
        itemRequestController.createRequest(secondUserDto.getId(), itemRequestInDto);

        assertEquals(1, itemRequestController.getRequests(userDto.getId()).size());
    }

    private UserDto getUserDto(Long counter) {
        count++;

        return new UserDto(counter,
                "testUserName-" + counter,
                "testUserEmail-" + counter);
    }

    private ItemRequestInDto getItemRequestInDto(Long counter) {
        count++;

        return ItemRequestInDto.builder()
                .description("ItemRequestDescription-" + counter)
                .build();
    }

}
