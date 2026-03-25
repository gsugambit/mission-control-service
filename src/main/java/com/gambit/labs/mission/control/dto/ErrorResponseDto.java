package com.gambit.labs.mission.control.dto;

import com.gambit.labs.mission.control.utils.ObjectUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Getter
@Builder(setterPrefix = "with")
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorResponseDto {
    private int status;
    private String error;
    private String message;
    private Instant timestamp;

    @Override
    public String toString() {
        return ObjectUtils.toString(this);
    }
}
