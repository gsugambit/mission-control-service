package com.gambit.labs.mission.control.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gambit.labs.mission.control.IntegrationTestBase;
import com.gambit.labs.mission.control.dao.MissionStatus;
import com.gambit.labs.mission.control.dto.ProjectDto;
import com.gambit.labs.mission.control.dto.TaskCommentCreateDto;
import com.gambit.labs.mission.control.dto.TaskCommentDto;
import com.gambit.labs.mission.control.dto.TaskDto;
import com.gambit.labs.mission.control.dto.UserDto;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class TaskCommentControllerIntTest extends IntegrationTestBase {

  private UUID taskId;
  private UUID userId;

  @BeforeEach
  void setUp() throws Exception {
    final UserDto userDto = UserDto.builder()
        .withUserName("testuser-" + UUID.randomUUID())
        .build();
    final String userResponse = mockMvc.perform(post("/api/mission-control/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(userDto)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    userId = jsonMapper.readValue(userResponse, UserDto.class).getId();

    final ProjectDto projectDto = ProjectDto.builder()
        .withName("Test Project-" + java.util.UUID.randomUUID())
        .withPrefix("TP" + java.util.UUID.randomUUID().toString().substring(0, 4))
        .withStatus(MissionStatus.BACKLOG)
        .build();
    final String projectResponse = mockMvc.perform(post("/api/mission-control/v1/projects")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(projectDto)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    final UUID projectID = jsonMapper.readValue(projectResponse, ProjectDto.class).getId();

    final TaskDto taskDto = TaskDto.builder()
        .withProjectId(projectID)
        .withName("Test Task")
        .withDescription("Test Description")
        .withStatus(MissionStatus.BACKLOG)
        .build();
    final String taskResponse = mockMvc.perform(post("/api/mission-control/v1/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(taskDto)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    taskId = jsonMapper.readValue(taskResponse, TaskDto.class).getId();
  }

  @Test
  void add_and_get_comments_ok() throws Exception {
    // given
    final TaskCommentCreateDto createDto1 = TaskCommentCreateDto.builder()
        .withUserId(userId)
        .withComment("First comment")
        .build();

    final TaskCommentCreateDto createDto2 = TaskCommentCreateDto.builder()
        .withUserId(userId)
        .withComment("Second comment")
        .build();

    // when (add comments)
    mockMvc.perform(post("/api/mission-control/v1/tasks/{taskId}/comments", taskId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(createDto1)))
        .andExpect(status().isCreated());

    mockMvc.perform(post("/api/mission-control/v1/tasks/{taskId}/comments", taskId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(createDto2)))
        .andExpect(status().isCreated());

    // then (get comments)
    final MvcResult result = mockMvc.perform(
            get("/api/mission-control/v1/tasks/{taskId}/comments", taskId))
        .andExpect(status().isOk())
        .andReturn();

    final List<TaskCommentDto> comments = jsonMapper.readValue(
        result.getResponse().getContentAsString(),
        jsonMapper.getTypeFactory().constructCollectionType(List.class, TaskCommentDto.class));

    assertThat(comments).hasSize(2);
    assertThat(comments.get(0).getComment()).isEqualTo("First comment");
    assertThat(comments.get(0).getTaskId()).isEqualTo(taskId);
    assertThat(comments.get(0).getUserId()).isEqualTo(userId);
    assertThat(comments.get(0).getDateCreated()).isNotNull();

    assertThat(comments.get(1).getComment()).isEqualTo("Second comment");
  }

  @Test
  void add_comment_task_not_found_ko() throws Exception {
    final TaskCommentCreateDto createDto = TaskCommentCreateDto.builder()
        .withUserId(userId)
        .withComment("Comment on non-existent task")
        .build();

    mockMvc.perform(post("/api/mission-control/v1/tasks/{taskId}/comments", UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(createDto)))
        .andExpect(status().isNotFound());
  }

  @Test
  void add_comment_user_not_found_ko() throws Exception {
    final TaskCommentCreateDto createDto = TaskCommentCreateDto.builder()
        .withUserId(UUID.randomUUID())
        .withComment("Comment by non-existent user")
        .build();

    mockMvc.perform(post("/api/mission-control/v1/tasks/{taskId}/comments", taskId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(createDto)))
        .andExpect(status().isNotFound());
  }

  @Test
  void add_comment_invalid_request_ko() throws Exception {
    final TaskCommentCreateDto createDto = TaskCommentCreateDto.builder()
        .withUserId(userId)
        .withComment("") // Empty comment
        .build();

    mockMvc.perform(post("/api/mission-control/v1/tasks/{taskId}/comments", taskId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(createDto)))
        .andExpect(status().isBadRequest());
  }
}
