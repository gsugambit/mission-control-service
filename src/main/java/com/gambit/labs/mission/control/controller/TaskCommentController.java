package com.gambit.labs.mission.control.controller;

import com.gambit.labs.mission.control.dto.TaskCommentCreateDto;
import com.gambit.labs.mission.control.dto.TaskCommentDto;
import com.gambit.labs.mission.control.service.TaskCommentService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mission-control/v1/tasks/{taskId}/comments")
@RequiredArgsConstructor
@CrossOrigin("*")
public class TaskCommentController {

  private final TaskCommentService taskCommentService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public TaskCommentDto addComment(
      @PathVariable final UUID taskId,
      @RequestBody final TaskCommentCreateDto createDto) {
    return taskCommentService.addComment(taskId, createDto);
  }

  @GetMapping
  public List<TaskCommentDto> getComments(@PathVariable final UUID taskId) {
    return taskCommentService.getCommentsForTask(taskId);
  }
}
