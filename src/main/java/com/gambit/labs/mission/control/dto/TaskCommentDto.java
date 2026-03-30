package com.gambit.labs.mission.control.dto;

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
public class TaskCommentDto {

  private UUID id;
  private UUID taskId;
  private UUID userId;
  private String comment;
  private Instant dateCreated;
  private Instant dateModified;

  @Override
  public String toString() {
    return ObjectUtils.toString(this);
  }
}
