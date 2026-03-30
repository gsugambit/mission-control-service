package com.gambit.labs.mission.control.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gambit.labs.mission.control.dao.UserDao;
import com.gambit.labs.mission.control.dto.UserDto;
import com.gambit.labs.mission.control.exception.DataViolationException;
import com.gambit.labs.mission.control.exception.InvalidRequestException;
import com.gambit.labs.mission.control.repository.ProjectRepository;
import com.gambit.labs.mission.control.repository.TaskRepository;
import com.gambit.labs.mission.control.repository.UserRepository;
import com.gambit.labs.mission.control.utils.TestDataUtils;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private TaskRepository taskRepository;

  @InjectMocks
  private UserService userService;

  @Test
  void create_user_with_duplicate_username_fails() {
    // given
    final String userName = TestDataUtils.makeUserName();
    final UserDto userDto = UserDto.builder().withUserName(userName).build();
    when(userRepository.existsByUserName(userName)).thenReturn(true);

    // when / then
    final DataViolationException exception = assertThrows(DataViolationException.class,
        () -> userService.createUser(userDto));

    assertEquals("User with the same name already exists", exception.getMessage());
    verify(userRepository, never()).save(any());
  }

  @Test
  void create_user_with_null_username_fails() {
    // given
    final UserDto userDto = UserDto.builder().withUserName(null).build();

    // when / then
    final InvalidRequestException exception = assertThrows(InvalidRequestException.class,
        () -> userService.createUser(userDto));

    assertEquals("User name is required", exception.getMessage());
    verify(userRepository, never()).existsByUserName(any());
    verify(userRepository, never()).save(any());
  }

  @Test
  void create_user_ok() {
    // given
    final String userName = TestDataUtils.makeUserName();
    final UserDto userDto = UserDto.builder().withUserName(userName).build();
    final UserDao savedUser = UserDao.builder()
        .withId(UUID.randomUUID())
        .withUserName(userName)
        .build();

    when(userRepository.existsByUserName(userName)).thenReturn(false);
    when(userRepository.save(any(UserDao.class))).thenReturn(savedUser);

    // when
    final UserDto result = userService.createUser(userDto);

    // then
    assertNotNull(result.getId());
    assertEquals(userName, result.getUserName());
    verify(userRepository).save(any(UserDao.class));
  }

  @Test
  void get_all_users_ok() {
    // given
    final List<UserDao> users = List.of(
        UserDao.builder().withId(UUID.randomUUID()).withUserName(TestDataUtils.makeUserName())
            .build(),
        UserDao.builder().withId(UUID.randomUUID()).withUserName(TestDataUtils.makeUserName())
            .build()
    );

    when(userRepository.findAll()).thenReturn(users);

    // when
    final List<UserDto> result = userService.getAllUsers();

    // then
    assertEquals(2, result.size());
  }

  @Test
  void delete_user_ok() {
    // given
    final UUID userId = UUID.randomUUID();
    when(userRepository.existsById(userId)).thenReturn(true);
    when(taskRepository.findAllByAssignedUserId(userId)).thenReturn(Collections.emptyList());
    when(projectRepository.findAllByAssignedUserId(userId)).thenReturn(Collections.emptyList());

    // when
    userService.deleteUser(userId);

    // then
    verify(userRepository).deleteById(userId);
  }
}
