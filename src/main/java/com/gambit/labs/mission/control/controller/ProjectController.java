package com.gambit.labs.mission.control.controller;

import com.gambit.labs.mission.control.dao.MissionStatus;
import com.gambit.labs.mission.control.dto.ProjectDto;
import com.gambit.labs.mission.control.service.ProjectService;
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
@RequestMapping("/api/mission-control/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectDto createProject(final @RequestBody ProjectDto projectDto) {
        return projectService.createProject(projectDto);
    }

    @GetMapping("/{id}")
    public ProjectDto getProject(final @PathVariable UUID id) {
        return projectService.getProject(id);
    }

    @GetMapping
    public List<ProjectDto> getAllProjects() {
        return projectService.getAllProjects();
    }

    @PutMapping("/{id}")
    public ProjectDto updateProject(final @PathVariable UUID id, final @RequestBody ProjectDto projectDto) {
        return projectService.updateProject(id, projectDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(final @PathVariable UUID id) {
        projectService.deleteProject(id);
    }

    @PatchMapping("/{id}/assign/{userId}")
    public ProjectDto assignUser(final @PathVariable UUID id, final @PathVariable UUID userId) {
        return projectService.assignUser(id, userId);
    }

    @PatchMapping("/{id}/status/{status}")
    public ProjectDto updateStatus(final @PathVariable UUID id, 
                                   final @PathVariable MissionStatus status,
                                   final @RequestBody(required = false) ProjectDto projectDto) {
        final String blockedReason = projectDto != null ? projectDto.getBlockedReason() : null;
        return projectService.updateProjectStatus(id, status, blockedReason);
    }

    @GetMapping("/user/{userId}")
    public List<ProjectDto> getProjectsByUser(final @PathVariable UUID userId) {
        return projectService.getProjectsByUserId(userId);
    }
}
