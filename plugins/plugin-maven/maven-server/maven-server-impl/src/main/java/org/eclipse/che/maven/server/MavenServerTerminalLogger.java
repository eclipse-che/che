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
package org.eclipse.che.maven.server;

import java.rmi.RemoteException;
import org.codehaus.plexus.logging.Logger;

/**
 * Implementation of {@link Logger}. This implementation delegates all log call to {@link
 * MavenTerminal} interface.
 *
 * @author Evgen Vidolob
 */
public class MavenServerTerminalLogger implements Logger {

  private int logLevel;
  private MavenTerminal terminal;

  @Override
  public void debug(String s) {
    print(MavenTerminal.LEVEL_DEBUG, s, null);
  }

  @Override
  public void debug(String s, Throwable throwable) {
    print(MavenTerminal.LEVEL_DEBUG, s, throwable);
  }

  @Override
  public boolean isDebugEnabled() {
    return logLevel <= MavenTerminal.LEVEL_DEBUG;
  }

  @Override
  public void info(String s) {
    print(MavenTerminal.LEVEL_INFO, s, null);
  }

  @Override
  public void info(String s, Throwable throwable) {
    print(MavenTerminal.LEVEL_INFO, s, throwable);
  }

  @Override
  public boolean isInfoEnabled() {
    return logLevel <= MavenTerminal.LEVEL_INFO;
  }

  @Override
  public void warn(String s) {
    print(MavenTerminal.LEVEL_WARN, s, null);
  }

  @Override
  public void warn(String s, Throwable throwable) {
    print(MavenTerminal.LEVEL_WARN, s, throwable);
  }

  @Override
  public boolean isWarnEnabled() {
    return logLevel <= MavenTerminal.LEVEL_WARN;
  }

  @Override
  public void error(String s) {
    print(MavenTerminal.LEVEL_ERROR, s, null);
  }

  @Override
  public void error(String s, Throwable throwable) {
    print(MavenTerminal.LEVEL_ERROR, s, throwable);
  }

  @Override
  public boolean isErrorEnabled() {
    return logLevel <= MavenTerminal.LEVEL_ERROR;
  }

  @Override
  public void fatalError(String s) {
    print(MavenTerminal.LEVEL_FATAL, s, null);
  }

  @Override
  public void fatalError(String s, Throwable throwable) {
    print(MavenTerminal.LEVEL_FATAL, s, null);
  }

  @Override
  public boolean isFatalErrorEnabled() {
    return logLevel <= MavenTerminal.LEVEL_FATAL;
  }

  @Override
  public int getThreshold() {
    return logLevel;
  }

  @Override
  public void setThreshold(int i) {
    logLevel = i;
  }

  @Override
  public Logger getChildLogger(String s) {
    return null;
  }

  @Override
  public String getName() {
    return "CheMavenLogger";
  }

  public void setTerminal(MavenTerminal terminal) {
    this.terminal = terminal;
  }

  private void print(int level, String message, Throwable t) {
    if (level < logLevel) {
      return;
    }

    if (terminal != null) {
      if (!message.endsWith("\n")) {
        message += "\n";
      }
      try {
        terminal.print(level, message, t);
      } catch (RemoteException ignore) {
      }
    }
  }
}
