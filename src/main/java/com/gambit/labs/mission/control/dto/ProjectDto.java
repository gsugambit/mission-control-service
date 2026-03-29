package com.gambit.labs.mission.control.dto;

import com.gambit.labs.mission.control.dao.MissionStatus;
import com.gambit.labs.mission.control.utils.ObjectUtils;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder(setterPrefix = "with")
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProjectDto {

  private UUID id;
  private String name;
  private String description;
  private String prefix;
  private UUID assignedUserId;
  private MissionStatus status;
  private String blockedReason;
  private Instant dateCreated;
  private Instant dateModified;

  @Override
  public String toString() {
    return ObjectUtils.toString(this);
  }
}
