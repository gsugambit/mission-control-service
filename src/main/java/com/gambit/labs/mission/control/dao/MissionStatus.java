package com.gambit.labs.mission.control.dao;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MissionStatus {
    BACKLOG("Backlog"),
    READY("Ready"),
    IN_PROGRESS("In Progress"),
    BLOCKED("Blocked"),
    IN_REVIEW("In Review"),
    DONE("Done");

    private final String description;
}
