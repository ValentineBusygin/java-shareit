package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exceptions.EmailAlreadyExistsException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.User;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class UserRepositoryDaoImpl implements UserRepositoryDao {
    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> emails = new HashSet<>();
    private Long counter = 1L;

    @Override
    public User add(User user) {
        checkEmail(user.getEmail());

        user.setId(counter++);
        users.put(user.getId(), user);
        emails.add(user.getEmail());

        return user;
    }

    @Override
    public User update(User user) {
        User oldUser = findById(user.getId());
        if (!user.getEmail().equals(oldUser.getEmail())) {
            checkEmail(user.getEmail());
            emails.remove(oldUser.getEmail());
            emails.add(user.getEmail());
        }
        users.put(user.getId(), user);

        return user;
    }

    @Override
    public User findById(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь c id = " + id + "не найден");
        }
        return users.get(id);
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void delete(Long id) {
        User user = findById(id);
        emails.remove(user.getEmail());
        users.remove(id);
    }

    private void checkEmail(String email) {
        if (emails.contains(email)) {
            throw new EmailAlreadyExistsException("Пользователь с таким email уже существует");
        }
    }
}
