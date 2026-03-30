package com.gambit.labs.mission.control.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gambit.labs.mission.control.IntegrationTestBase;
import com.gambit.labs.mission.control.dao.MissionStatus;
import com.gambit.labs.mission.control.dto.ProjectDto;
import com.gambit.labs.mission.control.dto.UserDto;
import com.gambit.labs.mission.control.utils.TestDataUtils;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import tools.jackson.core.type.TypeReference;

public class UserControllerIntTest extends IntegrationTestBase {

  @Test
  void create_user_ok() throws Exception {
    // given
    final String userName = TestDataUtils.makeUserName();
    final UserDto userDto = UserDto.builder()
        .withUserName(userName)
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
    assertThat(createdUser.getUserName()).isEqualTo(userName);
    assertThat(createdUser.getDateCreated()).isNotNull();
    assertThat(createdUser.getDateModified()).isNotNull();
  }

  @Test
  void get_all_users_ok() throws Exception {
    // given
    final String userName = TestDataUtils.makeUserName();
    final UserDto userDto = UserDto.builder()
        .withUserName(userName)
        .build();
    final String content = jsonMapper.writeValueAsString(userDto);
    mockMvc.perform(post("/api/mission-control/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(content))
        .andExpect(status().isCreated());

    // when
    final String responseContent = mockMvc.perform(get("/api/mission-control/v1/users"))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();

    // then
    final List<UserDto> users = jsonMapper.readValue(responseContent, new TypeReference<>() {
    });
    assertThat(users).isNotEmpty();
    assertThat(users).anyMatch(user -> user.getUserName().equals(userName));
  }

  @Test
  void delete_user_ok() throws Exception {
    // given
    final UserDto userDto = UserDto.builder()
        .withUserName(TestDataUtils.makeUserName())
        .build();
    final String content = jsonMapper.writeValueAsString(userDto);
    final String responseContent = mockMvc.perform(post("/api/mission-control/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(content))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    final UserDto created = jsonMapper.readValue(responseContent, UserDto.class);

    // when / then
    mockMvc.perform(delete("/api/mission-control/v1/users/" + created.getId()))
        .andExpect(status().isNoContent());
  }

  @Test
  void delete_user_conflict_when_assigned() throws Exception {
    // given
    final UserDto userDto = UserDto.builder()
        .withUserName(TestDataUtils.makeUserName())
        .build();
    final String userContent = jsonMapper.writeValueAsString(userDto);
    final String userResponse = mockMvc.perform(post("/api/mission-control/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(userContent))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    final UserDto createdUser = jsonMapper.readValue(userResponse, UserDto.class);

    final ProjectDto projectDto = ProjectDto.builder()
        .withName(TestDataUtils.makeProjectName())
        .withPrefix("USR" + java.util.UUID.randomUUID().toString().substring(0, 4))
        .withAssignedUserId(createdUser.getId())
        .withStatus(MissionStatus.BACKLOG)
        .build();
    final String projectContent = jsonMapper.writeValueAsString(projectDto);
    mockMvc.perform(post("/api/mission-control/v1/projects")
            .contentType(MediaType.APPLICATION_JSON)
            .content(projectContent))
        .andExpect(status().isCreated());

    // when / then
    mockMvc.perform(delete("/api/mission-control/v1/users/" + createdUser.getId()))
        .andExpect(status().isConflict());
  }
}
