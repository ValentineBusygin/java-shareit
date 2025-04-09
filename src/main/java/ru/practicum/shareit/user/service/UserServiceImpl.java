package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto add(UserDto userDto) {
        return UserMapper.toUserDto(userRepository.save(UserMapper.toUser(userDto)));
    }

    @Override
    @Transactional
    public UserDto update(Long id, UserDto userDto) {
        User updatingUser = UserMapper.toUser(userDto);
        User existingUser = getUserById(id);

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

        return UserMapper.toUserDto(userRepository.save(updatedUser));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto findById(Long id) {
        return UserMapper.toUserDto(getUserById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        return userRepository.findAll().stream().map(UserMapper::toUserDto).toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    private User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Пользователь c ID " + id + " не найден")
        );
    }
}
