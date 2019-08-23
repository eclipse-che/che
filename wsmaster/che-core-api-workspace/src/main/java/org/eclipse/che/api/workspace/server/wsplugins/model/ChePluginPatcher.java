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

package org.eclipse.che.api.workspace.server.wsplugins.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;

/** Represent patcher for Che plugin configuration. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChePluginPatcher {

  @JsonProperty("initContainers")
  private List<CheContainer> initContainers;

  @JsonProperty("pluginTypeMatcher")
  private List<String> pluginTypeMatcher;

  private List<String> pluginContainerCommand;
  private List<String> pluginContainerArgs;

  public ChePluginPatcher initContainers(List<CheContainer> initContainers) {
    this.initContainers = initContainers;
    return this;
  }

  public List<CheContainer> getInitContainers() {
    return initContainers;
  }

  public void setInitContainers(List<CheContainer> initContainers) {
    this.initContainers = initContainers;
  }

  public ChePluginPatcher pluginTypeMatcher(List<String> pluginTypeMatcher) {
    this.pluginTypeMatcher = pluginTypeMatcher;
    return this;
  }

  public List<String> getPluginTypeMatcher() {
    return pluginTypeMatcher;
  }

  public void setPluginTypeMatcher(List<String> pluginTypeMatcher) {
    this.pluginTypeMatcher = pluginTypeMatcher;
  }

  public ChePluginPatcher pluginContainerCommand(List<String> pluginContainerCommand) {
    this.pluginContainerCommand = pluginContainerCommand;
    return this;
  }

  public List<String> getPluginContainerCommand() {
    return pluginContainerCommand;
  }

  public void setPluginContainerCommand(List<String> pluginContainerCommand) {
    this.pluginContainerCommand = pluginContainerCommand;
  }

  public ChePluginPatcher pluginContainerArgs(List<String> pluginContainerArgs) {
    this.pluginContainerArgs = pluginContainerArgs;
    return this;
  }

  public List<String> getPluginContainerArgs() {
    return pluginContainerArgs;
  }

  public void setPluginContainerArgs(List<String> pluginContainerArgs) {
    this.pluginContainerArgs = pluginContainerArgs;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ChePluginPatcher)) return false;
    ChePluginPatcher that = (ChePluginPatcher) o;
    return Objects.equals(getInitContainers(), that.getInitContainers())
        && Objects.equals(getPluginTypeMatcher(), that.getPluginTypeMatcher())
        && Objects.equals(getPluginContainerCommand(), that.getPluginContainerCommand())
        && Objects.equals(getPluginContainerArgs(), that.getPluginContainerArgs());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getInitContainers(),
        getPluginTypeMatcher(),
        getPluginContainerCommand(),
        getPluginContainerArgs());
  }

  @Override
  public String toString() {
    return "ChePluginPatcher{"
        + "initContainers="
        + initContainers
        + ", pluginTypeMatcher="
        + pluginTypeMatcher
        + ", pluginContainerCommand="
        + pluginContainerCommand
        + ", pluginContainerArgs="
        + pluginContainerArgs
        + '}';
  }
}
