package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    public static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final ItemService itemService;

    @PostMapping()
    public ItemDto add(@RequestHeader(USER_ID_HEADER) Long userId,
                       @Valid @RequestBody ItemDto itemDto) {
        log.info("Получен запрос на создание пользователем {} вещи: {}", userId, itemDto);

        return itemService.add(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(USER_ID_HEADER) Long userId,
                          @PathVariable Long itemId,
                          @RequestBody ItemDto itemDto) {
        log.info("Получен запрос на обновление вещи {} пользователем {}", itemId, userId);

        return itemService.update(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto findById(@RequestHeader(USER_ID_HEADER) Long userId,
                            @PathVariable Long itemId) {
        log.info("Получен запрос на получение вещи с id = {}", itemId);

        return itemService.findById(userId, itemId);
    }

    @GetMapping
    public List<ItemDto> findAll(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Получен запрос на получение всех вещей пользователя с id = {}", userId);

        return itemService.findAll(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestHeader(USER_ID_HEADER) Long userId,
                                @RequestParam String text) {
        log.info("Получен запрос на поиск вещей с текстом {}", text);

        return itemService.search(userId, text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@PathVariable Long itemId,
                                 @RequestHeader(USER_ID_HEADER) Long userId,
                                 @RequestBody CommentDto commentDto) {
        log.info("Получен запрос на добавление комментария к вещи с id = {} пользователем {}", itemId, userId);

        return itemService.addComment(userId, itemId, commentDto);
    }
}
