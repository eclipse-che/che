// Copyright 2012 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.eclipse.che.ide.util.loging;

import com.google.gwt.user.client.Window;

/**
 * Deferred bound class to determing statically whether or not logging is enabled.
 *
 * <p>This is package protected all the way and only used internally by {@link Log}.
 */
class LogConfig {
  public static enum LogLevel {
    DEBUG,
    INFO,
    WARNING,
    ERROR
  }

  private static final String LOG_LEVEL_PARAM = "logLevel";

  private static final LogConfig INSTANCE = new LogConfig();

  static LogLevel getLogLevel() {
    return INSTANCE.getLogLevelImpl();
  }

  static void setLogLevel(LogLevel level) {
    INSTANCE.setLogLevelImpl(level);
  }

  private LogLevel currentLevel = null;

  private void ensureLogLevel() {
    if (currentLevel == null) {
      // First inspect the URL to see if it has one set.
      setLogLevel(maybeGetLevelFromUrl());

      // If it is still not set, make the default be INFO.
      setLogLevel((currentLevel == null) ? LogLevel.INFO : currentLevel);
    }
  }

  private LogLevel getLogLevelImpl() {
    ensureLogLevel();
    return currentLevel;
  }

  private LogLevel maybeGetLevelFromUrl() {
    String levelStr = Window.Location.getParameter(LOG_LEVEL_PARAM);

    // The common case.
    if (levelStr == null) {
      return null;
    }

    levelStr = levelStr.toUpperCase();

    try {
      // Extract the correct Enum value;
      return LogLevel.valueOf(levelStr);
    } catch (IllegalArgumentException e) {
      // We had a String but it was malformed.
      return null;
    }
  }

  private void setLogLevelImpl(LogLevel level) {
    currentLevel = level;
  }
}
