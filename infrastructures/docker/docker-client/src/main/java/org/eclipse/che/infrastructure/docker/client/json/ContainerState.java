/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.infrastructure.docker.client.json;

import java.util.Objects;

/** @author andrew00x */
public class ContainerState {
  private boolean running;
  private int pid;
  private int exitCode;
  // Date format: yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX
  private String startedAt;
  private String finishedAt;
  private boolean paused;
  private boolean restarting;
  private boolean dead;
  private boolean oOMKilled;
  private String error;
  private String status;

  public boolean isRunning() {
    return running;
  }

  public void setRunning(boolean running) {
    this.running = running;
  }

  public int getPid() {
    return pid;
  }

  public void setPid(int pid) {
    this.pid = pid;
  }

  public int getExitCode() {
    return exitCode;
  }

  public void setExitCode(int exitCode) {
    this.exitCode = exitCode;
  }

  public String getStartedAt() {
    return startedAt;
  }

  public void setStartedAt(String startedAt) {
    this.startedAt = startedAt;
  }

  public String getFinishedAt() {
    return finishedAt;
  }

  public void setFinishedAt(String finishedAt) {
    this.finishedAt = finishedAt;
  }

  public boolean isPaused() {
    return paused;
  }

  public void setPaused(boolean paused) {
    this.paused = paused;
  }

  public boolean isRestarting() {
    return restarting;
  }

  public void setRestarting(boolean restarting) {
    this.restarting = restarting;
  }

  public boolean isDead() {
    return dead;
  }

  public void setDead(boolean dead) {
    this.dead = dead;
  }

  public boolean isOOMKilled() {
    return oOMKilled;
  }

  public void setOOMKilled(boolean OOMKilled) {
    this.oOMKilled = OOMKilled;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ContainerState)) return false;
    ContainerState that = (ContainerState) o;
    return isRunning() == that.isRunning()
        && getPid() == that.getPid()
        && getExitCode() == that.getExitCode()
        && isPaused() == that.isPaused()
        && isRestarting() == that.isRestarting()
        && isDead() == that.isDead()
        && oOMKilled == that.oOMKilled
        && Objects.equals(getStartedAt(), that.getStartedAt())
        && Objects.equals(getFinishedAt(), that.getFinishedAt())
        && Objects.equals(getError(), that.getError())
        && Objects.equals(getStatus(), that.getStatus());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        isRunning(),
        getPid(),
        getExitCode(),
        getStartedAt(),
        getFinishedAt(),
        isPaused(),
        isRestarting(),
        isDead(),
        oOMKilled,
        getError(),
        getStatus());
  }

  @Override
  public String toString() {
    return "ContainerState{"
        + "running="
        + running
        + ", pid="
        + pid
        + ", exitCode="
        + exitCode
        + ", startedAt='"
        + startedAt
        + '\''
        + ", finishedAt='"
        + finishedAt
        + '\''
        + ", paused="
        + paused
        + ", restarting="
        + restarting
        + ", dead="
        + dead
        + ", oOMKilled="
        + oOMKilled
        + ", error='"
        + error
        + '\''
        + '}';
  }
}
