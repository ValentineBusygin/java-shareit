package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceDtoImpl implements UserServiceDto {
    private final UserRepositoryDao userRepositoryDao;

    @Override
    public UserDto add(UserDto userDto) {
        return UserMapper.toUserDto(userRepositoryDao.add(UserMapper.toUser(userDto)));
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        User updatingUser = UserMapper.toUser(userDto);
        User existingUser = userRepositoryDao.findById(id);

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

        return UserMapper.toUserDto(userRepositoryDao.update(updatedUser));
    }

    @Override
    public UserDto findById(Long id) {
        return UserMapper.toUserDto(userRepositoryDao.findById(id));
    }

    @Override
    public List<UserDto> findAll() {
        return userRepositoryDao.findAll().stream().map(UserMapper::toUserDto).toList();
    }

    @Override
    public void delete(Long id) {
        userRepositoryDao.delete(id);
    }
}
