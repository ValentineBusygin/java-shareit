package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.User;

import java.util.List;

public interface UserRepositoryDao {
    User add(User user);

    User update(User user);

    User findById(Long id);

    List<User> findAll();

    void delete(Long id);
}
