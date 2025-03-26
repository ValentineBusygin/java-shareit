package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

@Repository
@Primary
@RequiredArgsConstructor
public class ItemServiceDaoImpl implements ItemServiceDao {

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

        return item;
    }

    @Override
    public Item update(Item item) {
        List<Item> items = itemsByOwner.get(item.getOwnerId());
        List<Item> toRemove = items.stream()
                .filter(i -> i.getId().equals(item.getId()))
                .toList();
        items.removeAll(toRemove);
        items.add(item);
        return item;
    }

    @Override
    public Optional<Item> findById(Long itemId) {
        return itemsByOwner.values().stream()
                .flatMap(Collection::stream)
                .filter(i -> i.getId().equals(itemId))
                .findFirst();
    }

    @Override
    public List<Item> findAll(Long userId) {
        return new ArrayList<>(itemsByOwner.get(userId));
    }

    @Override
    public List<Item> search(String text) {
        return itemsByOwner.values().stream()
                .flatMap(Collection::stream)
                .filter(Item::getAvailable)
                .filter(i -> i.getName().toLowerCase().contains(text.toLowerCase())
                        || i.getDescription().toLowerCase().contains(text.toLowerCase()))
                .toList();
    }
}
