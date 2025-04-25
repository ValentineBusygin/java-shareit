package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest({BookingController.class})
@AutoConfigureMockMvc
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    private BookingService bookingService;

    @InjectMocks
    private BookingController bookingController;

    private final BookingInDto bookingInDto = BookingInDto.builder()
            .itemId(1L)
            .start(LocalDateTime.now().plusDays(1))
            .end(LocalDateTime.now().plusDays(2))
            .build();

    private final UserDto userDto = UserDto.builder()
            .id(1L)
            .name("Test")
            .email("test@test.com")
            .build();

    private final ItemDto itemDto = ItemDto.builder()
            .id(1L)
            .name("Test Item")
            .description("Test Item Description")
            .available(true)
            .build();

    private final BookingOutDto bookingOutDto = BookingOutDto.builder()
            .id(1L)
            .start(bookingInDto.getStart())
            .end(bookingInDto.getEnd())
            .booker(userDto)
            .item(itemDto)
            .status(BookingStatus.WAITING)
            .build();

    @BeforeAll
    static void init() {
        objectMapper.findAndRegisterModules();
    }

    @Test
    void addBooking() throws Exception {
        when(bookingService.create(any(), anyLong()))
                .thenReturn(bookingOutDto);

        mockMvc.perform(post("/bookings")
                        .header(BookingController.USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingInDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));

        verify(bookingService).create(any(BookingInDto.class), eq(1L));
    }

    @Test
    void approveBooking() throws Exception {
        when(bookingService.update(any(), anyLong(), anyBoolean()))
                .thenReturn(bookingOutDto);

        mockMvc.perform(patch("/bookings/1?approved=true")
                        .header(BookingController.USER_ID_HEADER, 1L))
                .andExpect(status().isOk());

        verify(bookingService, times(1)).update(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    void getBookingById() throws Exception {
        when(bookingService.getById(any(), anyLong()))
                .thenReturn(bookingOutDto);

        mockMvc.perform(get("/bookings/1")
                        .header(BookingController.USER_ID_HEADER, 1L))
                .andExpect(status().isOk());

        verify(bookingService).getById(anyLong(), anyLong());
    }

    @Test
    void getAllBookingsByUserId() throws Exception {
        when(bookingService.getAll0fUserByState(anyLong(), any()))
                .thenReturn(List.of(bookingOutDto));

        mockMvc.perform(get("/bookings?state=PAST")
                        .header(BookingController.USER_ID_HEADER, 1L))
                .andExpect(status().isOk());

        verify(bookingService, times(1)).getAll0fUserByState(anyLong(), any());
    }

    @Test
    void getAllBookingsByOwnerId() throws Exception {
        when(bookingService.getAll0fOwnerByState(anyLong(), any()))
                .thenReturn(List.of(bookingOutDto));

        mockMvc.perform(get("/bookings/owner?state=FUTURE")
                        .header(BookingController.USER_ID_HEADER, 1L))
                .andExpect(status().isOk());

        verify(bookingService).getAll0fOwnerByState(anyLong(), any());
    }
}
