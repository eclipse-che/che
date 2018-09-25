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
package org.eclipse.che.workspace.infrastructure.docker.environment.dockerfile;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.environment.InternalRecipe;

/** @author Sergii Leshchenko */
public class DockerfileEnvironment extends InternalEnvironment {
  public static final String TYPE = "dockerfile";

  private final String dockerfileContent;

  DockerfileEnvironment(
      String dockerfileContent,
      InternalRecipe recipe,
      Map<String, InternalMachineConfig> machines,
      List<Warning> warnings) {
    super(recipe, machines, warnings);
    this.dockerfileContent = dockerfileContent;
  }

  @Override
  public DockerfileEnvironment setType(String type) {
    return (DockerfileEnvironment) super.setType(type);
  }

  /** Returns the content of dockerfile. */
  public String getDockerfileContent() {
    return dockerfileContent;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof DockerfileEnvironment)) {
      return false;
    }
    final DockerfileEnvironment that = (DockerfileEnvironment) obj;
    return Objects.equals(dockerfileContent, that.dockerfileContent)
        && Objects.equals(getRecipe(), that.getRecipe())
        && Objects.equals(getMachines(), that.getMachines())
        && Objects.equals(getWarnings(), that.getWarnings())
        && Objects.equals(getType(), that.getType())
        && Objects.equals(getAttributes(), that.getAttributes());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        dockerfileContent, getRecipe(), getMachines(), getWarnings(), getType(), getAttributes());
  }

  @Override
  public String toString() {
    return "DockerfileEnvironment{"
        + "dockerfile='"
        + dockerfileContent
        + '\''
        + ", machines="
        + getMachines()
        + ", recipe="
        + getRecipe()
        + ", warnings="
        + getWarnings()
        + ", type="
        + getType()
        + ", attributes="
        + getAttributes()
        + '}';
  }
}
