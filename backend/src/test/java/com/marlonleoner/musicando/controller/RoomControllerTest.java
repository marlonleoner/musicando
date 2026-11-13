package com.marlonleoner.musicando.controller;

import com.marlonleoner.musicando.aggregation.RoomAggregation;
import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.enums.RoomStatus;
import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.domain.exception.ObjectNotFoundException;
import com.marlonleoner.musicando.domain.exception.RoomNotJoinableException;
import com.marlonleoner.musicando.dto.request.JoinRoomRequest;
import com.marlonleoner.musicando.dto.response.JoinRoomResponse;
import com.marlonleoner.musicando.dto.response.RoomCreatedResponse;
import com.marlonleoner.musicando.dto.response.RoomResponse;
import com.marlonleoner.musicando.fixture.Fixture;
import com.marlonleoner.musicando.handler.ExceptionHandlers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RoomControllerTest {

    private static final String JOIN_URL = "/room/join";

    private static final String JOIN_BODY = "{\"code\":\"ABC123\",\"nickname\":\"Marlon\"}";

    @Mock
    private RoomAggregation aggregation;

    @InjectMocks
    private RoomController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ExceptionHandlers())
                .build();
    }

    @Test
    void shouldReturnOkWhenGettingAllRooms() throws Exception {
        when(aggregation.getAllRooms()).thenReturn(Fixture.makeList(RoomResponse.class, 2));

        mockMvc.perform(get("/room"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnOkWhenCreatingRoom() throws Exception {
        when(aggregation.createRoom()).thenReturn(Fixture.make(RoomCreatedResponse.class));

        mockMvc.perform(post("/room"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnOkWhenJoiningRoom() throws Exception {
        when(aggregation.joinRoom(any(JoinRoomRequest.class))).thenReturn(Fixture.make(JoinRoomResponse.class));

        mockMvc.perform(post(JOIN_URL).contentType(MediaType.APPLICATION_JSON).content(JOIN_BODY))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnNotFoundWhenRoomDoesNotExist() throws Exception {
        when(aggregation.joinRoom(any(JoinRoomRequest.class))).thenThrow(new ObjectNotFoundException(Room.class));

        mockMvc.perform(post(JOIN_URL).contentType(MediaType.APPLICATION_JSON).content(JOIN_BODY))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertThat(result.getResolvedException()).isInstanceOf(ObjectNotFoundException.class));
    }

    @Test
    void shouldReturnConflictWhenRoomIsNotJoinable() throws Exception {
        when(aggregation.joinRoom(any(JoinRoomRequest.class))).thenThrow(new RoomNotJoinableException("ABC123", RoomStatus.FINISHED));

        mockMvc.perform(post(JOIN_URL).contentType(MediaType.APPLICATION_JSON).content(JOIN_BODY))
                .andExpect(status().isConflict())
                .andExpect(result -> assertThat(result.getResolvedException()).isInstanceOf(BaseException.class));
    }

    @Test
    void shouldReturnBadRequestWhenBodyIsMissing() throws Exception {
        mockMvc.perform(post(JOIN_URL).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnInternalServerErrorWhenUnexpectedErrorOccurs() throws Exception {
        when(aggregation.createRoom()).thenThrow(new IllegalStateException("database down"));

        mockMvc.perform(post("/room"))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertThat(result.getResolvedException()).isInstanceOf(IllegalStateException.class));
    }
}
