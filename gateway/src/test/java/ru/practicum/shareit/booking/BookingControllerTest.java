package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
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
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class BookingControllerTest {

    private MockMvc mockMvc;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private BookingClient bookingClient;

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

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(bookingController)
                .build();
    }

    @Test
    void addBooking() throws Exception {
        when(bookingClient.add(any(), anyLong()))
                .thenReturn(ResponseEntity.ok(bookingOutDto));

        mockMvc.perform(post("/bookings")
                        .header(BookingController.USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingInDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));

        verify(bookingClient).add(any(BookingInDto.class), eq(1L));
    }

    @Test
    void approveBooking() throws Exception {
        when(bookingClient.approveBooking(any(), anyLong(), anyBoolean()))
                .thenReturn(ResponseEntity.ok(bookingOutDto));

        mockMvc.perform(patch("/bookings/1?approved=true")
                        .header(BookingController.USER_ID_HEADER, 1L))
                .andExpect(status().isOk());

        verify(bookingClient, times(1)).approveBooking(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    void getBookingById() throws Exception {
        when(bookingClient.getBookingById(any(), anyLong()))
                .thenReturn(ResponseEntity.ok(bookingOutDto));

        mockMvc.perform(get("/bookings/1")
                        .header(BookingController.USER_ID_HEADER, 1L))
                .andExpect(status().isOk());

        verify(bookingClient).getBookingById(anyLong(), anyLong());
    }

    @Test
    void getAllBookingsByUserId() throws Exception {
        when(bookingClient.getAll0fUserByState(anyLong(), any()))
                .thenReturn(ResponseEntity.ok(bookingOutDto));

        mockMvc.perform(get("/bookings?state=PAST")
                        .header(BookingController.USER_ID_HEADER, 1L))
                .andExpect(status().isOk());

        verify(bookingClient, times(1)).getAll0fUserByState(anyLong(), any());
    }

    @Test
    void getAllBookingsByOwnerId() throws Exception {
        when(bookingClient.getAllBookingsByOwnerIdAndState(anyLong(), any()))
                .thenReturn(ResponseEntity.ok(bookingOutDto));

        mockMvc.perform(get("/bookings/owner?state=FUTURE")
                        .header(BookingController.USER_ID_HEADER, 1L))
                .andExpect(status().isOk());

        verify(bookingClient).getAllBookingsByOwnerIdAndState(anyLong(), any());
    }

    @Test
    void getAllBookingsWithDefaultState() throws Exception {
        when(bookingClient.getAll0fUserByState(anyLong(), any()))
                .thenReturn(ResponseEntity.ok(bookingOutDto));

        mockMvc.perform(get("/bookings")
                        .header(BookingController.USER_ID_HEADER, 1L))
                .andExpect(status().isOk());

        verify(bookingClient).getAll0fUserByState(anyLong(), any());
    }
}
