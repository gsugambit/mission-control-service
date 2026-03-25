package com.gambit.labs.mission.control.service;

import com.gambit.labs.mission.control.dao.ProjectDao;
import com.gambit.labs.mission.control.dao.TaskDao;
import com.gambit.labs.mission.control.dao.MissionStatus;
import com.gambit.labs.mission.control.dao.UserDao;
import com.gambit.labs.mission.control.dto.TaskDto;
import com.gambit.labs.mission.control.exception.DataViolationException;
import com.gambit.labs.mission.control.exception.NotFoundException;
import com.gambit.labs.mission.control.repository.ProjectRepository;
import com.gambit.labs.mission.control.repository.TaskRepository;
import com.gambit.labs.mission.control.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public TaskDto createTask(final TaskDto taskDto) {
        final ProjectDao projectDao = projectRepository.findById(taskDto.getProjectId())
                .orElseThrow(() -> new NotFoundException("Project not found with id: " + taskDto.getProjectId()));

        UserDao assignedUser = null;
        if (taskDto.getAssignedUserId() != null) {
            assignedUser = userRepository.findById(taskDto.getAssignedUserId())
                    .orElseThrow(() -> new NotFoundException("User not found with id: " + taskDto.getAssignedUserId()));
        }

        final MissionStatus status = taskDto.getStatus() != null ? taskDto.getStatus() : MissionStatus.BACKLOG;
        validateBlockedReason(status, taskDto.getBlockedReason());

        final TaskDao taskDao = TaskDao.builder()
                .withProject(projectDao)
                .withAssignedUser(assignedUser)
                .withStatus(status)
                .withBlockedReason(taskDto.getBlockedReason())
                .withDescription(taskDto.getDescription())
                .withAcceptanceCriteria(taskDto.getAcceptanceCriteria())
                .build();

        final TaskDao savedTask = taskRepository.save(taskDao);
        LOGGER.info("Created task with id: {} for project id: {}", savedTask.getId(), projectDao.getId());

        return mapToDto(savedTask);
    }

    @Transactional(readOnly = true)
    public TaskDto getTask(final UUID id) {
        return taskRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new NotFoundException("Task not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<TaskDto> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public TaskDto updateTask(final UUID id, final TaskDto taskDto) {
        final TaskDao taskDao = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found with id: " + id));

        if (taskDto.getStatus() != null) {
            validateBlockedReason(taskDto.getStatus(), taskDto.getBlockedReason());
            updateStatusAndBlockedReason(taskDao, taskDto.getStatus(), taskDto.getBlockedReason());
        }
        taskDao.setDescription(taskDto.getDescription());
        taskDao.setAcceptanceCriteria(taskDto.getAcceptanceCriteria());

        final TaskDao updatedTask = taskRepository.save(taskDao);
        LOGGER.info("Updated task with id: {}", updatedTask.getId());

        return mapToDto(updatedTask);
    }

    public void deleteTask(final UUID id) {
        if (!taskRepository.existsById(id)) {
            throw new NotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
        LOGGER.info("Deleted task with id: {}", id);
    }

    public TaskDto assignUser(final UUID taskId, final UUID userId) {
        final TaskDao taskDao = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found with id: " + taskId));

        final UserDao userDao = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        taskDao.setAssignedUser(userDao);
        final TaskDao updatedTask = taskRepository.save(taskDao);
        LOGGER.info("Assigned user with id: {} to task with id: {}", userId, taskId);

        return mapToDto(updatedTask);
    }

    public TaskDto updateTaskStatus(final UUID taskId, final MissionStatus status, final String blockedReason) {
        final TaskDao taskDao = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found with id: " + taskId));

        validateBlockedReason(status, blockedReason);
        updateStatusAndBlockedReason(taskDao, status, blockedReason);
        
        final TaskDao updatedTask = taskRepository.save(taskDao);
        LOGGER.info("Updated status to {} for task with id: {}", status, taskId);

        return mapToDto(updatedTask);
    }

    @Transactional(readOnly = true)
    public List<TaskDto> getTasksByUserId(final UUID userId) {
        return taskRepository.findAllByAssignedUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private TaskDto mapToDto(final TaskDao taskDao) {
        return TaskDto.builder()
                .withId(taskDao.getId())
                .withProjectId(taskDao.getProject().getId())
                .withAssignedUserId(taskDao.getAssignedUser() != null ? taskDao.getAssignedUser().getId() : null)
                .withStatus(taskDao.getStatus())
                .withBlockedReason(taskDao.getBlockedReason())
                .withDescription(taskDao.getDescription())
                .withAcceptanceCriteria(taskDao.getAcceptanceCriteria())
                .withDateCreated(taskDao.getDateCreated())
                .withDateModified(taskDao.getDateModified())
                .build();
    }

    private void validateBlockedReason(final MissionStatus status, final String blockedReason) {
        if (MissionStatus.BLOCKED.equals(status) && (blockedReason == null || blockedReason.isBlank())) {
            throw new DataViolationException("Blocked reason is required when status is BLOCKED");
        }
    }

    private void updateStatusAndBlockedReason(final TaskDao taskDao, final MissionStatus newStatus, final String newReason) {
        if (MissionStatus.BLOCKED.equals(taskDao.getStatus()) && !MissionStatus.BLOCKED.equals(newStatus)) {
            taskDao.setBlockedReason(null);
        } else if (MissionStatus.BLOCKED.equals(newStatus)) {
            taskDao.setBlockedReason(newReason);
        }
        taskDao.setStatus(newStatus);
    }
}
