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
import com.gambit.labs.mission.control.dao.TaskDao;
import com.gambit.labs.mission.control.dao.UserDao;
import com.gambit.labs.mission.control.dto.TaskDto;
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
class TaskServiceTest {

  @Mock
  private TaskRepository taskRepository;

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private TaskService taskService;

  @Test
  void create_task_with_null_name_fails() {
    // given
    final TaskDto taskDto = TaskDto.builder()
        .withProjectId(UUID.randomUUID())
        .withName(null)
        .withDescription("Description")
        .withStatus(MissionStatus.BACKLOG)
        .build();

    // when / then
    final InvalidRequestException exception = assertThrows(InvalidRequestException.class,
        () -> taskService.createTask(taskDto));

    assertEquals("Task name is required", exception.getMessage());
    verify(taskRepository, never()).save(any());
  }

  @Test
  void create_task_with_null_description_fails() {
    // given
    final TaskDto taskDto = TaskDto.builder()
        .withProjectId(UUID.randomUUID())
        .withName("Task name")
        .withDescription(null)
        .withStatus(MissionStatus.BACKLOG)
        .build();

    // when / then
    final InvalidRequestException exception = assertThrows(InvalidRequestException.class,
        () -> taskService.createTask(taskDto));

    assertEquals("Task description is required", exception.getMessage());
    verify(taskRepository, never()).save(any());
  }

  @Test
  void create_task_without_status_fails() {
    // given
    final UUID projectId = UUID.randomUUID();
    final TaskDto taskDto = TaskDto.builder()
        .withProjectId(projectId)
        .withName("Task name")
        .withDescription("Description")
        .withStatus(null)
        .build();

    final ProjectDao projectDao = ProjectDao.builder().withId(projectId).build();
    when(projectRepository.findById(projectId)).thenReturn(Optional.of(projectDao));

    // when / then
    final InvalidRequestException exception = assertThrows(InvalidRequestException.class,
        () -> taskService.createTask(taskDto));

    assertEquals("Task status is required", exception.getMessage());
    verify(taskRepository, never()).save(any());
  }

  @Test
  void create_task_ok() {
    // given
    final UUID projectId = UUID.randomUUID();
    final String taskName = "New Task";
    final String taskCode = "PRJ-1";
    final TaskDto taskDto = TaskDto.builder()
        .withProjectId(projectId)
        .withName(taskName)
        .withDescription("Task description")
        .withStatus(MissionStatus.BACKLOG)
        .build();

    final ProjectDao projectDao = ProjectDao.builder().withId(projectId).build();
    final TaskDao savedTask = TaskDao.builder()
        .withId(UUID.randomUUID())
        .withProject(projectDao)
        .withName(taskName)
        .withDescription("Task description")
        .withStatus(MissionStatus.BACKLOG)
        .withTaskCode(taskCode)
        .build();

    when(projectRepository.findById(projectId)).thenReturn(Optional.of(projectDao));
    when(taskRepository.saveAndFlush(any(TaskDao.class))).thenReturn(savedTask);

    // when
    final TaskDto result = taskService.createTask(taskDto);

    // then
    assertNotNull(result.getId());
    assertEquals(taskName, result.getName());
    assertEquals(MissionStatus.BACKLOG, result.getStatus());
    assertEquals(taskCode, result.getTaskCode());
    verify(taskRepository).saveAndFlush(any(TaskDao.class));
  }

