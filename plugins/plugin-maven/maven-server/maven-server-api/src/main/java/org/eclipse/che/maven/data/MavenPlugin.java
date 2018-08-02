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
package org.eclipse.che.maven.data;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import org.eclipse.che.maven.util.JdomUtil;
import org.jdom.Element;

/**
 * Data class for org.apache.maven.model.Plugin
 *
 * @author Evgen Vidolob
 */
public class MavenPlugin implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String groupId;
  private final String artifactId;
  private final String version;
  private final boolean isDefault;
  private final Element configuration;
  private final List<MavenPluginExecution> executions;
  private final List<MavenKey> dependencies;

  public MavenPlugin(
      String groupId,
      String artifactId,
      String version,
      boolean isDefault,
      Element configuration,
      List<MavenPluginExecution> executions,
      List<MavenKey> dependencies) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
    this.isDefault = isDefault;
    this.configuration = configuration;
    this.executions = executions;
    this.dependencies = dependencies;
  }

  public String getGroupId() {
    return groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public String getVersion() {
    return version;
  }

  public boolean isDefault() {
    return isDefault;
  }

  public Element getConfiguration() {
    return configuration;
  }

  public List<MavenPluginExecution> getExecutions() {
    return executions;
  }

  public List<MavenKey> getDependencies() {
    return dependencies;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MavenPlugin that = (MavenPlugin) o;
    return isDefault == that.isDefault
        && Objects.equals(groupId, that.groupId)
        && Objects.equals(artifactId, that.artifactId)
        && Objects.equals(version, that.version)
        && JdomUtil.isElementEquals(configuration, that.configuration)
        && Objects.equals(executions, that.executions)
        && Objects.equals(dependencies, that.dependencies);
  }

  @Override
  public int hashCode() {
    return 31 * Objects.hash(groupId, artifactId, version, isDefault, executions, dependencies)
        + (configuration != null ? JdomUtil.getElementHash(configuration) : 0);
  }

  public Element getGoalConfiguration(String goal) {
    for (MavenPluginExecution execution : executions) {
      if (execution.getGoals().contains(goal)) {
        return execution.getConfiguration();
      }
    }
    return null;
  }
}
