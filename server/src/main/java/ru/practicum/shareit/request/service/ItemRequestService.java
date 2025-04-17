package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(Long userId, ItemRequestInDto itemRequestDt);

    ItemRequestDto getItemRequestById(Long requestId);

    List<ItemRequestDto> getItemRequestsByUserId(Long userId);

    List<ItemRequestDto> getAllItemRequests(Long userId);
}
