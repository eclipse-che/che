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
package org.eclipse.che.infrastructure.docker.client.params;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import javax.validation.constraints.NotNull;
import org.eclipse.che.infrastructure.docker.client.DockerConnector;

/**
 * Arguments holder for {@link DockerConnector#getExecInfo(GetExecInfoParams)}.
 *
 * @author Mykola Morhun
 */
public class GetExecInfoParams {

  private String execId;

  /**
   * Creates arguments holder with required parameters.
   *
   * @param execId exec id
   * @return arguments holder with required parameters
   */
  public static GetExecInfoParams create(@NotNull String execId) {
    return new GetExecInfoParams().withExecId(execId);
  }

  private GetExecInfoParams() {}

  /**
   * Adds exec it to this parameters.
   *
   * @param execId exec id
   * @return this params instance
   */
  public GetExecInfoParams withExecId(@NotNull String execId) {
    requireNonNull(execId);
    this.execId = execId;
    return this;
  }

  public String getExecId() {
    return execId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    GetExecInfoParams that = (GetExecInfoParams) o;
    return Objects.equals(execId, that.execId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(execId);
  }
}
