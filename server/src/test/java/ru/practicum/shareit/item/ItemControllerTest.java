package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemServiceImpl;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ItemControllerTest {

    @Mock
    private ItemServiceImpl itemService;

    @InjectMocks
    private ItemController itemController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    private ItemDto itemDto;

    private CommentDto commentDto;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(itemController)
                .build();

        itemDto = ItemDto.builder()
                .id(1L)
                .name("item")
                .description("desc")
                .available(true)
                .build();

        commentDto = CommentDto.builder()
                .id(1L)
                .text("Nice item!")
                .build();
    }

    @Test
    void addNewItem() throws Exception {
        when(itemService.add(anyLong(), any(ItemDto.class)))
                .thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header(ItemController.USER_ID_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));

        verify(itemService, times(1)).add(eq(1L), any(ItemDto.class));
    }

    @Test
    void findByIdOk() throws Exception {
        when(itemService.findById(anyLong(), anyLong())).thenReturn(itemDto);

        mockMvc.perform(get("/items" + "/1")
                        .header(ItemController.USER_ID_HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())));

        verify(itemService, times(1)).findById(anyLong(), anyLong());
    }

    @Test
    void findAllOk() throws Exception {
        when(itemService.findAll(anyLong())).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items")
                        .header(ItemController.USER_ID_HEADER, "1"))
                .andExpect(status().isOk());

        verify(itemService, times(1)).findAll(anyLong());
    }

    @Test
    void updateItemOk() throws Exception {
        when(itemService.update(anyLong(), anyLong(), any(ItemDto.class)))
                .thenReturn(itemDto);

        mockMvc.perform(patch("/items/1")
                        .header(ItemController.USER_ID_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));

        verify(itemService, times(1)).update(eq(1L), eq(1L), eq(itemDto));
    }

    @Test
    void addCommentOk() throws Exception {
        when(itemService.addComment(anyLong(), anyLong(), any(CommentDto.class)))
                .thenReturn(commentDto);

        mockMvc.perform(post("/items/1/comment")
                        .header(ItemController.USER_ID_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.text", is(commentDto.getText())));

        verify(itemService, times(1)).addComment(anyLong(), anyLong(), any(CommentDto.class));
    }
}
