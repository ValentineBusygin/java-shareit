package ru.practicum.shareit.item;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;

@Transactional
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ItemIntegrationTest {

    private final ItemController itemController;

    private final EntityManager entityManager;

    private Long itemsCount = 0L;

    /*@Test
    void addItemOk() {
        ItemDto newItem = getItemDto(itemsCount);

        ItemDto addedItem = itemController.add(1L, newItem);

        TypedQuery<Item> query = entityManager.createQuery("SELECT i FROM Item i WHERE i.id = :id", Item.class);
        Item addedItemDb = query.setParameter("id", addedItem.getId()).getSingleResult();

        assertNotNull(addedItemDb.getId());
        assertEquals(addedItemDb.getName(), newItem.getName(), "Wrong name of item");
        assertEquals(addedItemDb.getDescription(), newItem.getDescription(), "Wrong description of item");
    }*/

    public ItemDto getItemDto(Long counter) {
        itemsCount++;

        return new ItemDto(counter,
                "testItemName-" + counter,
                "testItemDescription" + counter,
                true,
                null,
                null,
                null,
                null);
    }
}
