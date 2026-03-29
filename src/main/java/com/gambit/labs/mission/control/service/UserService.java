package com.gambit.labs.mission.control.service;

import com.gambit.labs.mission.control.dao.UserDao;
import com.gambit.labs.mission.control.dto.UserDto;
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
public class UserService {

  private final UserRepository userRepository;
  private final ProjectRepository projectRepository;
  private final TaskRepository taskRepository;

  public UserDto createUser(final UserDto userDto) {
    if (!StringUtils.hasText(userDto.getUserName())) {
      throw new InvalidRequestException("User name is required");
    }

    if (userRepository.existsByUserName(userDto.getUserName())) {
      throw new DataViolationException("User with the same name already exists");
    }

    final UserDao userDao = UserDao.builder()
        .withUserName(userDto.getUserName())
        .build();

    final UserDao savedUser = userRepository.save(userDao);
    LOGGER.info("Created user with id: {}", savedUser.getId());
    return mapToDto(savedUser);
  }

  public List<UserDto> getAllUsers() {
    return userRepository.findAll()
        .stream()
        .map(this::mapToDto)
        .collect(Collectors.toList());
  }

  public void deleteUser(final UUID id) {
    if (!userRepository.existsById(id)) {
      throw new NotFoundException("User not found with id: " + id);
    }
    if (!taskRepository.findAllByAssignedUserId(id).isEmpty()) {
      throw new DataViolationException(
          "Cannot delete user with id: " + id + " because it is assigned to tasks.");
    }
    if (!projectRepository.findAllByAssignedUserId(id).isEmpty()) {
      throw new DataViolationException(
          "Cannot delete user with id: " + id + " because it is assigned to projects.");
    }
    userRepository.deleteById(id);
    LOGGER.info("Deleted user with id: {}", id);
  }

  private UserDto mapToDto(final UserDao userDao) {
    return UserDto.builder()
        .withId(userDao.getId())
        .withUserName(userDao.getUserName())
        .withDateCreated(userDao.getDateCreated())
        .withDateModified(userDao.getDateModified())
        .build();
  }
}
