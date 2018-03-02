/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.commons.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.LifeCycle;
import java.util.Arrays;

public class EnvironmentVariablesLogLevelPropagator extends ContextAwareBase
    implements LoggerContextListener, LifeCycle {

  private boolean isStarted;

  private void setLoggerLevel(String loggerConfig) {
    int i = loggerConfig.indexOf('=');
    if (i < 0) {
      return;
    }
    String loggerName = loggerConfig.substring(0, i);
    String levelStr = loggerConfig.substring(i + 1);
    if (loggerName == null) {
      return;
    }
    if (levelStr == null) {
      return;
    }
    loggerName = loggerName.trim();
    levelStr = levelStr.trim();

    addInfo("Trying to set level " + levelStr + " to logger " + loggerName);
    LoggerContext lc = (LoggerContext) context;

    Logger logger = lc.getLogger(loggerName);
    if ("null".equalsIgnoreCase(levelStr)) {
      logger.setLevel(null);
    } else {
      Level level = Level.toLevel(levelStr, null);
      if (level != null) {
        logger.setLevel(level);
      }
    }
  }

  @Override
  public void start() {

    String config = System.getenv("CHE_LOGGER_CONFIG");
    if (config != null && !config.isEmpty()) {
      Arrays.stream(config.split(",")).map(String::trim).forEach(this::setLoggerLevel);
    }
    isStarted = true;
  }

  public void stop() {
    isStarted = false;
  }

  public boolean isStarted() {
    return isStarted;
  }

  @Override
  public boolean isResetResistant() {
    return false;
  }

  @Override
  public void onStart(LoggerContext context) {}

  @Override
  public void onReset(LoggerContext context) {}

  @Override
  public void onStop(LoggerContext context) {}

  @Override
  public void onLevelChange(Logger logger, Level level) {}
}
