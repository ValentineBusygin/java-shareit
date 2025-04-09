package ru.practicum.shareit.item.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("""
            SELECT i FROM Item i
            WHERE i.available = true
            AND lower(i.name) LIKE lower(%?1%) OR lower(i.description) LIKE lower(%?1%)
            """)
    List<Item> findByText(String text);

    List<Item> findAllByOwnerId(Long ownerId);
}
