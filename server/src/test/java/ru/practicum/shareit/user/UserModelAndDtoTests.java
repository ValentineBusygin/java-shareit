package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserModelAndDtoTests {

    @Test
    void testUserNoArgsConstructor() {
        User user = new User();
        user.setId(1L);
        user.setName("name");
        user.setEmail("email");

        assertEquals(1L, user.getId());
        assertEquals("name", user.getName());
        assertEquals("email", user.getEmail());
    }

    @Test
    void testUserAllArgsConstructor() {
        User user = new User(1L, "name", "email");

        assertEquals(1L, user.getId());
        assertEquals("name", user.getName());
        assertEquals("email", user.getEmail());
    }

    @Test
    void testUserBuilder() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("email")
                .build();

        assertEquals(1L, user.getId());
        assertEquals("name", user.getName());
        assertEquals("email", user.getEmail());
    }

    @Test
    void testUserDtoAllArgsConstructor() {
        UserDto userDto = new UserDto(1L, "name", "email");

        assertEquals(1L, userDto.getId());
        assertEquals("name", userDto.getName());
        assertEquals("email", userDto.getEmail());
    }

    @Test
    void testUserDtoBuilder() {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("name")
                .email("email")
                .build();

        assertEquals(1L, userDto.getId());
        assertEquals("name", userDto.getName());
        assertEquals("email", userDto.getEmail());
    }
}
