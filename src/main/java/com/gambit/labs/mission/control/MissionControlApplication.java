package com.gambit.labs.mission.control;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.gambit.labs.mission.control"})
public class MissionControlApplication {

  public static void main(String[] args) {
    SpringApplication.run(MissionControlApplication.class, args);
  }
}