  @Test
  void get_task_ok() {
    // given
    final UUID taskId = UUID.randomUUID();
    final ProjectDao projectDao = ProjectDao.builder().withId(UUID.randomUUID()).build();
    final TaskDao taskDao = TaskDao.builder()
        .withId(taskId)
        .withProject(projectDao)
        .withName("Task Name")
        .withStatus(MissionStatus.BACKLOG)
        .build();

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskDao));

    // when
    final TaskDto result = taskService.getTask(taskId);

    // then
    assertNotNull(result);
    assertEquals(taskId, result.getId());
    assertEquals("Task Name", result.getName());
  }

  @Test
  void get_task_not_found_fails() {
    // given
    final UUID taskId = UUID.randomUUID();
    when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

    // when / then
    assertThrows(com.gambit.labs.mission.control.exception.NotFoundException.class,
        () -> taskService.getTask(taskId));
  }

  @Test
  void get_task_by_code_ok() {
    // given
    final String taskCode = "PRJ-1";
    final UUID taskId = UUID.randomUUID();
    final ProjectDao projectDao = ProjectDao.builder().withId(UUID.randomUUID()).build();
    final TaskDao taskDao = TaskDao.builder()
        .withId(taskId)
        .withProject(projectDao)
        .withName("Task Name")
        .withStatus(MissionStatus.BACKLOG)
        .withTaskCode(taskCode)
        .build();

    when(taskRepository.findByTaskCode(taskCode)).thenReturn(Optional.of(taskDao));

    // when
    final TaskDto result = taskService.getTaskByCode(taskCode);

    // then
    assertNotNull(result);
    assertEquals(taskId, result.getId());
    assertEquals(taskCode, result.getTaskCode());
  }

  @Test
  void get_task_by_code_not_found_fails() {
    // given
    final String taskCode = "NON-EXISTENT";
    when(taskRepository.findByTaskCode(taskCode)).thenReturn(Optional.empty());

    // when / then
    assertThrows(com.gambit.labs.mission.control.exception.NotFoundException.class,
        () -> taskService.getTaskByCode(taskCode));
  }

  @Test
  void get_all_tasks_ok() {
    // given
    final ProjectDao projectDao = ProjectDao.builder().withId(UUID.randomUUID()).build();
    final List<TaskDao> tasks = List.of(
        TaskDao.builder().withId(UUID.randomUUID()).withProject(projectDao).build()
    );

    when(taskRepository.findAll()).thenReturn(tasks);

    // when
    final List<TaskDto> result = taskService.getAllTasks();

    // then
    assertEquals(1, result.size());
  }

  @Test
  void update_task_ok() {
    // given
    final UUID taskId = UUID.randomUUID();
    final ProjectDao projectDao = ProjectDao.builder().withId(UUID.randomUUID()).build();
    final TaskDao existingTask = TaskDao.builder()
        .withId(taskId)
        .withProject(projectDao)
        .withName("Old Name")
        .withStatus(MissionStatus.BACKLOG)
        .build();
    final TaskDto updateRequest = TaskDto.builder()
        .withName("New Name")
        .withStatus(MissionStatus.IN_PROGRESS)
        .build();

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
    when(taskRepository.save(any(TaskDao.class))).thenReturn(existingTask);

    // when
    final TaskDto result = taskService.updateTask(taskId, updateRequest);

    // then
    assertEquals("New Name", result.getName());
    assertEquals(MissionStatus.IN_PROGRESS, result.getStatus());
    verify(taskRepository).save(existingTask);
  }

  @Test
  void update_task_not_found_fails() {
    // given
    final UUID taskId = UUID.randomUUID();
    final TaskDto updateRequest = TaskDto.builder().withName("New Name").build();
    when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

    // when / then
    assertThrows(com.gambit.labs.mission.control.exception.NotFoundException.class,
        () -> taskService.updateTask(taskId, updateRequest));
  }

  @Test
  void delete_task_ok() {
    // given
    final UUID taskId = UUID.randomUUID();
    when(taskRepository.existsById(taskId)).thenReturn(true);

    // when
    taskService.deleteTask(taskId);

    // then
    verify(taskRepository).deleteById(taskId);
  }

  @Test
  void delete_task_not_found_fails() {
    // given
    final UUID taskId = UUID.randomUUID();
    when(taskRepository.existsById(taskId)).thenReturn(false);

    // when / then
    assertThrows(com.gambit.labs.mission.control.exception.NotFoundException.class,
        () -> taskService.deleteTask(taskId));
  }

  @Test
  void assign_user_to_task_ok() {
    // given
    final UUID taskId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();
    final ProjectDao projectDao = ProjectDao.builder().withId(UUID.randomUUID()).build();
    final TaskDao taskDao = TaskDao.builder().withId(taskId).withProject(projectDao).build();
    final UserDao userDao = UserDao.builder().withId(userId).build();

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskDao));
    when(userRepository.findById(userId)).thenReturn(Optional.of(userDao));
    when(taskRepository.save(any(TaskDao.class))).thenReturn(taskDao);

    // when
    final TaskDto result = taskService.assignUser(taskId, userId);

    // then
    assertEquals(userId, result.getAssignedUserId());
    verify(taskRepository).save(taskDao);
  }

  @Test
  void update_task_status_ok() {
    // given
    final UUID taskId = UUID.randomUUID();
    final ProjectDao projectDao = ProjectDao.builder().withId(UUID.randomUUID()).build();
    final TaskDao taskDao = TaskDao.builder()
        .withId(taskId)
        .withProject(projectDao)
        .withStatus(MissionStatus.BACKLOG)
        .build();

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskDao));
    when(taskRepository.save(any(TaskDao.class))).thenReturn(taskDao);

    // when
    final TaskDto result = taskService.updateTaskStatus(taskId, MissionStatus.DONE, null);

    // then
    assertEquals(MissionStatus.DONE, result.getStatus());
    verify(taskRepository).save(taskDao);
  }

  @Test
  void update_task_status_blocked_without_reason_fails() {
    // given
    final UUID taskId = UUID.randomUUID();
    final ProjectDao projectDao = ProjectDao.builder().withId(UUID.randomUUID()).build();
    final TaskDao taskDao = TaskDao.builder()
        .withId(taskId)
        .withProject(projectDao)
        .withStatus(MissionStatus.BACKLOG)
        .build();

    when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskDao));

    // when / then
    assertThrows(com.gambit.labs.mission.control.exception.DataViolationException.class,
        () -> taskService.updateTaskStatus(taskId, MissionStatus.BLOCKED, null));
  }

  @Test
  void get_tasks_by_user_id_ok() {
    // given
    final UUID userId = UUID.randomUUID();
    final ProjectDao projectDao = ProjectDao.builder().withId(UUID.randomUUID()).build();
    final List<TaskDao> tasks = List.of(
        TaskDao.builder().withId(UUID.randomUUID()).withProject(projectDao).build()
    );

    when(taskRepository.findAllByAssignedUserId(userId)).thenReturn(tasks);

    // when
    final List<TaskDto> result = taskService.getTasksByUserId(userId);

    // then
    assertEquals(1, result.size());
  }
}
