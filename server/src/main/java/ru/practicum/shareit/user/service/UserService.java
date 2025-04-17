package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto add(UserDto userDto);

    UserDto update(Long id, UserDto userDto);

    UserDto findById(Long id);

    List<UserDto> findAll();

    void delete(Long id);
}
