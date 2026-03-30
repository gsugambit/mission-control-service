package com.gambit.labs.mission.control.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gambit.labs.mission.control.IntegrationTestBase;
import com.gambit.labs.mission.control.dao.MissionStatus;
import com.gambit.labs.mission.control.dto.ProjectDto;
import com.gambit.labs.mission.control.dto.TaskDto;
import com.gambit.labs.mission.control.dto.UserDto;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

public class TaskControllerIntTest extends IntegrationTestBase {

  @Test
  void task_lifecycle_ok() throws Exception {
    // 1. Create Project (Tasks need a project)
    final ProjectDto projectDto = ProjectDto.builder()
        .withName("Project for Task " + UUID.randomUUID())
        .withPrefix("TSK" + UUID.randomUUID().toString().substring(0, 4))
        .withStatus(MissionStatus.BACKLOG)
        .build();

    final String projectResponse = mockMvc.perform(post("/api/mission-control/v1/projects")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(projectDto)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();

    final ProjectDto createdProject = jsonMapper.readValue(projectResponse, ProjectDto.class);

    // 2. Create Task
    final TaskDto taskDto = TaskDto.builder()
        .withProjectId(createdProject.getId())
        .withName("Test Task Name")
        .withDescription("Test Task")
        .withStatus(MissionStatus.BACKLOG)
        .build();

    final String taskResponse = mockMvc.perform(post("/api/mission-control/v1/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(taskDto)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();

    final TaskDto createdTask = jsonMapper.readValue(taskResponse, TaskDto.class);
    assertThat(createdTask.getId()).isNotNull();
    assertThat(createdTask.getProjectId()).isEqualTo(createdProject.getId());
    assertThat(createdTask.getTaskCode()).startsWith(createdProject.getPrefix() + "-");

    // 3. Update Task (taskCode should be immutable)
    final TaskDto updateDto = TaskDto.builder()
        .withProjectId(createdProject.getId())
        .withName("Updated Task Name")
        .withDescription("Updated Task Description")
        .withStatus(MissionStatus.BACKLOG)
        .withTaskCode("FAKE-CODE")
        .build();

    final String updateResponse = mockMvc.perform(
            put("/api/mission-control/v1/tasks/" + createdTask.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(updateDto)))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();

    final TaskDto updatedTask = jsonMapper.readValue(updateResponse, TaskDto.class);
    assertThat(updatedTask.getName()).isEqualTo("Updated Task Name");
    assertThat(updatedTask.getTaskCode()).isEqualTo(createdTask.getTaskCode());
    assertThat(updatedTask.getTaskCode()).isNotEqualTo("FAKE-CODE");

    // 4. Delete Task
    mockMvc.perform(delete("/api/mission-control/v1/tasks/" + createdTask.getId()))
        .andExpect(status().isNoContent());
  }

  @Test
  void get_task_by_code_ok() throws Exception {
    // 1. Create Project
    final ProjectDto projectDto = ProjectDto.builder()
        .withName("Project for Task " + UUID.randomUUID())
        .withPrefix("COD" + UUID.randomUUID().toString().substring(0, 4))
        .withStatus(MissionStatus.BACKLOG)
        .build();

    final String projectResponse = mockMvc.perform(post("/api/mission-control/v1/projects")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(projectDto)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();

    final ProjectDto createdProject = jsonMapper.readValue(projectResponse, ProjectDto.class);

    // 2. Create Task
    final TaskDto taskDto = TaskDto.builder()
        .withProjectId(createdProject.getId())
        .withName("Test Task Name")
        .withDescription("Test Task")
        .withStatus(MissionStatus.BACKLOG)
        .build();

    final String taskResponse = mockMvc.perform(post("/api/mission-control/v1/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(taskDto)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();

    final TaskDto createdTask = jsonMapper.readValue(taskResponse, TaskDto.class);

    // 3. Get Task by Task Code
    final String getByCodeResponse = mockMvc.perform(
            get("/api/mission-control/v1/tasks/task-code/" + createdTask.getTaskCode()))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();

    final TaskDto taskFromCode = jsonMapper.readValue(getByCodeResponse, TaskDto.class);
    assertThat(taskFromCode.getId()).isEqualTo(createdTask.getId());
    assertThat(taskFromCode.getTaskCode()).isEqualTo(createdTask.getTaskCode());
  }

  @Test
  void create_task_without_project_fails() throws Exception {
    final TaskDto taskDto = TaskDto.builder()
        .withProjectId(UUID.randomUUID())
        .withName("Orphan Task Name")
        .withDescription("Orphan Task")
        .build();

    mockMvc.perform(post("/api/mission-control/v1/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(taskDto)))
        .andExpect(status().isNotFound());
  }

  @Test
  void assign_user_to_task_ok() throws Exception {
    // 1. Create Project
    final ProjectDto projectDto = ProjectDto.builder()
        .withName("Project " + UUID.randomUUID())
        .withPrefix("ASG" + UUID.randomUUID().toString().substring(0, 4))
        .withStatus(MissionStatus.BACKLOG)
        .build();
    final String projectResponse = mockMvc.perform(post("/api/mission-control/v1/projects")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(projectDto)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    final ProjectDto createdProject = jsonMapper.readValue(projectResponse, ProjectDto.class);

    // 2. Create Task
    final TaskDto taskDto = TaskDto.builder()
        .withProjectId(createdProject.getId())
        .withName("Task to assign Name")
        .withDescription("Task to assign")
        .withStatus(MissionStatus.BACKLOG)
        .build();
    final String taskResponse = mockMvc.perform(post("/api/mission-control/v1/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(taskDto)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    final TaskDto createdTask = jsonMapper.readValue(taskResponse, TaskDto.class);

    // 3. Create User
    final UserDto userDto = UserDto.builder().withUserName("testuser-" + UUID.randomUUID()).build();
    final String userResponse = mockMvc.perform(post("/api/mission-control/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(userDto)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    final UserDto createdUser = jsonMapper.readValue(userResponse, UserDto.class);

    // 4. Assign User
    final String assignedTaskResponse = mockMvc.perform(patch(
            "/api/mission-control/v1/tasks/" + createdTask.getId() + "/assign/" + createdUser.getId()))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    final TaskDto assignedTask = jsonMapper.readValue(assignedTaskResponse, TaskDto.class);
    assertThat(assignedTask.getAssignedUserId()).isEqualTo(createdUser.getId());
  }

  @Test
  void update_task_assigned_user_ok() throws Exception {
    // 1. Create Project
    final ProjectDto projectDto = ProjectDto.builder()
        .withName("Project Update " + UUID.randomUUID())
        .withPrefix("UPD" + UUID.randomUUID().toString().substring(0, 4))
        .withStatus(MissionStatus.BACKLOG)
        .build();
    final String projectResponse = mockMvc.perform(post("/api/mission-control/v1/projects")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(projectDto)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    final ProjectDto createdProject = jsonMapper.readValue(projectResponse, ProjectDto.class);

    // 2. Create Task
    final TaskDto taskDto = TaskDto.builder()
        .withProjectId(createdProject.getId())
        .withName("Task to update Name")
        .withDescription("Task to update")
        .withStatus(MissionStatus.BACKLOG)
        .build();
    final String taskResponse = mockMvc.perform(post("/api/mission-control/v1/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(taskDto)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    final TaskDto createdTask = jsonMapper.readValue(taskResponse, TaskDto.class);

    // 3. Create User
    final UserDto userDto = UserDto.builder().withUserName("updateuser-" + UUID.randomUUID())
        .build();
    final String userResponse = mockMvc.perform(post("/api/mission-control/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(userDto)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    final UserDto createdUser = jsonMapper.readValue(userResponse, UserDto.class);

    // 4. Update Task with assigned user
    final TaskDto updateDto = TaskDto.builder()
        .withName("Updated Task Name")
        .withAssignedUserId(createdUser.getId())
        .build();

    final String updatedTaskResponse = mockMvc.perform(
            put("/api/mission-control/v1/tasks/" + createdTask.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(updateDto)))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();

    final TaskDto updatedTask = jsonMapper.readValue(updatedTaskResponse, TaskDto.class);
    assertThat(updatedTask.getName()).isEqualTo("Updated Task Name");
    assertThat(updatedTask.getAssignedUserId()).isEqualTo(createdUser.getId());
  }

  @Test
  void move_task_to_new_status_ok() throws Exception {
    // 1. Create Project
    final ProjectDto projectDto = ProjectDto.builder()
        .withName("Project Move " + UUID.randomUUID())
        .withPrefix("MOVE" + UUID.randomUUID().toString().substring(0, 4))
        .withStatus(MissionStatus.BACKLOG)
        .build();
    final String projectResponse = mockMvc.perform(post("/api/mission-control/v1/projects")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(projectDto)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    final ProjectDto createdProject = jsonMapper.readValue(projectResponse, ProjectDto.class);

    // 2. Create Task
    final TaskDto taskDto = TaskDto.builder()
        .withProjectId(createdProject.getId())
        .withName("Task to move Name")
        .withDescription("Task to move")
        .withStatus(MissionStatus.BACKLOG)
        .build();
    final String taskResponse = mockMvc.perform(post("/api/mission-control/v1/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(taskDto)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    final TaskDto createdTask = jsonMapper.readValue(taskResponse, TaskDto.class);

    // 3. Move Task
    final String movedTaskResponse = mockMvc.perform(patch(
            "/api/mission-control/v1/tasks/" + createdTask.getId() + "/status/"
                + MissionStatus.IN_PROGRESS))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    final TaskDto movedTask = jsonMapper.readValue(movedTaskResponse, TaskDto.class);
    assertThat(movedTask.getStatus()).isEqualTo(MissionStatus.IN_PROGRESS);
  }

  @Test
  void create_task_with_assigned_user_ok() throws Exception {
    // 1. Create Project
    final ProjectDto projectDto = ProjectDto.builder()
        .withName("Project with assigned task " + UUID.randomUUID())
        .withPrefix("ATSK" + UUID.randomUUID().toString().substring(0, 4))
        .withStatus(MissionStatus.BACKLOG)
        .build();
    final String projectResponse = mockMvc.perform(post("/api/mission-control/v1/projects")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(projectDto)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    final ProjectDto createdProject = jsonMapper.readValue(projectResponse, ProjectDto.class);

    // 2. Create User
    final UserDto userDto = UserDto.builder().withUserName("assignee-" + UUID.randomUUID()).build();
    final String userResponse = mockMvc.perform(post("/api/mission-control/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(userDto)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    final UserDto createdUser = jsonMapper.readValue(userResponse, UserDto.class);

    // 3. Create Task with Assigned User
    final TaskDto taskDto = TaskDto.builder()
        .withProjectId(createdProject.getId())
        .withAssignedUserId(createdUser.getId())
        .withName("Task with user Name")
        .withDescription("Task created with user")
        .withStatus(MissionStatus.BACKLOG)
        .build();

    final String taskResponse = mockMvc.perform(post("/api/mission-control/v1/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(taskDto)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();

    final TaskDto createdTask = jsonMapper.readValue(taskResponse, TaskDto.class);
    assertThat(createdTask.getAssignedUserId()).isEqualTo(createdUser.getId());
  }

  @Test
  void task_blocked_status_validation() throws Exception {
    // 1. Create Project
    final ProjectDto projectDto = ProjectDto.builder()
        .withName("Project for Task " + UUID.randomUUID())
        .withPrefix("BLK" + UUID.randomUUID().toString().substring(0, 4))
        .withStatus(MissionStatus.BACKLOG)
        .build();
    final String projectResponse = mockMvc.perform(post("/api/mission-control/v1/projects")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(projectDto)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    final ProjectDto createdProject = jsonMapper.readValue(projectResponse, ProjectDto.class);

    // 2. Create Task with BLOCKED status without reason - should fail (Conflict)
    final TaskDto taskDto = TaskDto.builder()
        .withProjectId(createdProject.getId())
        .withName("Blocked Task Name")
        .withDescription("Blocked Task")
        .withStatus(MissionStatus.BLOCKED)
        .build();
    mockMvc.perform(post("/api/mission-control/v1/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(taskDto)))
        .andExpect(status().isConflict());

    // 3. Create Task with BLOCKED status with reason - should succeed
    final TaskDto taskWithReasonDto = TaskDto.builder()
        .withProjectId(createdProject.getId())
        .withName("Blocked Task 2 Name")
        .withDescription("Blocked Task 2")
        .withStatus(MissionStatus.BLOCKED)
        .withBlockedReason("Dependency missing")
        .build();
    final String taskResponse = mockMvc.perform(post("/api/mission-control/v1/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(taskWithReasonDto)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    final TaskDto createdTask = jsonMapper.readValue(taskResponse, TaskDto.class);
    assertThat(createdTask.getBlockedReason()).isEqualTo("Dependency missing");

    // 4. Update status to IN_PROGRESS - reason should be cleared
    final String updatedTaskResponse = mockMvc.perform(patch(
            "/api/mission-control/v1/tasks/" + createdTask.getId() + "/status/"
                + MissionStatus.IN_PROGRESS))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    final TaskDto updatedTask = jsonMapper.readValue(updatedTaskResponse, TaskDto.class);
    assertThat(updatedTask.getStatus()).isEqualTo(MissionStatus.IN_PROGRESS);
    assertThat(updatedTask.getBlockedReason()).isNull();

    // 5. Update status to BLOCKED via patch status without body - should fail (Conflict)
    mockMvc.perform(patch("/api/mission-control/v1/tasks/" + createdTask.getId() + "/status/"
            + MissionStatus.BLOCKED))
        .andExpect(status().isConflict());

    // 6. Update status to BLOCKED via patch status with reason - should succeed
    final TaskDto blockedStatusDto = TaskDto.builder().withBlockedReason("New dependency issue")
        .build();
    final String reBlockedTaskResponse = mockMvc.perform(patch(
            "/api/mission-control/v1/tasks/" + createdTask.getId() + "/status/" + MissionStatus.BLOCKED)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(blockedStatusDto)))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    final TaskDto reBlockedTask = jsonMapper.readValue(reBlockedTaskResponse, TaskDto.class);
    assertThat(reBlockedTask.getStatus()).isEqualTo(MissionStatus.BLOCKED);
    assertThat(reBlockedTask.getBlockedReason()).isEqualTo("New dependency issue");
  }

  @Test
  void get_tasks_by_user_ok() throws Exception {
    // 1. Create Project
    final ProjectDto projectDto = ProjectDto.builder()
        .withName("Project for search tasks " + UUID.randomUUID())
        .withPrefix("SRCH" + UUID.randomUUID().toString().substring(0, 4))
        .withStatus(MissionStatus.BACKLOG)
        .build();
    final String projectResponse = mockMvc.perform(post("/api/mission-control/v1/projects")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(projectDto)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    final ProjectDto createdProject = jsonMapper.readValue(projectResponse, ProjectDto.class);

    // 2. Create User
    final UserDto userDto = UserDto.builder().withUserName("task-search-user-" + UUID.randomUUID())
        .build();
    final String userResponse = mockMvc.perform(post("/api/mission-control/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(userDto)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    final UserDto createdUser = jsonMapper.readValue(userResponse, UserDto.class);

    // 3. Create Tasks
    final TaskDto task1 = TaskDto.builder()
        .withProjectId(createdProject.getId())
        .withName("Task 1 Name")
        .withDescription("Task 1")
        .withAssignedUserId(createdUser.getId())
        .withStatus(MissionStatus.BACKLOG)
        .build();
    final TaskDto task2 = TaskDto.builder()
        .withProjectId(createdProject.getId())
        .withName("Task 2 Name")
        .withDescription("Task 2")
        .withAssignedUserId(createdUser.getId())
        .withStatus(MissionStatus.BACKLOG)
        .build();
    final TaskDto task3 = TaskDto.builder()
        .withProjectId(createdProject.getId())
        .withName("Task 3 Name")
        .withDescription("Task 3")
        .withStatus(MissionStatus.BACKLOG)
        .build();

    mockMvc.perform(post("/api/mission-control/v1/tasks")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonMapper.writeValueAsString(task1))).andExpect(status().isCreated());
    mockMvc.perform(post("/api/mission-control/v1/tasks")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonMapper.writeValueAsString(task2))).andExpect(status().isCreated());
    mockMvc.perform(post("/api/mission-control/v1/tasks")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonMapper.writeValueAsString(task3))).andExpect(status().isCreated());

    // 4. Get Tasks by User
    final String tasksResponse = mockMvc.perform(
            get("/api/mission-control/v1/tasks/user/" + createdUser.getId()))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();

    final List<TaskDto> tasks = jsonMapper.readValue(tasksResponse,
        new tools.jackson.core.type.TypeReference<List<TaskDto>>() {
        });
    assertThat(tasks).hasSize(2);
    assertThat(tasks).extracting(TaskDto::getDescription)
        .containsExactlyInAnyOrder("Task 1", "Task 2");
  }
}
