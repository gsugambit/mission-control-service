package com.gambit.labs.mission.control;

import com.gambit.labs.mission.control.repository.ProjectRepository;
import com.gambit.labs.mission.control.repository.TaskRepository;
import com.gambit.labs.mission.control.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
public class IntegrationTestBase {

  @Autowired
  protected MockMvc mockMvc;

  @Autowired
  protected JsonMapper jsonMapper;

  @Autowired
  protected UserRepository userRepository;

  @Autowired
  protected ProjectRepository projectRepository;

  @Autowired
  protected TaskRepository taskRepository;

  @AfterEach
  void cleanup() {
    taskRepository.deleteAll();
    projectRepository.deleteAll();
    userRepository.deleteAll();
  }

}
