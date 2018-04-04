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
package org.eclipse.che.infrastructure.docker.client.params;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import org.eclipse.che.infrastructure.docker.client.DockerConnector;

/**
 * Arguments holder for {@link DockerConnector#top(TopParams)}.
 *
 * @author Mykola Morhun
 */
public class TopParams {

  private String container;
  private String[] psArgs;

  /**
   * Creates arguments holder with required parameters.
   *
   * @param container container id or name
   * @return arguments holder with required parameters
   * @throws NullPointerException if {@code container} is null
   */
  public static TopParams create(@NotNull String container) {
    return new TopParams().withContainer(container);
  }

  private TopParams() {}

  /**
   * Adds container to this parameters.
   *
   * @param container container id or name
   * @return this params instance
   * @throws NullPointerException if {@code container} is null
   */
  public TopParams withContainer(@NotNull String container) {
    requireNonNull(container);
    this.container = container;
    return this;
  }

  /**
   * Adds ps command arguments to this parameters.
   *
   * @param psArgs ps arguments to use
   * @return this params instance
   */
  public TopParams withPsArgs(String... psArgs) {
    if (psArgs.length > 0) {
      this.psArgs = psArgs;
    }
    return this;
  }

  public String getContainer() {
    return container;
  }

  public String[] getPsArgs() {
    return psArgs;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TopParams topParams = (TopParams) o;
    return Objects.equals(container, topParams.container)
        && Arrays.equals(psArgs, topParams.psArgs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(container, Arrays.hashCode(psArgs));
  }
}
