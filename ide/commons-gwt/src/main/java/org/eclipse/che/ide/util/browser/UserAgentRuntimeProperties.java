/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eclipse.che.ide.util.browser;

import com.google.gwt.core.client.GWT;
import org.eclipse.che.ide.util.Box;

/** Class to contain run-time checks of a user-agent's capabilities. */
public class UserAgentRuntimeProperties {
  private static final UserAgentRuntimeProperties INSTANCE = createInstance();

  private static UserAgentRuntimeProperties createInstance() {
    return GWT.isScript()
        ? new UserAgentRuntimeProperties(getNativeUserAgent())
        : new UserAgentRuntimeProperties("");
  }

  public static UserAgentRuntimeProperties get() {
    return INSTANCE;
  }

  private final String userAgent;
  private final int version;
  private final boolean isMac;
  private final boolean isWin;
  private final boolean isLinux;
  private final boolean isIe7;
  private final boolean isIe8;
  private final boolean isChrome;

  public UserAgentRuntimeProperties(String userAgent) {
    this.userAgent = userAgent;
    this.version = calculateVersion(userAgent);
    this.isMac = calculateIsMac(userAgent);
    this.isWin = calculateIsWin(userAgent);
    this.isLinux = calculateIsLinux(userAgent);
    this.isIe7 = calculateIe7(userAgent);
    this.isIe8 = calculateIe8(userAgent);
    this.isChrome = calculateIsChrome(userAgent);
  }

  public String getUserAgent() {
    return userAgent;
  }

  public boolean isMac() {
    return isMac;
  }

  public boolean isWin() {
    return isWin;
  }

  public boolean isLinux() {
    return isLinux;
  }

  public boolean isIe7() {
    return isIe7;
  }

  public boolean isIe8() {
    return isIe8;
  }

  public boolean isChrome() {
    return isChrome;
  }

  /**
   * @return whether the current user agent version is at least the one given by the method
   *     parameters.
   */
  public boolean isAtLeastVersion(int major, int minor) {
    return version >= (major * 1000 + minor);
  }

  /**
   * Do not use this for program logic - for debugging only. For program logic, instead use {@link
   * #isAtLeastVersion(int, int)}
   */
  public int getMajorVer() {
    return version / 1000;
  }

  /**
   * Do not use this for program logic - for debugging only. For program logic, instead use {@link
   * #isAtLeastVersion(int, int)}
   */
  public int getMinorVer() {
    return version % 1000;
  }

  private static native String getNativeUserAgent() /*-{
        return navigator.userAgent;
    }-*/;

  private static boolean calculateIe7(String userAgent) {
    return userAgent.indexOf(" MSIE 7.") != -1;
  }

  private static boolean calculateIe8(String userAgent) {
    return userAgent.indexOf(" MSIE 8.") != -1;
  }

  private static boolean calculateIsMac(String userAgent) {
    return userAgent.indexOf("Mac") != -1;
  }

  private static boolean calculateIsWin(String userAgent) {
    return userAgent.indexOf("Windows") != -1;
  }

  private static boolean calculateIsLinux(String userAgent) {
    return userAgent.indexOf("Linux") != -1;
  }

  private static boolean calculateIsChrome(String userAgent) {
    return userAgent.indexOf("Chrome") != -1;
  }

  private static int calculateVersion(String userAgent) {
    if (userAgent == null || userAgent.isEmpty()) {
      return -1;
    }

    //  TODO(user): Make this work after regex deps are fixed and don't break static rendering
    //
    //    String regexps[] = {"firefox.([0-9]+).([0-9]+)",
    //                        "webkit.([0-9]+).([0-9]+)",
    //                        "msie.([0-9]+).([0-9]+)",
    //                        "minefield.([0-9]+).([0-9]+)"};

    //  TODO(user): Don't use "firefox" and "minefield", check Gecko rv.
    String names[] = {"firefox", "webkit", "msie", "minefield"};

    for (String name : names) {
      int v = calculateVersion(name, userAgent);
      if (v >= 0) {
        return v;
      }
    }
    return -1;
  }

  //  /**
  //   * Matches a browser-specific regular expression against the user agent to
  //   * obtain a version number.
  //   *
  //   * @param regexp The browser-specific regular expression to use
  //   * @param userAgent The user agent string to check
  //   * @return A version number or -1 if unknown
  //   */

  /**
   * Matches a browser-specific name against the user agent to obtain a version number.
   *
   * @param name The browser-specific name to use
   * @param userAgent The user agent string to check
   * @return A version number or -1 if unknown
   */
  private static int calculateVersion(String name, String userAgent) {
    int index = userAgent.toLowerCase().indexOf(name);
    if (index == -1) {
      return -1;
    }

    Box<Integer> output = Box.create();

    index += name.length() + 1;

    if ((index = consumeDigit(index, userAgent, output)) == -1) {
      return -1;
    }
    int major = output.boxed;

    index++;

    if ((index = consumeDigit(index, userAgent, output)) == -1) {
      return -1;
    }
    int minor = output.boxed;

    return major * 1000 + minor;

    //  TODO(user): Make this work after regex deps are fixed and don't break static rendering
    //
    //    RegExp pattern = RegExp.compile(regexp);
    //    MatchResult result = pattern.exec(userAgent.toLowerCase());
    //    if (result != null && result.getGroupCount() == 3) {
    //      int major = Integer.parseInt(result.getGroup(1));
    //      int minor = Integer.parseInt(result.getGroup(2));
    //      return major * 1000 + minor;
    //    }
    //    return -1;
  }

  private static int consumeDigit(int index, String str, Box<Integer> output) {
    StringBuilder nb = new StringBuilder();

    char c;
    while (index < str.length() && Character.isDigit((c = str.charAt(index)))) {
      nb.append(c);
      index++;
    }

    if (nb.length() == 0) {
      return -1;
    }

    try {
      output.boxed = Integer.parseInt(nb.toString());
      return index;
    } catch (NumberFormatException e) {
      return -1;
    }
  }
}
