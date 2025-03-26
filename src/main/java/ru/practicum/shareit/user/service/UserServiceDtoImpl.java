package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserMapper;

import java.util.List;

@Service
@Primary
@RequiredArgsConstructor
public class UserServiceDtoImpl implements UserServiceDto {
    private final UserServiceDao userServiceDao;

    @Override
    public UserDto add(UserDto userDto) {
        return UserMapper.toUserDto(userServiceDao.add(UserMapper.toUser(userDto)));
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        User updatingUser = UserMapper.toUser(userDto);
        User existingUser = userServiceDao.findById(id);

        User updatedUser = new User();

        updatedUser.setId(id);

        if (updatingUser.getEmail() != null) {
            updatedUser.setEmail(updatingUser.getEmail());
        } else {
            updatedUser.setEmail(existingUser.getEmail());
        }

        if (updatingUser.getName() != null) {
            updatedUser.setName(updatingUser.getName());
        } else {
            updatedUser.setName(existingUser.getName());
        }

        return UserMapper.toUserDto(userServiceDao.update(updatedUser));
    }

    @Override
    public UserDto findById(Long id) {
        return UserMapper.toUserDto(userServiceDao.findById(id));
    }

    @Override
    public List<UserDto> findAll() {
        return userServiceDao.findAll().stream().map(UserMapper::toUserDto).toList();
    }

    @Override
    public void delete(Long id) {
        userServiceDao.delete(id);
    }
}
