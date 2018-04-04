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
package org.eclipse.che.workspace.infrastructure.docker.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Describes how to build image for container creation.
 *
 * @author Alexander Garagatyi
 */
public class DockerBuildContext {
  private Map<String, String> args;
  private String context;
  private String dockerfilePath;
  private String dockerfileContent;
  private Long cpuPeriod;
  private Long cpuQuota;
  private String cpuSet;

  public DockerBuildContext() {}

  public DockerBuildContext(
      String context, String dockerfilePath, String dockerfileContent, Map<String, String> args) {
    this.context = context;
    this.dockerfilePath = dockerfilePath;
    this.dockerfileContent = dockerfileContent;
    if (args != null) {
      this.args = new HashMap<>(args);
    }
  }

  public DockerBuildContext(DockerBuildContext buildContext) {
    this(
        buildContext.getContext(),
        buildContext.getDockerfilePath(),
        buildContext.getDockerfileContent(),
        buildContext.getArgs());
    this.cpuPeriod = buildContext.getCpuPeriod();
    this.cpuQuota = buildContext.getCpuQuota();
    this.cpuSet = buildContext.getCpuSet();
  }

  /**
   * Build context.
   *
   * <p>Can be git repository, url to Dockerfile.
   */
  public String getContext() {
    return context;
  }

  public DockerBuildContext setContext(String context) {
    this.context = context;
    return this;
  }

  /**
   * Path to alternate Dockerfile, including name.
   *
   * <p>Needed if dockerfile has non-default name or is not placed in the root of build context.
   * <br>
   * Mutually exclusive with {@code #getDockerfileContent()}.
   */
  public String getDockerfilePath() {
    return dockerfilePath;
  }

  public DockerBuildContext setDockerfilePath(String dockerfilePath) {
    this.dockerfilePath = dockerfilePath;
    return this;
  }

  /**
   * Content of Dockerfile.
   *
   * <p>Mutually exclusive with {@code #getDockerfilePath()}.
   */
  public String getDockerfileContent() {
    return dockerfileContent;
  }

  public DockerBuildContext setDockerfileContent(String dockerfileContent) {
    this.dockerfileContent = dockerfileContent;
    return this;
  }

  /** Args for Dockerfile build. */
  public Map<String, String> getArgs() {
    if (args == null) {
      args = new HashMap<>();
    }
    return args;
  }

  public DockerBuildContext setArgs(Map<String, String> args) {
    if (args != null) {
      args = new HashMap<>(args);
    }
    this.args = args;
    return this;
  }

  public Long getCpuPeriod() {
    return cpuPeriod;
  }

  public DockerBuildContext setCpuPeriod(Long cpuPeriod) {
    this.cpuPeriod = cpuPeriod;
    return this;
  }

  public Long getCpuQuota() {
    return cpuQuota;
  }

  public DockerBuildContext setCpuQuota(Long cpuQuota) {
    this.cpuQuota = cpuQuota;
    return this;
  }

  public String getCpuSet() {
    return cpuSet;
  }

  public DockerBuildContext setCpuSet(String cpuSet) {
    this.cpuSet = cpuSet;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DockerBuildContext)) return false;
    DockerBuildContext that = (DockerBuildContext) o;
    return Objects.equals(getArgs(), that.getArgs())
        && Objects.equals(getContext(), that.getContext())
        && Objects.equals(getDockerfilePath(), that.getDockerfilePath())
        && Objects.equals(getDockerfileContent(), that.getDockerfileContent())
        && Objects.equals(getCpuPeriod(), that.getCpuPeriod())
        && Objects.equals(getCpuQuota(), that.getCpuQuota())
        && Objects.equals(getCpuSet(), that.getCpuSet());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getArgs(),
        getContext(),
        getDockerfilePath(),
        getDockerfileContent(),
        getCpuPeriod(),
        getCpuQuota(),
        getCpuSet());
  }

  @Override
  public String toString() {
    return "DockerBuildContext{"
        + "args="
        + args
        + ", context='"
        + context
        + '\''
        + ", dockerfilePath='"
        + dockerfilePath
        + '\''
        + ", dockerfileContent='"
        + dockerfileContent
        + '\''
        + ", cpuPeriod="
        + cpuPeriod
        + ", cpuQuota="
        + cpuQuota
        + ", cpuSet='"
        + cpuSet
        + '\''
        + '}';
  }
}
