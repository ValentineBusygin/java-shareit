package ru.practicum.shareit.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class UserIntegrationTest {

    private final UserController userController;

    private final EntityManager entityManager;

    private static Long count = 0L;

    @Test
    void addUserOk() {
        UserDto newUserDto = createUserDto(count);

        userController.add(newUserDto);

        TypedQuery<User> query = entityManager.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class);
        User newUser = query.setParameter("email", newUserDto.getEmail()).getSingleResult();

        assertNotNull(newUser.getId());
        assertEquals(newUser.getName(), newUserDto.getName(), "Wrong name of user");
        assertEquals(newUser.getEmail(), newUserDto.getEmail(), "Wrong email of user");
    }

    @Test
    void addUserEmailExistErr() {
        UserDto newUserDto = createUserDto(count);

        userController.add(newUserDto);

        assertThrows(DataIntegrityViolationException.class, () -> userController.add(newUserDto));
    }

    @Test
    void findByIdOk() {
        UserDto newUserDto = createUserDto(count);

        userController.add(newUserDto);

        TypedQuery<User> query = entityManager.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class);
        User newUser = query.setParameter("email", newUserDto.getEmail()).getSingleResult();

        UserDto foundUser = userController.findById(newUser.getId());
        assertEquals(newUser.getId(), foundUser.getId());
        assertEquals(newUser.getName(), foundUser.getName());
        assertEquals(newUser.getEmail(), foundUser.getEmail());
    }

    @Test
    void findByIdNotFoundErr() {
        assertThrows(NotFoundException.class, () -> userController.findById(0L));
    }

    @Test
    void findAllOk() {
        UserDto newUserDto = createUserDto(count);
        newUserDto = userController.add(newUserDto);

        UserDto newUserDto2 = createUserDto(count);
        newUserDto2 = userController.add(newUserDto2);

        List<UserDto> users = userController.findAll();
        assertEquals(users.size(), 2);
    }

    @Test
    void updateUserOk() {
        UserDto newUserDto = createUserDto(count);
        newUserDto = userController.add(newUserDto);

        TypedQuery<User> query = entityManager.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class);
        User newUser = query.setParameter("email", newUserDto.getEmail()).getSingleResult();

        assertEquals(newUser.getId(), newUserDto.getId());
        assertEquals(newUser.getName(), newUserDto.getName());
        assertEquals(newUser.getEmail(), newUserDto.getEmail());

        newUser.setName("UpdatedName");

        userController.update(newUser.getId(), UserMapper.toUserDto(newUser));
        User updatedUser = query.setParameter("email", newUserDto.getEmail()).getSingleResult();

        assertEquals(updatedUser.getId(), newUser.getId());
        assertEquals(updatedUser.getName(), newUser.getName());
        assertEquals(updatedUser.getEmail(), newUser.getEmail());
    }

    @Test
    void updateUserNoNameAndEmailOk() {
        UserDto newUserDto = createUserDto(count);
        newUserDto = userController.add(newUserDto);

        Long userId = newUserDto.getId();
        UserDto emptyUserDto = UserDto.builder()
                .id(userId)
                .build();

        userController.update(userId, emptyUserDto);

        TypedQuery<User> query = entityManager.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class);
        User newUser = query.setParameter("email", newUserDto.getEmail()).getSingleResult();

        assertEquals(newUser.getId(), newUserDto.getId());
        assertEquals(newUser.getName(), newUserDto.getName());
        assertEquals(newUser.getEmail(), newUserDto.getEmail());
    }

    @Test
    void deleteUserOk() {
        UserDto newUserDto = createUserDto(count);

        userController.add(newUserDto);

        TypedQuery<User> query = entityManager.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class);
        User newUser = query.setParameter("email", newUserDto.getEmail()).getSingleResult();

        userController.delete(newUser.getId());

        assertThrows(NoResultException.class, () -> query.setParameter("email", newUserDto.getEmail()).getSingleResult());
    }

    private UserDto createUserDto(Long counter) {
        count++;
        return new UserDto(null, "Name-" + counter, "test-" + counter + "@mail.ru");
    }
}
