/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.commons.lang;

import java.util.Map;

/** A deserializer that resolve system properties to allow runtime configuration. */
public class Deserializer {
  /**
   * Resolve the variables of type ${my.var} for the current context from the system properties
   *
   * @param input the input value
   * @return the resolve value
   */
  public static String resolveVariables(String input) {
    return resolveVariables(input, null, true);
  }

  /**
   * Resolve the variables of type ${my.var} for the current context which is composed only of the
   * given settings
   *
   * @param input the input value
   * @param props a set of parameters to add for the variable resolution
   * @return the resolve value
   */
  public static String resolveVariables(String input, Map<String, String> props) {
    return resolveVariables(input, props, true);
  }

  /**
   * Resolve the variables of type ${my.var} for the current context which is composed of the system
   * properties and the given settings
   *
   * @param input the input value
   * @param props a set of parameters to add for the variable resolution
   * @return the resolve value
   */
  public static String resolveVariables(
      String input, Map<String, String> props, boolean includeSysProps) {
    final int NORMAL = 0;
    final int SEEN_DOLLAR = 1;
    final int IN_BRACKET = 2;
    if (input == null) {
      return input;
    }
    if (!includeSysProps && (props == null || props.size() == 0)) {
      return input;
    }
    char[] chars = input.toCharArray();
    StringBuffer buffer = new StringBuffer();
    boolean properties = false;
    int state = NORMAL;
    int start = 0;
    for (int i = 0; i < chars.length; ++i) {
      char c = chars[i];
      if (c == '$' && state != IN_BRACKET) {
        state = SEEN_DOLLAR;
      } else if (c == '{' && state == SEEN_DOLLAR) {
        buffer.append(input.substring(start, i - 1));
        state = IN_BRACKET;
        start = i - 1;
      } else if (state == SEEN_DOLLAR) {
        state = NORMAL;
      } else if (c == '}' && state == IN_BRACKET) {
        if (start + 2 == i) {
          buffer.append("${}");
        } else {
          String value = null;
          String key = input.substring(start + 2, i);

          if (props != null) {
            // Some parameters have been given thus we need to check
            // inside first
            String sValue = props.get(key);
            value = sValue == null || sValue.length() == 0 ? null : sValue;
          }
          if (value == null && includeSysProps) {
            // try to get it from the system properties
            value = System.getProperty(key);
          }

          if (value != null) {
            properties = true;
            buffer.append(value);
          }
        }
        start = i + 1;
        state = NORMAL;
      }
    }
    if (properties == false) {
      return input;
    }
    if (start != chars.length) {
      buffer.append(input.substring(start, chars.length));
    }
    return buffer.toString();
  }
}
