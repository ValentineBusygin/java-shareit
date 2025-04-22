package ru.practicum.shareit.user;

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
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserClient userClient;

    @InjectMocks
    private UserController userController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    private final UserDto userDto = UserDto.builder()
            .id(1L)
            .name("name")
            .email("email@email.ru")
            .build();

    private final UserDto userDtoNoName = UserDto.builder()
            .id(1L)
            .email("email@email.ru")
            .build();

    private final UserDto userDtoNoEmail = UserDto.builder()
            .id(1L)
            .name("name")
            .build();

    private final UserDto userDtoWrongEmail = UserDto.builder()
            .id(1L)
            .name("name")
            .email("email@email@ru")
            .build();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .build();
    }

    @Test
    void addNewUserOk() throws Exception {
        when(userClient.add(any())).thenReturn(ResponseEntity.ok(userDto));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        verify(userClient, times(1)).add(any(UserDto.class));
    }

    @Test
    void addNewUserNameError() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDtoNoName)))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).add(any());
    }

    @Test
    void addNewUserNoEmailError() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDtoNoEmail)))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).add(any());
    }

    @Test
    void addNewUserWrongEmailError() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDtoWrongEmail)))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).add(any());
    }

    @Test
    void findByIdOk() throws Exception {
        when(userClient.findById(any())).thenReturn(ResponseEntity.ok(userDto));

        mockMvc.perform(get("/users" + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        verify(userClient, times(1)).findById(any());
    }

    @Test
    void findAllOk() throws Exception {
        when(userClient.findAll()).thenReturn(ResponseEntity.ok(List.of(userDto)));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());

        verify(userClient, times(1)).findAll();
    }

    @Test
    void updateUserOk() throws Exception {
        when(userClient.update(any(), any())).thenReturn(ResponseEntity.ok(userDto));

        mockMvc.perform(patch("/users" + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        verify(userClient, times(1)).update(any(), any(UserUpdateDto.class));
    }

    @Test
    void deleteUserOk() throws Exception {
        when(userClient.deleteUser(any())).thenReturn(null);

        mockMvc.perform(delete("/users" + "/1"))
                .andExpect(status().isOk());

        verify(userClient, times(1)).deleteUser(any());
    }
}
