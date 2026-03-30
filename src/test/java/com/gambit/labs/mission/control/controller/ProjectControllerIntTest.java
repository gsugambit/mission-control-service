package com.gambit.labs.mission.control.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gambit.labs.mission.control.IntegrationTestBase;
import com.gambit.labs.mission.control.dao.MissionStatus;
import com.gambit.labs.mission.control.dto.ProjectDto;
import com.gambit.labs.mission.control.dto.TaskDto;
import com.gambit.labs.mission.control.dto.UserDto;
import com.gambit.labs.mission.control.exception.ExceptionResponse;
import com.gambit.labs.mission.control.utils.TestDataUtils;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

public class ProjectControllerIntTest extends IntegrationTestBase {

  @Test
  void project_lifecycle_ok() throws Exception {
    // 1. Create Project
    final ProjectDto projectDto = ProjectDto.builder()
        .withName(TestDataUtils.makeProjectName())
        .withDescription("Test Description")
        .withPrefix("PRJ" + java.util.UUID.randomUUID().toString().substring(0, 4))
        .withStatus(MissionStatus.BACKLOG)
        .build();

    final String projectResponse = mockMvc.perform(post("/api/mission-control/v1/projects")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(projectDto)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();

    final ProjectDto createdProject = jsonMapper.readValue(projectResponse, ProjectDto.class);
    assertThat(createdProject.getId()).isNotNull();
    assertThat(createdProject.getName()).isEqualTo(projectDto.getName());
    assertThat(createdProject.getPrefix()).isEqualTo(projectDto.getPrefix());
    assertThat(createdProject.getStatus()).isEqualTo(MissionStatus.BACKLOG);

    // 2. Verify Get Project
    mockMvc.perform(get("/api/mission-control/v1/projects/" + createdProject.getId()))
        .andExpect(status().isOk());

    // 3. Delete Project
    mockMvc.perform(delete("/api/mission-control/v1/projects/" + createdProject.getId()))
        .andExpect(status().isNoContent());

    // 4. Verify Project is gone
    mockMvc.perform(get("/api/mission-control/v1/projects/" + createdProject.getId()))
        .andExpect(status().isNotFound());
  }

  @Test
  void delete_project_with_tasks_fails() throws Exception {
    // 1. Create Project
    final ProjectDto projectDto = ProjectDto.builder()
        .withName(TestDataUtils.makeProjectName())
        .withPrefix("TASK" + java.util.UUID.randomUUID().toString().substring(0, 4))
        .withStatus(MissionStatus.BACKLOG)
        .build();

    final String projectResponse = mockMvc.perform(post("/api/mission-control/v1/projects")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(projectDto)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();

    final ProjectDto createdProject = jsonMapper.readValue(projectResponse, ProjectDto.class);

    // 2. Create Task for Project
    final TaskDto taskDto = TaskDto.builder()
        .withName("Task for project deletion test")
        .withProjectId(createdProject.getId())
        .withDescription("Test Task")
        .withStatus(MissionStatus.BACKLOG)
        .build();

    mockMvc.perform(post("/api/mission-control/v1/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(taskDto)))
        .andExpect(status().isCreated());

    // 3. Try to delete project with tasks (should fail)
    final String errorResponse = mockMvc.perform(
            delete("/api/mission-control/v1/projects/" + createdProject.getId()))
        .andExpect(status().isConflict())
        .andReturn().getResponse().getContentAsString();

    final ExceptionResponse error = jsonMapper.readValue(errorResponse, ExceptionResponse.class);
    assertThat(error.getMessage()).contains("because it has associated tasks");
  }

  @Test
  void assign_user_to_project_ok() throws Exception {
    // 1. Create Project
    final ProjectDto projectDto = ProjectDto.builder()
        .withName(TestDataUtils.makeProjectName())
        .withPrefix("ASGN" + java.util.UUID.randomUUID().toString().substring(0, 4))
        .withStatus(MissionStatus.BACKLOG)
        .build();
    final String projectResponse = mockMvc.perform(post("/api/mission-control/v1/projects")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(projectDto)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    final ProjectDto createdProject = jsonMapper.readValue(projectResponse, ProjectDto.class);

    // 2. Create User
    final UserDto userDto = UserDto.builder()
        .withUserName(TestDataUtils.makeUserName()).build();
    final String userResponse = mockMvc.perform(post("/api/mission-control/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(userDto)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    final UserDto createdUser = jsonMapper.readValue(userResponse, UserDto.class);

    // 3. Assign User
    final String assignedProjectResponse = mockMvc.perform(patch(
            "/api/mission-control/v1/projects/" + createdProject.getId() + "/assign/"
                + createdUser.getId()))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    final ProjectDto assignedProject = jsonMapper.readValue(assignedProjectResponse,
        ProjectDto.class);
    assertThat(assignedProject.getAssignedUserId()).isEqualTo(createdUser.getId());
  }

  @Test
  void create_project_with_assigned_user_ok() throws Exception {
    // 1. Create User
    final UserDto userDto = UserDto.builder()
        .withUserName(TestDataUtils.makeUserName()).build();
    final String userResponse = mockMvc.perform(post("/api/mission-control/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(userDto)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    final UserDto createdUser = jsonMapper.readValue(userResponse, UserDto.class);

    // 2. Create Project with Assigned User
    final ProjectDto projectDto = ProjectDto.builder()
        .withName(TestDataUtils.makeProjectName())
        .withPrefix("INIT" + java.util.UUID.randomUUID().toString().substring(0, 4))
        .withAssignedUserId(createdUser.getId())
        .withStatus(MissionStatus.BACKLOG)
        .build();

    final String projectResponse = mockMvc.perform(post("/api/mission-control/v1/projects")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(projectDto)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();

    final ProjectDto createdProject = jsonMapper.readValue(projectResponse, ProjectDto.class);
    assertThat(createdProject.getAssignedUserId()).isEqualTo(createdUser.getId());
  }

  @Test
  void move_project_to_new_status_ok() throws Exception {
    // 1. Create Project
    final ProjectDto projectDto = ProjectDto.builder()
        .withName(TestDataUtils.makeProjectName())
        .withPrefix("MOVE" + java.util.UUID.randomUUID().toString().substring(0, 4))
        .withStatus(MissionStatus.BACKLOG)
        .build();
    final String projectResponse = mockMvc.perform(post("/api/mission-control/v1/projects")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(projectDto)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    final ProjectDto createdProject = jsonMapper.readValue(projectResponse, ProjectDto.class);

    // 2. Move Project
    final String movedProjectResponse = mockMvc.perform(patch(
            "/api/mission-control/v1/projects/" + createdProject.getId() + "/status/"
                + MissionStatus.IN_PROGRESS))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    final ProjectDto movedProject = jsonMapper.readValue(movedProjectResponse, ProjectDto.class);
    assertThat(movedProject.getStatus()).isEqualTo(MissionStatus.IN_PROGRESS);
  }

  @Test
  void create_project_with_status_ok() throws Exception {
    // 1. Create Project with READY status
    final ProjectDto projectDto = ProjectDto.builder()
        .withName(TestDataUtils.makeProjectName())
        .withPrefix("RDY" + java.util.UUID.randomUUID().toString().substring(0, 4))
        .withStatus(MissionStatus.READY)
        .build();

    final String projectResponse = mockMvc.perform(post("/api/mission-control/v1/projects")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(projectDto)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();

    final ProjectDto createdProject = jsonMapper.readValue(projectResponse, ProjectDto.class);
    assertThat(createdProject.getStatus()).isEqualTo(MissionStatus.READY);
  }

  @Test
  void project_blocked_status_validation() throws Exception {
    // 1. Create Project with BLOCKED status without reason - should fail (Conflict)
    final ProjectDto projectDto = ProjectDto.builder()
        .withName(TestDataUtils.makeProjectName())
        .withPrefix("BL1" + java.util.UUID.randomUUID().toString().substring(0, 3))
        .withStatus(MissionStatus.BLOCKED)
        .build();
    mockMvc.perform(post("/api/mission-control/v1/projects")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(projectDto)))
        .andExpect(status().isConflict());

    // 2. Create Project with BLOCKED status with reason - should succeed
    final ProjectDto projectWithReasonDto = ProjectDto.builder()
        .withName(TestDataUtils.makeProjectName())
        .withPrefix("BL2" + java.util.UUID.randomUUID().toString().substring(0, 3))
        .withStatus(MissionStatus.BLOCKED)
        .withBlockedReason("Need more coffee")
        .build();
    final String projectResponse = mockMvc.perform(post("/api/mission-control/v1/projects")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(projectWithReasonDto)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    final ProjectDto createdProject = jsonMapper.readValue(projectResponse, ProjectDto.class);
    assertThat(createdProject.getBlockedReason()).isEqualTo("Need more coffee");

    // 3. Update status to IN_PROGRESS - reason should be cleared
    final String updatedProjectResponse = mockMvc.perform(patch(
            "/api/mission-control/v1/projects/" + createdProject.getId() + "/status/"
                + MissionStatus.IN_PROGRESS))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    final ProjectDto updatedProject = jsonMapper.readValue(updatedProjectResponse,
        ProjectDto.class);
    assertThat(updatedProject.getStatus()).isEqualTo(MissionStatus.IN_PROGRESS);
    assertThat(updatedProject.getBlockedReason()).isNull();

    // 4. Update status to BLOCKED via patch status without body - should fail (Conflict)
    mockMvc.perform(patch("/api/mission-control/v1/projects/" + createdProject.getId() + "/status/"
            + MissionStatus.BLOCKED))
        .andExpect(status().isConflict());

    // 5. Update status to BLOCKED via patch status with reason - should succeed
    final ProjectDto blockedStatusDto = ProjectDto.builder().withBlockedReason("Really blocked now")
        .build();
    final String reBlockedProjectResponse = mockMvc.perform(patch(
            "/api/mission-control/v1/projects/" + createdProject.getId() + "/status/"
                + MissionStatus.BLOCKED)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(blockedStatusDto)))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    final ProjectDto reBlockedProject = jsonMapper.readValue(reBlockedProjectResponse,
        ProjectDto.class);
    assertThat(reBlockedProject.getStatus()).isEqualTo(MissionStatus.BLOCKED);
    assertThat(reBlockedProject.getBlockedReason()).isEqualTo("Really blocked now");
  }

  @Test
  void get_projects_by_user_ok() throws Exception {
    // 1. Create User
    final UserDto userDto = UserDto.builder()
        .withUserName(TestDataUtils.makeUserName()).build();
    final String userResponse = mockMvc.perform(post("/api/mission-control/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(userDto)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    final UserDto createdUser = jsonMapper.readValue(userResponse, UserDto.class);

    // 2. Create Projects assigned to user
    final String p1Name = TestDataUtils.makeProjectName();
    final String p2Name = TestDataUtils.makeProjectName();
    final ProjectDto project1 = ProjectDto.builder().withName(p1Name)
        .withPrefix("P1" + java.util.UUID.randomUUID().toString().substring(0, 2))
        .withAssignedUserId(createdUser.getId())
        .withStatus(MissionStatus.BACKLOG)
        .build();
    final ProjectDto project2 = ProjectDto.builder().withName(p2Name)
        .withPrefix("P2" + java.util.UUID.randomUUID().toString().substring(0, 2))
        .withAssignedUserId(createdUser.getId())
        .withStatus(MissionStatus.BACKLOG)
        .build();
    final ProjectDto project3 = ProjectDto.builder()
        .withName(TestDataUtils.makeProjectName())
        .withPrefix("P3" + java.util.UUID.randomUUID().toString().substring(0, 2))
        .withStatus(MissionStatus.BACKLOG)
        .build();

    mockMvc.perform(post("/api/mission-control/v1/projects")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonMapper.writeValueAsString(project1))).andExpect(status().isCreated());
    mockMvc.perform(post("/api/mission-control/v1/projects")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonMapper.writeValueAsString(project2))).andExpect(status().isCreated());
    mockMvc.perform(post("/api/mission-control/v1/projects")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonMapper.writeValueAsString(project3))).andExpect(status().isCreated());

    // 3. Get Projects by User
    final String projectsResponse = mockMvc.perform(
            get("/api/mission-control/v1/projects/user/" + createdUser.getId()))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();

    final List<ProjectDto> projects = jsonMapper.readValue(projectsResponse,
        new tools.jackson.core.type.TypeReference<List<ProjectDto>>() {
        });
    assertThat(projects).hasSize(2);
    assertThat(projects).extracting(ProjectDto::getName)
        .containsExactlyInAnyOrder(p1Name, p2Name);
  }
}
