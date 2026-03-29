package com.gambit.labs.mission.control.service;

import com.gambit.labs.mission.control.dao.MissionStatus;
import com.gambit.labs.mission.control.dao.ProjectDao;
import com.gambit.labs.mission.control.dao.UserDao;
import com.gambit.labs.mission.control.dto.ProjectDto;
import com.gambit.labs.mission.control.exception.DataViolationException;
import com.gambit.labs.mission.control.exception.InvalidRequestException;
import com.gambit.labs.mission.control.exception.NotFoundException;
import com.gambit.labs.mission.control.repository.ProjectRepository;
import com.gambit.labs.mission.control.repository.TaskRepository;
import com.gambit.labs.mission.control.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProjectService {

  private final ProjectRepository projectRepository;
  private final TaskRepository taskRepository;
  private final UserRepository userRepository;

  public ProjectDto createProject(final ProjectDto projectDto) {
    if (!StringUtils.hasText(projectDto.getName())) {
      throw new InvalidRequestException("Project name is required");
    }

    if (!StringUtils.hasText(projectDto.getPrefix())) {
      throw new InvalidRequestException("Project prefix is required");
    }

    if (projectRepository.existsByName(projectDto.getName())) {
      throw new DataViolationException("Project with the same name already exists");
    }

    UserDao assignedUser = null;
    if (projectDto.getAssignedUserId() != null) {
      assignedUser = userRepository.findById(projectDto.getAssignedUserId())
          .orElseThrow(() -> new NotFoundException(
              "User not found with id: " + projectDto.getAssignedUserId()));
    }

    if (projectDto.getStatus() == null) {
      throw new InvalidRequestException("Project status is required");
    }

    validateBlockedReason(projectDto.getStatus(), projectDto.getBlockedReason());

    final ProjectDao projectDao = ProjectDao.builder()
        .withName(projectDto.getName())
        .withDescription(projectDto.getDescription())
        .withPrefix(projectDto.getPrefix())
        .withStatus(projectDto.getStatus())
        .withBlockedReason(projectDto.getBlockedReason())
        .withAssignedUser(assignedUser)
        .build();

    final ProjectDao savedProject = projectRepository.save(projectDao);
    LOGGER.info("Created project with id: {}", savedProject.getId());
    return mapToDto(savedProject);
  }

  @Transactional(readOnly = true)
  public ProjectDto getProject(final UUID id) {
    return projectRepository.findById(id)
        .map(this::mapToDto)
        .orElseThrow(() -> new NotFoundException("Project not found with id: " + id));
  }

  @Transactional(readOnly = true)
  public List<ProjectDto> getAllProjects() {
    return projectRepository.findAll().stream()
        .map(this::mapToDto)
        .collect(Collectors.toList());
  }

  public ProjectDto updateProject(final UUID id, final ProjectDto projectDto) {
    final ProjectDao projectDao = projectRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Project not found with id: " + id));

    if (StringUtils.hasText(projectDto.getName())) {
      if (!projectDao.getName().equals(projectDto.getName())
          && projectRepository.existsByName(projectDto.getName())) {
        throw new DataViolationException("Project with the same name already exists");
      }
      projectDao.setName(projectDto.getName());
    }

    if (!StringUtils.hasText(projectDto.getPrefix())) {
      throw new InvalidRequestException("Project prefix is required");
    }

    projectDao.setDescription(projectDto.getDescription());
    projectDao.setPrefix(projectDto.getPrefix());
    if (projectDto.getStatus() != null) {
      validateBlockedReason(projectDto.getStatus(), projectDto.getBlockedReason());
      updateStatusAndBlockedReason(projectDao, projectDto.getStatus(),
          projectDto.getBlockedReason());
    }

    final ProjectDao updatedProject = projectRepository.save(projectDao);
    LOGGER.info("Updated project with id: {}", updatedProject.getId());

    return mapToDto(updatedProject);
  }

  public void deleteProject(final UUID id) {
    if (!projectRepository.existsById(id)) {
      throw new NotFoundException("Project not found with id: " + id);
    }

    if (taskRepository.existsByProjectId(id)) {
      throw new DataViolationException(
          "Cannot delete project with id: " + id + " because it has associated tasks.");
    }

    projectRepository.deleteById(id);
    LOGGER.info("Deleted project with id: {}", id);
  }

  public ProjectDto assignUser(final UUID projectId, final UUID userId) {
    final ProjectDao projectDao = projectRepository.findById(projectId)
        .orElseThrow(() -> new NotFoundException("Project not found with id: " + projectId));

    final UserDao userDao = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

    projectDao.setAssignedUser(userDao);
    final ProjectDao updatedProject = projectRepository.save(projectDao);
    LOGGER.info("Assigned user with id: {} to project with id: {}", userId, projectId);

    return mapToDto(updatedProject);
  }

  public ProjectDto updateProjectStatus(final UUID projectId, final MissionStatus status,
      final String blockedReason) {
    final ProjectDao projectDao = projectRepository.findById(projectId)
        .orElseThrow(() -> new NotFoundException("Project not found with id: " + projectId));

    validateBlockedReason(status, blockedReason);
    updateStatusAndBlockedReason(projectDao, status, blockedReason);

    final ProjectDao updatedProject = projectRepository.save(projectDao);
    LOGGER.info("Updated status to {} for project with id: {}", status, projectId);

    return mapToDto(updatedProject);
  }

  @Transactional(readOnly = true)
  public List<ProjectDto> getProjectsByUserId(final UUID userId) {
    return projectRepository.findAllByAssignedUserId(userId).stream()
        .map(this::mapToDto)
        .collect(Collectors.toList());
  }

  private ProjectDto mapToDto(final ProjectDao projectDao) {
    return ProjectDto.builder()
        .withId(projectDao.getId())
        .withName(projectDao.getName())
        .withDescription(projectDao.getDescription())
        .withPrefix(projectDao.getPrefix())
        .withAssignedUserId(
            projectDao.getAssignedUser() != null ? projectDao.getAssignedUser().getId() : null)
        .withStatus(projectDao.getStatus())
        .withBlockedReason(projectDao.getBlockedReason())
        .withDateCreated(projectDao.getDateCreated())
        .withDateModified(projectDao.getDateModified())
        .build();
  }

  private void validateBlockedReason(final MissionStatus status, final String blockedReason) {
    if (MissionStatus.BLOCKED.equals(status) && !StringUtils.hasText(blockedReason)) {
      throw new DataViolationException("Blocked reason is required when status is BLOCKED");
    }
  }

  private void updateStatusAndBlockedReason(final ProjectDao projectDao,
      final MissionStatus newStatus, final String newReason) {
    if (MissionStatus.BLOCKED.equals(projectDao.getStatus()) && !MissionStatus.BLOCKED.equals(
        newStatus)) {
      projectDao.setBlockedReason(null);
    } else if (MissionStatus.BLOCKED.equals(newStatus)) {
      projectDao.setBlockedReason(newReason);
    }
    projectDao.setStatus(newStatus);
  }
}
