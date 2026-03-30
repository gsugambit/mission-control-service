package com.gambit.labs.mission.control.service;

import com.gambit.labs.mission.control.dao.TaskCommentDao;
import com.gambit.labs.mission.control.dao.TaskDao;
import com.gambit.labs.mission.control.dao.UserDao;
import com.gambit.labs.mission.control.dto.TaskCommentCreateDto;
import com.gambit.labs.mission.control.dto.TaskCommentDto;
import com.gambit.labs.mission.control.exception.InvalidRequestException;
import com.gambit.labs.mission.control.exception.NotFoundException;
import com.gambit.labs.mission.control.repository.TaskCommentRepository;
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
public class TaskCommentService {

  private final TaskCommentRepository taskCommentRepository;
  private final TaskRepository taskRepository;
  private final UserRepository userRepository;

  public TaskCommentDto addComment(final UUID taskId, final TaskCommentCreateDto createDto) {
    if (!StringUtils.hasText(createDto.getComment())) {
      throw new InvalidRequestException("Comment text is required");
    }
    if (createDto.getUserId() == null) {
      throw new InvalidRequestException("User ID is required");
    }

    final TaskDao taskDao = taskRepository.findById(taskId)
        .orElseThrow(() -> new NotFoundException("Task not found with id: " + taskId));

    final UserDao userDao = userRepository.findById(createDto.getUserId())
        .orElseThrow(
            () -> new NotFoundException("User not found with id: " + createDto.getUserId()));

    final TaskCommentDao commentDao = TaskCommentDao.builder()
        .withTask(taskDao)
        .withUser(userDao)
        .withComment(createDto.getComment())
        .build();

    final TaskCommentDao savedComment = taskCommentRepository.save(commentDao);
    LOGGER.info("Added comment to task with id: {} by user with id: {}", taskId,
        createDto.getUserId());

    return mapToDto(savedComment);
  }

  @Transactional(readOnly = true)
  public List<TaskCommentDto> getCommentsForTask(final UUID taskId) {
    if (!taskRepository.existsById(taskId)) {
      throw new NotFoundException("Task not found with id: " + taskId);
    }
    return taskCommentRepository.findAllByTaskIdOrderByDateCreatedAsc(taskId).stream()
        .map(this::mapToDto)
        .collect(Collectors.toList());
  }

  private TaskCommentDto mapToDto(final TaskCommentDao commentDao) {
    return TaskCommentDto.builder()
        .withId(commentDao.getId())
        .withTaskId(commentDao.getTask().getId())
        .withUserId(commentDao.getUser().getId())
        .withComment(commentDao.getComment())
        .withDateCreated(commentDao.getDateCreated())
        .withDateModified(commentDao.getDateModified())
        .build();
  }
}
