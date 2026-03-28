package com.gambit.labs.mission.control.dto;

import com.gambit.labs.mission.control.dao.MissionStatus;
import com.gambit.labs.mission.control.utils.ObjectUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder(setterPrefix = "with")
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TaskDto {
    private UUID id;
    private UUID projectId;
    private UUID assignedUserId;
    private MissionStatus status;
    private String blockedReason;
    private String name;
    private String description;
    private String acceptanceCriteria;
    private Instant dateCreated;
    private Instant dateModified;

    @Override
    public String toString() {
        return ObjectUtils.toString(this);
    }
}
