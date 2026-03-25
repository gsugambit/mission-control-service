package com.gambit.labs.mission.control.service;

import com.gambit.labs.mission.control.dao.UserDao;
import com.gambit.labs.mission.control.dto.UserDto;
import com.gambit.labs.mission.control.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDto createUser(final UserDto userDto) {
        final UserDao userDao = UserDao.builder()
                .withUserName(userDto.getUserName())
                .build();

        final UserDao savedUser = userRepository.save(userDao);
        LOGGER.info("Created user with id: {}", savedUser.getId());

        return mapToDto(savedUser);
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
