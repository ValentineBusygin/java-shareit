package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    public static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final ItemClient itemClient;

    @PostMapping()
    public ResponseEntity<Object> add(@RequestHeader(USER_ID_HEADER) Long userId,
                                      @RequestBody ItemDto itemDto) {
        log.info("Получен запрос на создание пользователем {} вещи: {}", userId, itemDto);

        return itemClient.add(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader(USER_ID_HEADER) Long userId,
                                         @PathVariable Long itemId,
                                         @RequestBody ItemDto itemDto) {
        log.info("Получен запрос на обновление вещи {} пользователем {}", itemId, userId);

        return itemClient.update(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> findById(@RequestHeader(USER_ID_HEADER) Long userId,
                                           @PathVariable Long itemId) {
        log.info("Получен запрос на получение вещи с id = {}", itemId);

        return itemClient.findById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> findAll(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Получен запрос на получение всех вещей пользователя с id = {}", userId);

        return itemClient.findAll(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestHeader(USER_ID_HEADER) Long userId,
                                         @RequestParam String text) {
        log.info("Получен запрос на поиск вещей с текстом {}", text);

        return itemClient.search(userId, text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@PathVariable Long itemId,
                                             @RequestHeader(USER_ID_HEADER) Long userId,
                                             @RequestBody CommentDto commentDto) {
        log.info("Получен запрос на добавление комментария к вещи с id = {} пользователем {}", itemId, userId);

        return itemClient.addComment(userId, itemId, commentDto);
    }
}
