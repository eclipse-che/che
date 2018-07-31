/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.logger.slf4j.gwtbackend;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

/** A wrapper over {@link java.util.logging.Logger} supported since GWT 2.1. */
public class GwtLoggerSlf4jBackend extends MarkerIgnoringBase {
  private final Logger logger;

  public GwtLoggerSlf4jBackend(String name) {
    logger = Logger.getLogger(name);
  }

  @Override
  public boolean isTraceEnabled() {
    return logger.isLoggable(FINEST);
  }

  @Override
  public void trace(String msg) {
    log(FINEST, msg, null);
  }

  @Override
  public void trace(String format, Object arg) {
    formatAndLog(FINEST, format, arg);
  }

  @Override
  public void trace(String format, Object arg1, Object arg2) {
    formatAndLog(FINEST, format, arg1, arg2);
  }

  @Override
  public void trace(String format, Object... argArray) {
    formatAndLog(FINEST, format, argArray);
  }

  @Override
  public void trace(String msg, Throwable t) {
    log(FINEST, msg, t);
  }

  @Override
  public boolean isDebugEnabled() {
    return logger.isLoggable(FINE);
  }

  @Override
  public void debug(String msg) {
    log(FINE, msg, null);
  }

  @Override
  public void debug(String format, Object arg) {
    formatAndLog(FINE, format, arg);
  }

  @Override
  public void debug(String format, Object arg1, Object arg2) {
    formatAndLog(FINE, format, arg1, arg2);
  }

  @Override
  public void debug(String format, Object... argArray) {
    formatAndLog(FINE, format, argArray);
  }

  @Override
  public void debug(String msg, Throwable t) {
    log(FINE, msg, t);
  }

  @Override
  public boolean isInfoEnabled() {
    return logger.isLoggable(INFO);
  }

  @Override
  public void info(String msg) {
    log(INFO, msg, null);
  }

  @Override
  public void info(String format, Object arg) {
    formatAndLog(INFO, format, arg);
  }

  @Override
  public void info(String format, Object arg1, Object arg2) {
    formatAndLog(INFO, format, arg1, arg2);
  }

  @Override
  public void info(String format, Object... argArray) {
    formatAndLog(INFO, format, argArray);
  }

  @Override
  public void info(String msg, Throwable t) {
    log(INFO, msg, t);
  }

  @Override
  public boolean isWarnEnabled() {
    return logger.isLoggable(WARNING);
  }

  @Override
  public void warn(String msg) {
    log(WARNING, msg, null);
  }

  @Override
  public void warn(String format, Object arg) {
    formatAndLog(WARNING, format, arg);
  }

  @Override
  public void warn(String format, Object arg1, Object arg2) {
    formatAndLog(WARNING, format, arg1, arg2);
  }

  @Override
  public void warn(String format, Object... argArray) {
    formatAndLog(WARNING, format, argArray);
  }

  @Override
  public void warn(String msg, Throwable t) {
    log(WARNING, msg, t);
  }

  @Override
  public boolean isErrorEnabled() {
    return logger.isLoggable(SEVERE);
  }

  @Override
  public void error(String msg) {
    log(SEVERE, msg, null);
  }

  @Override
  public void error(String format, Object arg) {
    formatAndLog(SEVERE, format, arg);
  }

  @Override
  public void error(String format, Object arg1, Object arg2) {
    formatAndLog(SEVERE, format, arg1, arg2);
  }

  @Override
  public void error(String format, Object... argArray) {
    formatAndLog(SEVERE, format, argArray);
  }

  @Override
  public void error(String msg, Throwable t) {
    log(SEVERE, msg, t);
  }

  private void log(Level level, String msg, Throwable t) {
    if (logger.isLoggable(level)) {
      logger.log(level, msg, t);
    }
  }

  private void formatAndLog(Level level, String format, Object... argArray) {
    if (logger.isLoggable(level)) {
      FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
      logger.log(level, ft.getMessage(), ft.getThrowable());
    }
  }
}
