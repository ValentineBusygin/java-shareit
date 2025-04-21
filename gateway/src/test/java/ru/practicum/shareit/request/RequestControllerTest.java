package ru.practicum.shareit.request;

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
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class RequestControllerTest {
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RequestClient requestClient;

    @InjectMocks
    private RequestController requestController;

    private final ItemRequestInDto requestInDto = new ItemRequestInDto("Need a drill");

    private final ItemRequestDto requestDto = new ItemRequestDto(1L, "Need a drill", null, null);

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(requestController)
                .build();
    }

    @Test
    void createRequest() throws Exception {
        when(requestClient.create(anyLong(), any()))
                .thenReturn(ResponseEntity.ok(requestDto));

        mockMvc.perform(post("/requests")
                        .header(RequestController.USER_ID_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInDto)))
                .andExpect(status().isOk());

        verify(requestClient, times(1)).create(anyLong(), any(ItemRequestInDto.class));
    }

    @Test
    void getRequests() throws Exception {
        when(requestClient.getItemRequestsByUserId(anyLong()))
                .thenReturn(ResponseEntity.ok(requestDto));

        mockMvc.perform(get("/requests")
                        .header(RequestController.USER_ID_HEADER, "1"))
                .andExpect(status().isOk());

        verify(requestClient, times(1)).getItemRequestsByUserId(anyLong());
    }

    @Test
    void getAllRequests() throws Exception {
        when(requestClient.getAllItemRequests(anyLong()))
                .thenReturn(ResponseEntity.ok(requestDto));

        mockMvc.perform(get("/requests/all")
                        .header(RequestController.USER_ID_HEADER, "1")
                        .param("from", "0")
                        .param("size", "20"))
                .andExpect(status().isOk());

        verify(requestClient, times(1)).getAllItemRequests(anyLong());
    }

    @Test
    void getRequestById() throws Exception {
        when(requestClient.getItemRequestById(anyLong(), anyLong()))
                .thenReturn(ResponseEntity.ok(requestDto));

        mockMvc.perform(get("/requests/123")
                        .header(RequestController.USER_ID_HEADER, "1"))
                .andExpect(status().isOk());

        verify(requestClient, times(1)).getItemRequestById(anyLong(), anyLong());
    }
}
