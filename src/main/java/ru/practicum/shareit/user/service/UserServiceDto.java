package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.UserDto;

import java.util.List;

public interface UserServiceDto {
    UserDto add(UserDto userDto);

    UserDto update(Long id, UserDto userDto);

    UserDto findById(Long id);

    List<UserDto> findAll();

    void delete(Long id);
}
