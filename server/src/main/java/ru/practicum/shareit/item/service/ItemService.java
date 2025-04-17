package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto add(Long ownerId, ItemDto itemDto);

    ItemDto update(Long ownerId, Long itemId, ItemDto itemDto);

    ItemDto findById(Long userId, Long itemId);

    List<ItemDto> findAll(Long userId);

    List<ItemDto> search(Long userId, String text);

    CommentDto addComment(Long userId, Long itemId, CommentDto commentDto);
}
