package com.gambit.labs.mission.control.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gambit.labs.mission.control.dao.MissionStatus;
import com.gambit.labs.mission.control.dao.ProjectDao;
import com.gambit.labs.mission.control.dao.UserDao;
import com.gambit.labs.mission.control.dto.ProjectDto;
import com.gambit.labs.mission.control.exception.DataViolationException;
import com.gambit.labs.mission.control.exception.InvalidRequestException;
import com.gambit.labs.mission.control.repository.ProjectRepository;
import com.gambit.labs.mission.control.repository.TaskRepository;
import com.gambit.labs.mission.control.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private TaskRepository taskRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private ProjectService projectService;

  @Test
  void create_project_with_duplicate_name_fails() {
    // given
    final String projectName = "Unique Project";
    final ProjectDto projectDto = ProjectDto.builder()
        .withName(projectName)
        .withPrefix("PRJ")
        .withStatus(MissionStatus.BACKLOG)
        .build();
    when(projectRepository.existsByName(projectName)).thenReturn(true);

    // when / then
    final DataViolationException exception = assertThrows(DataViolationException.class,
        () -> projectService.createProject(projectDto));

    assertEquals("Project with the same name already exists", exception.getMessage());
    verify(projectRepository, never()).save(any());
  }

  @Test
  void create_project_with_null_name_fails() {
    // given
    final ProjectDto projectDto = ProjectDto.builder().withName(null).build();

    // when / then
    final InvalidRequestException exception = assertThrows(InvalidRequestException.class,
        () -> projectService.createProject(projectDto));

    assertEquals("Project name is required", exception.getMessage());
    verify(projectRepository, never()).existsByName(any());
    verify(projectRepository, never()).save(any());
  }

  @Test
  void create_project_without_prefix_fails() {
    // given
    final ProjectDto projectDto = ProjectDto.builder()
        .withName("Test Project")
        .withPrefix(null)
        .build();

    // when / then
    final InvalidRequestException exception = assertThrows(InvalidRequestException.class,
        () -> projectService.createProject(projectDto));

    assertEquals("Project prefix is required", exception.getMessage());
    verify(projectRepository, never()).save(any());
  }

  @Test
  void update_project_with_duplicate_name_fails() {
    // given
    final UUID projectId = UUID.randomUUID();
    final String existingName = "Project A";
    final String newName = "Project B";
    final ProjectDao existingProject = ProjectDao.builder()
        .withId(projectId)
        .withName(existingName)
        .build();

    when(projectRepository.findById(projectId)).thenReturn(Optional.of(existingProject));
    when(projectRepository.existsByName(newName)).thenReturn(true);

    final ProjectDto updateRequest = ProjectDto.builder().withName(newName).build();

    // when / then
    final DataViolationException exception = assertThrows(DataViolationException.class,
        () -> projectService.updateProject(projectId, updateRequest));

    assertEquals("Project with the same name already exists", exception.getMessage());
    verify(projectRepository, never()).save(any());
  }

  @Test
  void update_project_without_prefix_fails() {
    // given
    final UUID projectId = UUID.randomUUID();
    final ProjectDao existingProject = ProjectDao.builder()
        .withId(projectId)
        .withName("Existing Project")
        .withPrefix("OLD")
        .build();

    when(projectRepository.findById(projectId)).thenReturn(Optional.of(existingProject));

    final ProjectDto updateRequest = ProjectDto.builder()
        .withPrefix(null)
        .build();

    // when / then
    final InvalidRequestException exception = assertThrows(InvalidRequestException.class,
        () -> projectService.updateProject(projectId, updateRequest));

    assertEquals("Project prefix is required", exception.getMessage());
    verify(projectRepository, never()).save(any());
  }

  @Test
  void create_project_without_status_fails() {
    // given
    final ProjectDto projectDto = ProjectDto.builder()
        .withName("No Status Project")
        .withPrefix("PRJ")
        .withStatus(null)
        .build();

    // when / then
    final InvalidRequestException exception = assertThrows(InvalidRequestException.class,
        () -> projectService.createProject(projectDto));

    assertEquals("Project status is required", exception.getMessage());
    verify(projectRepository, never()).save(any());
  }

  @Test
  void create_project_ok() {
    // given
    final String projectName = "New Project";
    final String prefix = "PRJ";
    final ProjectDto projectDto = ProjectDto.builder()
        .withName(projectName)
        .withPrefix(prefix)
        .withStatus(MissionStatus.BACKLOG)
        .build();
    final ProjectDao savedProject = ProjectDao.builder()
        .withId(UUID.randomUUID())
        .withName(projectName)
        .withPrefix(prefix)
        .withStatus(MissionStatus.BACKLOG)
        .build();

    when(projectRepository.existsByName(projectName)).thenReturn(false);
    when(projectRepository.save(any(ProjectDao.class))).thenReturn(savedProject);

    // when
    final ProjectDto result = projectService.createProject(projectDto);

    // then
    assertNotNull(result.getId());
    assertEquals(projectName, result.getName());
    assertEquals(MissionStatus.BACKLOG, result.getStatus());
    verify(projectRepository).save(any(ProjectDao.class));
  }

  @Test
  void get_project_ok() {
    // given
    final UUID projectId = UUID.randomUUID();
    final ProjectDao projectDao = ProjectDao.builder()
        .withId(projectId)
        .withName("Project Name")
        .withStatus(MissionStatus.BACKLOG)
        .build();

    when(projectRepository.findById(projectId)).thenReturn(Optional.of(projectDao));

    // when
    final ProjectDto result = projectService.getProject(projectId);

    // then
    assertNotNull(result);
    assertEquals(projectId, result.getId());
    assertEquals("Project Name", result.getName());
  }

  @Test
  void get_all_projects_ok() {
    // given
    final List<ProjectDao> projects = List.of(
        ProjectDao.builder().withId(UUID.randomUUID()).withName("Project 1").build(),
        ProjectDao.builder().withId(UUID.randomUUID()).withName("Project 2").build()
    );

    when(projectRepository.findAll()).thenReturn(projects);

    // when
    final List<ProjectDto> result = projectService.getAllProjects();

    // then
    assertEquals(2, result.size());
  }

  @Test
  void update_project_ok() {
    // given
    final UUID projectId = UUID.randomUUID();
    final ProjectDao existingProject = ProjectDao.builder()
        .withId(projectId)
        .withName("Old Name")
        .withStatus(MissionStatus.BACKLOG)
        .build();
    final ProjectDto updateRequest = ProjectDto.builder()
        .withName("New Name")
        .withPrefix("NEW")
        .withStatus(MissionStatus.IN_PROGRESS)
        .build();

    when(projectRepository.findById(projectId)).thenReturn(Optional.of(existingProject));
    when(projectRepository.existsByName("New Name")).thenReturn(false);
    when(projectRepository.save(any(ProjectDao.class))).thenReturn(existingProject);

    // when
    final ProjectDto result = projectService.updateProject(projectId, updateRequest);

    // then
    assertEquals("New Name", result.getName());
    assertEquals(MissionStatus.IN_PROGRESS, result.getStatus());
    verify(projectRepository).save(existingProject);
  }

  @Test
  void delete_project_ok() {
    // given
    final UUID projectId = UUID.randomUUID();
    when(projectRepository.existsById(projectId)).thenReturn(true);
    when(taskRepository.existsByProjectId(projectId)).thenReturn(false);

    // when
    projectService.deleteProject(projectId);

    // then
    verify(projectRepository).deleteById(projectId);
  }

  @Test
  void assign_user_to_project_ok() {
    // given
    final UUID projectId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();
    final ProjectDao projectDao = ProjectDao.builder().withId(projectId).build();
    final UserDao userDao = UserDao.builder().withId(userId).build();

    when(projectRepository.findById(projectId)).thenReturn(Optional.of(projectDao));
    when(userRepository.findById(userId)).thenReturn(Optional.of(userDao));
    when(projectRepository.save(any(ProjectDao.class))).thenReturn(projectDao);

    // when
    final ProjectDto result = projectService.assignUser(projectId, userId);

    // then
    assertEquals(userId, result.getAssignedUserId());
    verify(projectRepository).save(projectDao);
  }

  @Test
  void update_project_status_ok() {
    // given
    final UUID projectId = UUID.randomUUID();
    final ProjectDao projectDao = ProjectDao.builder()
        .withId(projectId)
        .withStatus(MissionStatus.BACKLOG)
        .build();

    when(projectRepository.findById(projectId)).thenReturn(Optional.of(projectDao));
    when(projectRepository.save(any(ProjectDao.class))).thenReturn(projectDao);

    // when
    final ProjectDto result = projectService.updateProjectStatus(projectId, MissionStatus.DONE,
        null);

    // then
    assertEquals(MissionStatus.DONE, result.getStatus());
    verify(projectRepository).save(projectDao);
  }

  @Test
  void get_projects_by_user_id_ok() {
    // given
    final UUID userId = UUID.randomUUID();
    final List<ProjectDao> projects = List.of(
        ProjectDao.builder().withId(UUID.randomUUID()).build()
    );

    when(projectRepository.findAllByAssignedUserId(userId)).thenReturn(projects);

    // when
    final List<ProjectDto> result = projectService.getProjectsByUserId(userId);

    // then
    assertEquals(1, result.size());
  }
}
