package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.NotOwnerException;
import ru.practicum.shareit.item.CommentMapper;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    private static final String USER_NOT_FOUND = "Пользователь с id %d не найден";

    @Override
    @Transactional(rollbackFor = NotFoundException.class)
    public ItemDto add(Long ownerId, ItemDto itemDto) {
        User owner = userRepository.findById(ownerId).orElseThrow(() ->
                new NotFoundException(String.format(USER_NOT_FOUND, ownerId)));

        Item item = ItemMapper.toItem(itemDto);

        item.setOwner(owner);

        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional(rollbackFor = {NotFoundException.class, NotOwnerException.class})
    public ItemDto update(Long ownerId, Long itemId, ItemDto itemDto) {
        User owner = userRepository.findById(ownerId).orElseThrow(() ->
                new NotFoundException(String.format(USER_NOT_FOUND, ownerId)));
        Optional<Item> oItem = itemRepository.findById(itemId);
        if (oItem.isPresent()) {
            Item itemInStorage = oItem.get();
            if (!itemInStorage.getOwner().getId().equals(owner.getId())) {
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

            return ItemMapper.toItemDto(itemRepository.save(itemInStorage));
        }

        return itemDto;
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto findById(Long userId, Long itemId) {
        userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException(String.format(USER_NOT_FOUND, userId)));

        Item item = itemRepository.findById(itemId).orElseThrow(() ->
                new NotFoundException(
                        String.format("Вещь с id = %d не найдена у пользователя с id = %d", itemId, userId)));

        ItemDto itemDto = ItemMapper.toItemDto(item);

        if (item.getOwner().getId().equals(userId)) {
            List<Booking> bookings = bookingRepository.findByItemOwnerId(item.getOwner().getId());

            setLastAndNextBooking(itemDto, bookings);
        }

        List<Comment> comments = commentRepository.findAllByItemIdOrderByCreatedDesc(item.getId());
        itemDto.setComments(comments.stream().map(CommentMapper::toCommentDto).toList());

        return itemDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> findAll(Long userId) {
        userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException(String.format(USER_NOT_FOUND, userId)));

        List<Item> items = itemRepository.findAllByOwnerId(userId);

        return items.stream()
                .map(item -> {
                    ItemDto itemDto = ItemMapper.toItemDto(item);

                    if (item.getOwner().getId().equals(userId)) {
                        List<Booking> bookings = bookingRepository.findByItemOwnerId(item.getOwner().getId());

                        setLastAndNextBooking(itemDto, bookings);
                    }

                    List<Comment> comments = commentRepository.findAllByItemIdOrderByCreatedDesc(item.getId());
                    itemDto.setComments(comments.stream().map(CommentMapper::toCommentDto).toList());

                    return itemDto;
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> search(Long userId, String text) {
        userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException(String.format(USER_NOT_FOUND, userId)));

        if (text.isBlank()) {
            return Collections.emptyList();
        }
        List<Item> items = itemRepository.findByText(text);

        return items.stream().map(ItemMapper::toItemDto).toList();
    }

    @Override
    @Transactional(rollbackFor = {NotFoundException.class, NotOwnerException.class})
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        User author = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException(String.format(USER_NOT_FOUND, userId)));

        Item item = itemRepository.findById(itemId).orElseThrow(() ->
                new NotFoundException(
                        String.format("Вещь с id = %d не найдена", itemId)));

        Booking booking = bookingRepository.findFirstByItemIdAndBookerIdAndEndBeforeOrderByStartDesc(
                itemId,
                userId,
                LocalDateTime.now());

        if (Objects.isNull(booking)) {
            throw new NotOwnerException(String.format("Вещь с id = %d не была забронирована пользователем с id = %d", itemId, userId));
        }

        Comment comment = CommentMapper.toComment(commentDto, item, author);

        commentRepository.save(comment);

        return CommentMapper.toCommentDto(comment);
    }

    private void setLastAndNextBooking(ItemDto item, List<Booking> bookings) {
        //Последнее бронирование может быть как завершённым, так и активным
        Booking lastBooking = bookings.stream()
                .filter(booking -> booking.getEnd().isBefore(LocalDateTime.now()) ||
                        (booking.getStart().isBefore(LocalDateTime.now()) && booking.getEnd().isAfter(LocalDateTime.now())))
                .max(Comparator.comparing(Booking::getEnd))
                .orElse(null);

        //Текущее бронирование следующим не считаем, поэтому не делаем доппроверку
        Booking nextBooking = bookings.stream()
                .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                .min(Comparator.comparing(Booking::getStart))
                .orElse(null);

        if (!Objects.isNull(lastBooking)) {
            item.setLastBooking(BookingMapper.toBookingOutDto(lastBooking));
        }

        if (!Objects.isNull(nextBooking)) {
            item.setNextBooking(BookingMapper.toBookingOutDto(nextBooking));
        }
    }
}
