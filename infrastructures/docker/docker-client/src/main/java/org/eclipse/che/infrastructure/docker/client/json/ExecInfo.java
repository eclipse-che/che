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
package org.eclipse.che.infrastructure.docker.client.json;

/** @author Eugene Voevodin */
public class ExecInfo {

  private String id;
  private ContainerInfo container;
  private ProcessConfig processConfig;
  private boolean openStdout;
  private boolean openStderr;
  private boolean openStdin;
  private boolean running;
  private int exitCode;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public ContainerInfo getContainer() {
    return container;
  }

  public void setContainer(ContainerInfo container) {
    this.container = container;
  }

  public ProcessConfig getProcessConfig() {
    return processConfig;
  }

  public void setProcessConfig(ProcessConfig processConfig) {
    this.processConfig = processConfig;
  }

  public boolean isOpenStdout() {
    return openStdout;
  }

  public void setOpenStdout(boolean openStdout) {
    this.openStdout = openStdout;
  }

  public boolean isOpenStderr() {
    return openStderr;
  }

  public void setOpenStderr(boolean openStderr) {
    this.openStderr = openStderr;
  }

  public boolean isOpenStdin() {
    return openStdin;
  }

  public void setOpenStdin(boolean openStdin) {
    this.openStdin = openStdin;
  }

  public boolean isRunning() {
    return running;
  }

  public void setRunning(boolean running) {
    this.running = running;
  }

  public int getExitCode() {
    return exitCode;
  }

  public void setExitCode(int exitCode) {
    this.exitCode = exitCode;
  }

  @Override
  public String toString() {
    return "ExecInfo{"
        + "id='"
        + id
        + '\''
        + ", container="
        + container
        + ", processConfig="
        + processConfig
        + ", openStdout='"
        + openStdout
        + '\''
        + ", openStderr='"
        + openStderr
        + '\''
        + ", openStdin='"
        + openStdin
        + '\''
        + ", running='"
        + running
        + '\''
        + ", exitCode='"
        + exitCode
        + '\''
        + '}';
  }
}
