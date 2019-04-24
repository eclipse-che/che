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
package org.eclipse.che.workspace.infrastructure.docker.environment.dockerimage;

import static java.lang.String.format;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.environment.InternalRecipe;

/**
 * Represents an environment based on a docker image. It must be declared with exactly 1 machine.
 *
 * @author Sergii Leshchenko
 */
public class DockerImageEnvironment extends InternalEnvironment {
  public static final String TYPE = "dockerimage";

  private final String dockerImage;

  DockerImageEnvironment(
      String dockerImage,
      InternalRecipe recipe,
      Map<String, InternalMachineConfig> machines,
      List<Warning> warnings) {
    super(recipe, checkSingleEntry(machines), warnings);
    this.dockerImage = dockerImage;
  }

  private static <K, V> Map<K, V> checkSingleEntry(Map<K, V> map) {
    if (map.size() == 1) {
      return map;
    } else {
      throw new IllegalArgumentException(
          format(
              "A docker image environment must contain precisely 1 machine configuration but found %d.",
              map.size()));
    }
  }

  @Override
  public DockerImageEnvironment setType(String type) {
    return (DockerImageEnvironment) super.setType(type);
  }

  public String getDockerImage() {
    return dockerImage;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof DockerImageEnvironment)) {
      return false;
    }
    final DockerImageEnvironment that = (DockerImageEnvironment) obj;
    return Objects.equals(dockerImage, that.dockerImage)
        && Objects.equals(getRecipe(), that.getRecipe())
        && Objects.equals(getMachines(), that.getMachines())
        && Objects.equals(getWarnings(), that.getWarnings())
        && Objects.equals(getType(), that.getType())
        && Objects.equals(getAttributes(), that.getAttributes());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        dockerImage, getRecipe(), getMachines(), getWarnings(), getType(), getAttributes());
  }

  @Override
  public String toString() {
    return "DockerfileEnvironment{"
        + "dockerImage='"
        + dockerImage
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
