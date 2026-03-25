package com.gambit.labs.mission.control.controller;

import com.gambit.labs.mission.control.IntegrationTestBase;
import com.gambit.labs.mission.control.dao.MissionStatus;
import com.gambit.labs.mission.control.dto.ErrorResponseDto;
import com.gambit.labs.mission.control.dto.ProjectDto;
import com.gambit.labs.mission.control.dto.TaskDto;
import com.gambit.labs.mission.control.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import tools.jackson.core.type.TypeReference;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProjectControllerIntTest extends IntegrationTestBase {

    @Test
    void project_lifecycle_ok() throws Exception {
        // 1. Create Project
        final ProjectDto projectDto = ProjectDto.builder()
                .withName("Test Project")
                .withDescription("Test Description")
                .build();
        
        final String projectResponse = mockMvc.perform(post("/api/mission-control/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(projectDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        
        final ProjectDto createdProject = jsonMapper.readValue(projectResponse, ProjectDto.class);
        assertThat(createdProject.getId()).isNotNull();
        assertThat(createdProject.getName()).isEqualTo("Test Project");
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
                .withName("Test Project with Tasks")
                .build();
        
        final String projectResponse = mockMvc.perform(post("/api/mission-control/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(projectDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        
        final ProjectDto createdProject = jsonMapper.readValue(projectResponse, ProjectDto.class);

        // 2. Create Task for Project
        final TaskDto taskDto = TaskDto.builder()
                .withProjectId(createdProject.getId())
                .withDescription("Test Task")
                .withStatus(MissionStatus.BACKLOG)
                .build();

        mockMvc.perform(post("/api/mission-control/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(taskDto)))
                .andExpect(status().isCreated());

        // 3. Try to delete project with tasks (should fail)
        final String errorResponse = mockMvc.perform(delete("/api/mission-control/v1/projects/" + createdProject.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResponse().getContentAsString();
        
        final ErrorResponseDto error = jsonMapper.readValue(errorResponse, ErrorResponseDto.class);
        assertThat(error.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(error.getMessage()).contains("because it has associated tasks");
    }

    @Test
    void assign_user_to_project_ok() throws Exception {
        // 1. Create Project
        final ProjectDto projectDto = ProjectDto.builder().withName("Project to assign").build();
        final String projectResponse = mockMvc.perform(post("/api/mission-control/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(projectDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        final ProjectDto createdProject = jsonMapper.readValue(projectResponse, ProjectDto.class);

        // 2. Create User
        final UserDto userDto = UserDto.builder().withUserName("project-assignee").build();
        final String userResponse = mockMvc.perform(post("/api/mission-control/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        final UserDto createdUser = jsonMapper.readValue(userResponse, UserDto.class);

        // 3. Assign User
        final String assignedProjectResponse = mockMvc.perform(patch("/api/mission-control/v1/projects/" + createdProject.getId() + "/assign/" + createdUser.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        final ProjectDto assignedProject = jsonMapper.readValue(assignedProjectResponse, ProjectDto.class);
        assertThat(assignedProject.getAssignedUserId()).isEqualTo(createdUser.getId());
    }

    @Test
    void create_project_with_assigned_user_ok() throws Exception {
        // 1. Create User
        final UserDto userDto = UserDto.builder().withUserName("initial-assignee").build();
        final String userResponse = mockMvc.perform(post("/api/mission-control/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        final UserDto createdUser = jsonMapper.readValue(userResponse, UserDto.class);

        // 2. Create Project with Assigned User
        final ProjectDto projectDto = ProjectDto.builder()
                .withName("Initial Project")
                .withAssignedUserId(createdUser.getId())
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
                .withName("Project to move")
                .withStatus(MissionStatus.BACKLOG)
                .build();
        final String projectResponse = mockMvc.perform(post("/api/mission-control/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(projectDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        final ProjectDto createdProject = jsonMapper.readValue(projectResponse, ProjectDto.class);

        // 2. Move Project
        final String movedProjectResponse = mockMvc.perform(patch("/api/mission-control/v1/projects/" + createdProject.getId() + "/status/" + MissionStatus.IN_PROGRESS))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        final ProjectDto movedProject = jsonMapper.readValue(movedProjectResponse, ProjectDto.class);
        assertThat(movedProject.getStatus()).isEqualTo(MissionStatus.IN_PROGRESS);
    }

    @Test
    void create_project_with_status_ok() throws Exception {
        // 1. Create Project with READY status
        final ProjectDto projectDto = ProjectDto.builder()
                .withName("Ready Project")
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
                .withName("Blocked Project")
                .withStatus(MissionStatus.BLOCKED)
                .build();
        mockMvc.perform(post("/api/mission-control/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(projectDto)))
                .andExpect(status().isConflict());

        // 2. Create Project with BLOCKED status with reason - should succeed
        final ProjectDto projectWithReasonDto = ProjectDto.builder()
                .withName("Blocked Project 2")
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
        final String updatedProjectResponse = mockMvc.perform(patch("/api/mission-control/v1/projects/" + createdProject.getId() + "/status/" + MissionStatus.IN_PROGRESS))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        final ProjectDto updatedProject = jsonMapper.readValue(updatedProjectResponse, ProjectDto.class);
        assertThat(updatedProject.getStatus()).isEqualTo(MissionStatus.IN_PROGRESS);
        assertThat(updatedProject.getBlockedReason()).isNull();

        // 4. Update status to BLOCKED via patch status without body - should fail (Conflict)
        mockMvc.perform(patch("/api/mission-control/v1/projects/" + createdProject.getId() + "/status/" + MissionStatus.BLOCKED))
                .andExpect(status().isConflict());

        // 5. Update status to BLOCKED via patch status with reason - should succeed
        final ProjectDto blockedStatusDto = ProjectDto.builder().withBlockedReason("Really blocked now").build();
        final String reBlockedProjectResponse = mockMvc.perform(patch("/api/mission-control/v1/projects/" + createdProject.getId() + "/status/" + MissionStatus.BLOCKED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(blockedStatusDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        final ProjectDto reBlockedProject = jsonMapper.readValue(reBlockedProjectResponse, ProjectDto.class);
        assertThat(reBlockedProject.getStatus()).isEqualTo(MissionStatus.BLOCKED);
        assertThat(reBlockedProject.getBlockedReason()).isEqualTo("Really blocked now");
    }

    @Test
    void get_projects_by_user_ok() throws Exception {
        // 1. Create User
        final UserDto userDto = UserDto.builder().withUserName("project-search-user").build();
        final String userResponse = mockMvc.perform(post("/api/mission-control/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        final UserDto createdUser = jsonMapper.readValue(userResponse, UserDto.class);

        // 2. Create Projects assigned to user
        final ProjectDto project1 = ProjectDto.builder().withName("Project 1").withAssignedUserId(createdUser.getId()).build();
        final ProjectDto project2 = ProjectDto.builder().withName("Project 2").withAssignedUserId(createdUser.getId()).build();
        final ProjectDto project3 = ProjectDto.builder().withName("Project 3").build();

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
        final String projectsResponse = mockMvc.perform(get("/api/mission-control/v1/projects/user/" + createdUser.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        final List<ProjectDto> projects = jsonMapper.readValue(projectsResponse, new tools.jackson.core.type.TypeReference<List<ProjectDto>>() {});
        assertThat(projects).hasSize(2);
        assertThat(projects).extracting(ProjectDto::getName).containsExactlyInAnyOrder("Project 1", "Project 2");
    }
}
