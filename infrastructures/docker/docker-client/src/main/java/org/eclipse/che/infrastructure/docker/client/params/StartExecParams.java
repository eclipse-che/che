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
package org.eclipse.che.infrastructure.docker.client.params;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import javax.validation.constraints.NotNull;
import org.eclipse.che.infrastructure.docker.client.DockerConnector;
import org.eclipse.che.infrastructure.docker.client.MessageProcessor;

/**
 * Arguments holder for {@link DockerConnector#startExec(StartExecParams, MessageProcessor)}.
 *
 * @author Mykola Morhun
 */
public class StartExecParams {

  private String execId;
  private Boolean detach;
  private Boolean tty;

  /**
   * Creates arguments holder with required parameters.
   *
   * @param execId exec id
   * @return arguments holder with required parameters
   * @throws NullPointerException if {@code execId} is null
   */
  public static StartExecParams create(@NotNull String execId) {
    return new StartExecParams().withExecId(execId);
  }

  private StartExecParams() {}

  /**
   * Adds exec id to this parameters.
   *
   * @param execId exec id
   * @return this params instance
   * @throws NullPointerException if {@code execId} is null
   */
  public StartExecParams withExecId(@NotNull String execId) {
    requireNonNull(execId);
    this.execId = execId;
    return this;
  }

  /**
   * Adds detach flag to this parameters.
   *
   * @param detach If detach is {@code true}, API returns after starting the exec command.
   *     Otherwise, API sets up an interactive session with the exec command.
   * @return this params instance
   */
  public StartExecParams withDetach(boolean detach) {
    this.detach = detach;
    return this;
  }

  /**
   * Adds pseudo-tty flag to this parameters.
   *
   * @param tty if {@code true} then will be allocated a pseudo-TTY
   * @return this params instance
   */
  public StartExecParams withTty(boolean tty) {
    this.tty = tty;
    return this;
  }

  public String getExecId() {
    return execId;
  }

  public Boolean isDetach() {
    return detach;
  }

  public Boolean isTty() {
    return tty;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof StartExecParams)) {
      return false;
    }
    final StartExecParams that = (StartExecParams) obj;
    return Objects.equals(execId, that.execId)
        && Objects.equals(detach, that.detach)
        && Objects.equals(tty, that.tty);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(execId);
    hash = 31 * hash + Objects.hashCode(detach);
    hash = 31 * hash + Objects.hashCode(tty);
    return hash;
  }

  @Override
  public String toString() {
    return "StartExecParams{"
        + "execId='"
        + execId
        + '\''
        + ", detach="
        + detach
        + ", tty="
        + tty
        + '}';
  }
}
