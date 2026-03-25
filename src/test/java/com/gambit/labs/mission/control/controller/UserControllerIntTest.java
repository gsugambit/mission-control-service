package com.gambit.labs.mission.control.controller;

import com.gambit.labs.mission.control.IntegrationTestBase;
import com.gambit.labs.mission.control.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerIntTest extends IntegrationTestBase {

    @Test
    void create_user_ok() throws Exception {
        // given
        final UserDto userDto = UserDto.builder()
                .withUserName("testuser")
                .build();
        final String content = jsonMapper.writeValueAsString(userDto);

        // when
        final String responseContent = mockMvc.perform(post("/api/mission-control/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // then
        final UserDto createdUser = jsonMapper.readValue(responseContent, UserDto.class);
        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getUserName()).isEqualTo("testuser");
        assertThat(createdUser.getDateCreated()).isNotNull();
        assertThat(createdUser.getDateModified()).isNotNull();
    }
}
