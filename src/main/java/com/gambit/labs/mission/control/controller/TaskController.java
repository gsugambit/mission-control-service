package com.gambit.labs.mission.control.controller;

import com.gambit.labs.mission.control.dao.MissionStatus;
import com.gambit.labs.mission.control.dto.TaskDto;
import com.gambit.labs.mission.control.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/mission-control/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDto createTask(final @RequestBody TaskDto taskDto) {
        return taskService.createTask(taskDto);
    }

    @GetMapping("/{id}")
    public TaskDto getTask(final @PathVariable UUID id) {
        return taskService.getTask(id);
    }

    @GetMapping
    public List<TaskDto> getAllTasks() {
        return taskService.getAllTasks();
    }

    @PutMapping("/{id}")
    public TaskDto updateTask(final @PathVariable UUID id, final @RequestBody TaskDto taskDto) {
        return taskService.updateTask(id, taskDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(final @PathVariable UUID id) {
        taskService.deleteTask(id);
    }

    @PatchMapping("/{id}/assign/{userId}")
    public TaskDto assignUser(final @PathVariable UUID id, final @PathVariable UUID userId) {
        return taskService.assignUser(id, userId);
    }

    @PatchMapping("/{id}/status/{status}")
    public TaskDto updateStatus(final @PathVariable UUID id, 
                                final @PathVariable MissionStatus status,
                                final @RequestBody(required = false) TaskDto taskDto) {
        final String blockedReason = taskDto != null ? taskDto.getBlockedReason() : null;
        return taskService.updateTaskStatus(id, status, blockedReason);
    }

    @GetMapping("/user/{userId}")
    public List<TaskDto> getTasksByUser(final @PathVariable UUID userId) {
        return taskService.getTasksByUserId(userId);
    }
}
