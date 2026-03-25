package com.gambit.labs.mission.control.utils;

import com.gambit.labs.mission.control.config.Beans;
import tools.jackson.databind.json.JsonMapper;

/**
 * Object Utility methods
 */
public class ObjectUtils {

  public static final JsonMapper JSON_MAPPER = new Beans().jsonMapper();

  /**
   * Writes the object as a json string
   * @param o - object
   * @return - jsonified string
   */
  public static String toString(Object o) {
    return JSON_MAPPER.writeValueAsString(o);
  }
}
