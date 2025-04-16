package ru.practicum.shareit.request.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ErrorMessagesConst;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemRepository;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemRequestDto create(Long userId, ItemRequestInDto itemRequestDto) {

        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException(String.format(ErrorMessagesConst.USER_NOT_FOUND, userId)));

        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto, user);

        itemRequestRepository.save(itemRequest);

        return ItemRequestMapper.toItemRequestDto(itemRequest);
    }

    @Override
    public ItemRequestDto getItemRequestById(Long requestId) {
        return itemRequestRepository.findById(requestId)
                .map(request -> {
                    List<ItemDto> items = itemRepository.findAllByRequestId(requestId)
                            .stream()
                            .map(ItemMapper::toItemDto)
                            .toList();

                    return ItemRequestMapper.toItemRequestDto(request, items);
                })
                .orElseThrow(
                        () -> new NotFoundException(String.format(ErrorMessagesConst.ITEM_REQUEST_NOT_FOUND, requestId)));
    }

    @Override
    public List<ItemRequestDto> getItemRequestsByUserId(Long userId) {
        return itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(userId).stream()
                .map(request -> {
                            List<ItemDto> items = itemRepository.findAllByRequestId(request.getId())
                                    .stream()
                                    .map(ItemMapper::toItemDto)
                                    .toList();

                            return ItemRequestMapper.toItemRequestDto(request, items);
                        }
                )
                .toList();
    }

    @Override
    public List<ItemRequestDto> getAllItemRequests(Long userId) {
        return itemRequestRepository.findAll().stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .toList();
    }
}
