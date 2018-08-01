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
import static org.eclipse.che.infrastructure.docker.client.params.ParamsUtils.requireNonEmptyArray;

import java.util.Arrays;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import org.eclipse.che.infrastructure.docker.client.DockerConnector;

/**
 * Arguments holder for {@link DockerConnector#createExec(CreateExecParams)}.
 *
 * @author Mykola Morhun
 */
public class CreateExecParams {

  private String container;
  private Boolean detach;
  private String[] cmd;
  private String user;

  /**
   * Creates arguments holder with required parameters.
   *
   * @param container info about this parameter see {@link #withContainer(String)}
   * @param cmd info about this parameter see {@link #withCmd(String[])}
   * @return arguments holder with required parameters
   * @throws NullPointerException if {@code container} or {@code cmd} is null
   */
  public static CreateExecParams create(@NotNull String container, @NotNull String[] cmd) {
    return new CreateExecParams().withContainer(container).withCmd(cmd);
  }

  private CreateExecParams() {}

  /**
   * Adds container to this parameters.
   *
   * @param container id or name of container
   * @return this params instance
   * @throws NullPointerException if {@code container} is null
   */
  public CreateExecParams withContainer(@NotNull String container) {
    requireNonNull(container);
    this.container = container;
    return this;
  }

  /**
   * Adds detach stdout & stderr flag to this parameters.
   *
   * @param detach is stdout & stderr detached
   * @return this params instance
   */
  public CreateExecParams withDetach(boolean detach) {
    this.detach = detach;
    return this;
  }

  /**
   * Adds command to run into this parameters.
   *
   * @param cmd command to run specified as a string or an array of strings
   * @return this params instance
   * @throws NullPointerException if {@code cmd} is null
   * @throws IllegalArgumentException if {@code cmd} is empty
   */
  public CreateExecParams withCmd(@NotNull String[] cmd) {
    requireNonNull(cmd);
    requireNonEmptyArray(cmd);
    if (cmd[0].isEmpty()) {
      throw new IllegalArgumentException("Create exec parameters: no command specified");
    }
    this.cmd = cmd;
    return this;
  }

  /**
   * Runs exec command as given user.
   *
   * @param user The user, and optionally, group to run the exec process inside the container.
   *     Format is one of: user, user:group, uid, or uid:gid.
   */
  public CreateExecParams withUser(String user) {
    this.user = user;
    return this;
  }

  public String getContainer() {
    return container;
  }

  public Boolean isDetach() {
    return detach;
  }

  public String[] getCmd() {
    return cmd;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CreateExecParams that = (CreateExecParams) o;
    return Objects.equals(container, that.container)
        && Objects.equals(detach, that.detach)
        && Arrays.equals(cmd, that.cmd)
        && Objects.equals(user, that.user);
  }

  @Override
  public int hashCode() {
    return Objects.hash(container, detach, Arrays.hashCode(cmd), user);
  }

  public String getUser() {
    return user;
  }

  @Override
  public String toString() {
    return "CreateExecParams{"
        + "container='"
        + container
        + '\''
        + ", detach="
        + detach
        + ", cmd="
        + Arrays.toString(cmd)
        + '}';
  }
}
