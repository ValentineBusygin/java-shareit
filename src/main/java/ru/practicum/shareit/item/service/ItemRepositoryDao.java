package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepositoryDao {
    Item add(Item item);

    Item update(Item item);

    Optional<Item> findById(Long itemId);

    List<Item> findAll(Long userId);

    List<Item> search(String text);
}
