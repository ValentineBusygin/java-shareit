package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.NotOwnerException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserRepositoryDao;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemServiceDtoImpl implements ItemServiceDto {

    private final ItemRepositoryDao itemRepository;
    private final UserRepositoryDao userService;

    @Override
    public ItemDto add(Long ownerId, ItemDto itemDto) {
        User owner = userService.findById(ownerId);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwnerId(owner.getId());

        return ItemMapper.toItemDto(itemRepository.add(item));
    }

    @Override
    public ItemDto update(Long ownerId, Long itemId, ItemDto itemDto) {
        User owner = userService.findById(ownerId);
        Optional<Item> oItem = itemRepository.findById(itemId);
        if (oItem.isPresent()) {
            Item itemInStorage = oItem.get();
            if (!itemInStorage.getOwnerId().equals(owner.getId())) {
                throw new NotOwnerException(
                        String.format("Пользователь с id %s не является владельцем вещи с id %s", ownerId, itemId));
            }

            Item itemForUpdate = ItemMapper.toItem(itemDto);

            if (!Objects.isNull(itemForUpdate.getAvailable())) {
                itemInStorage.setAvailable(itemForUpdate.getAvailable());
            }

            if (!Objects.isNull(itemForUpdate.getDescription())) {
                itemInStorage.setDescription(itemForUpdate.getDescription());
            }

            if (!Objects.isNull(itemForUpdate.getName())) {
                itemInStorage.setName(itemForUpdate.getName());
            }

            itemInStorage.setRequestId(itemForUpdate.getRequestId());

            return ItemMapper.toItemDto(itemRepository.update(itemInStorage));
        }

        return itemDto;
    }

    @Override
    public ItemDto findById(Long userId, Long itemId) {
        userService.findById(userId);
        Item item = itemRepository.findById(itemId).orElseThrow(() ->
            new NotFoundException(
                    String.format("Вещь с id = %s не найдена у пользователя с id = %s", itemId, userId)));

        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> findAll(Long userId) {
        userService.findById(userId);
        List<Item> items = itemRepository.findAll(userId);

        return items.stream().map(ItemMapper::toItemDto).toList();
    }

    @Override
    public List<ItemDto> search(Long userId, String text) {
        userService.findById(userId);
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        List<Item> items = itemRepository.search(text);

        return items.stream().map(ItemMapper::toItemDto).toList();
    }
}
