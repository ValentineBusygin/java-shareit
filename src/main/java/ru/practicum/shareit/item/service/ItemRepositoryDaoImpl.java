package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class ItemRepositoryDaoImpl implements ItemRepositoryDao {

    private final Map<Long, Item> itemsById = new HashMap<>();
    private final Map<Long, List<Item>> itemsByOwner = new HashMap<>();
    private long counter = 1L;

    @Override
    public Item add(Item item) {
        item.setId(counter++);

        if (!itemsByOwner.containsKey(item.getOwnerId())) {
            List<Item> items = new ArrayList<>();
            items.add(item);
            itemsByOwner.put(item.getOwnerId(), items);
        } else {
            itemsByOwner.get(item.getOwnerId()).add(item);
        }

        itemsById.put(item.getId(), item);

        return item;
    }

    @Override
    public Item update(Item item) {
        itemsById.put(item.getId(), item);

        List<Item> items = itemsByOwner.get(item.getOwnerId());
        items.removeIf(i -> i.getId().equals(item.getId()));
        items.add(item);

        return item;
    }

    @Override
    public Optional<Item> findById(Long itemId) {
        return Optional.ofNullable(itemsById.get(itemId));
    }

    @Override
    public List<Item> findAll(Long userId) {
        return new ArrayList<>(itemsByOwner.get(userId));
    }

    @Override
    public List<Item> search(String text) {
        return itemsById.values().stream()
                .filter(Item::getAvailable)
                .filter(i -> i.getName().toLowerCase().contains(text.toLowerCase())
                        || i.getDescription().toLowerCase().contains(text.toLowerCase()))
                .toList();
    }
}
