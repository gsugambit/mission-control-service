package com.gambit.labs.mission.control.utils;

import org.apache.commons.lang3.RandomStringUtils;

public final class TestDataUtils {

  public static String makeUserName() {
    return RandomStringUtils.secure().nextAlphabetic(12);
  }

  public static String makeProjectName() {
    return RandomStringUtils.secure().nextAlphabetic(8);
  }
}
