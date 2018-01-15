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
import static org.eclipse.che.infrastructure.docker.client.params.ParamsUtils.requireNonEmptyArray;
import static org.eclipse.che.infrastructure.docker.client.params.ParamsUtils.requireNonNullNorEmpty;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import org.eclipse.che.infrastructure.docker.client.DockerConnector;
import org.eclipse.che.infrastructure.docker.client.ProgressMonitor;
import org.eclipse.che.infrastructure.docker.client.dto.AuthConfigs;

/**
 * Arguments holder for {@link DockerConnector#buildImage(BuildImageParams, ProgressMonitor)}.
 *
 * @author Mykola Morhun
 * @author Alexander Garagatyi
 */
public class BuildImageParams {
  private String repository;
  private String tag;
  private AuthConfigs authConfigs;
  private Boolean doForcePull;
  private Long memoryLimit;
  private Long memorySwapLimit;
  private List<File> files;
  private String dockerfile;
  private String remote;
  private Boolean quiet;
  private Boolean noCache;
  private Boolean removeIntermediateContainer;
  private Boolean forceRemoveIntermediateContainers;
  private Map<String, String> buildArgs;
  private String cpusetCpus;
  private Long cpuPeriod;
  private Long cpuQuota;

  /**
   * Creates arguments holder with required parameters.
   *
   * @param files info about this parameter see {@link #withFiles(File...)}
   * @return arguments holder with required parameters
   * @throws NullPointerException if {@code files} is null
   * @throws IllegalArgumentException if {@code files} is empty array
   */
  public static BuildImageParams create(@NotNull File... files) {
    return new BuildImageParams().withFiles(files);
  }

  /**
   * Creates arguments holder with required parameters.
   *
   * @param remote info about this parameter see {@link #withRemote(String)}
   * @return arguments holder with required parameters
   * @throws NullPointerException if {@code remote} is null
   * @throws IllegalArgumentException if {@code remote} is empty
   */
  public static BuildImageParams create(@NotNull String remote) {
    return new BuildImageParams().withRemote(remote);
  }

  private BuildImageParams() {}

  /**
   * Adds repository to this parameters.
   *
   * @param repository full repository name to be applied to newly created image
   * @return this params instance
   */
  public BuildImageParams withRepository(String repository) {
    this.repository = repository;
    return this;
  }

  /**
   * Adds tag to this parameters.
   *
   * @param tag tag of the image
   * @return this params instance
   */
  public BuildImageParams withTag(String tag) {
    this.tag = tag;
    return this;
  }

  /**
   * Adds auth configuration to this parameters.
   *
   * @param authConfigs authentication configuration for registries
   * @return this params instance
   */
  public BuildImageParams withAuthConfigs(AuthConfigs authConfigs) {
    this.authConfigs = authConfigs;
    return this;
  }

  /**
   * Adds force flag to this parameters.
   *
   * @param doForcePull if {@code true} attempts to pull the image even if an older image exists
   *     locally
   * @return this params instance
   */
  public BuildImageParams withDoForcePull(boolean doForcePull) {
    this.doForcePull = doForcePull;
    return this;
  }

  /**
   * Adds RAM memory limit to this parameters.
   *
   * @param memoryLimit RAM memory limit for build in bytes
   * @return this params instance
   */
  public BuildImageParams withMemoryLimit(long memoryLimit) {
    this.memoryLimit = memoryLimit;
    return this;
  }

  /**
   * Adds swap memory limit to this parameters.
   *
   * @param memorySwapLimit total memory in bytes (memory + swap), -1 to enable unlimited swap
   * @return this params instance
   */
  public BuildImageParams withMemorySwapLimit(long memorySwapLimit) {
    this.memorySwapLimit = memorySwapLimit;
    return this;
  }

  /**
   * Sets list of files for creation docker image. One of them must be Dockerfile.
   *
   * @param files files that are needed for creation docker images (e.g. file of directories used in
   *     ADD instruction in Dockerfile). One of them must be Dockerfile.
   * @return this params instance
   * @throws NullPointerException if {@code files} is null
   * @throws IllegalArgumentException if {@code files} is empty array
   * @throws IllegalStateException if other parameter incompatible with files is set
   */
  public BuildImageParams withFiles(@NotNull File... files) {
    if (remote != null) {
      throw new IllegalStateException(
          "Remote parameter is already set. Remote and files parameters are mutually exclusive.");
    }
    requireNonNull(files);
    requireNonEmptyArray(files);
    this.files = new ArrayList<>(files.length + 1);
    return addFiles(files);
  }

  /**
   * Adds files to the file list. see {@link #withFiles(File...)}
   *
   * @param files files to add to image
   * @return this params instance
   * @throws NullPointerException if {@code files} is null
   */
  public BuildImageParams addFiles(@NotNull File... files) {
    if (remote != null) {
      throw new IllegalStateException(
          "Remote parameter is already set. Remote and files parameters are mutually exclusive.");
    }
    requireNonNull(files);
    for (File file : files) {
      requireNonNull(file);
      this.files.add(file);
    }
    return this;
  }

  /**
   * Sets GIT repo or HTTP(S) location of dockerfile or build sources
   *
   * @param remote URI of build context
   * @return this params instance
   * @throws NullPointerException if {@code remote} is null
   * @throws IllegalArgumentException if {@code remote} is empty
   * @throws IllegalStateException if other parameter incompatible with remote is set
   */
  public BuildImageParams withRemote(@NotNull String remote) {
    requireNonNullNorEmpty(remote);
    if (files != null) {
      throw new IllegalStateException(
          "Files parameter is already set. Remote and files parameters are mutually exclusive.");
    }

    this.remote = remote;
    return this;
  }

  /**
   * Suppress verbose build output.
   *
   * @param quiet quiet flag
   * @return this params instance
   */
  public BuildImageParams withQuiet(boolean quiet) {
    this.quiet = quiet;
    return this;
  }

  /**
   * Do not use the cache when building the image.
   *
   * @param noCache no cache flag
   * @return this params instance
   */
  public BuildImageParams withNoCache(boolean noCache) {
    this.noCache = noCache;
    return this;
  }

  /**
   * Remove intermediate containers after a successful build.
   *
   * @param removeIntermediateContainer remove intermediate container flag
   * @return this params instance
   */
  public BuildImageParams withRemoveIntermediateContainers(boolean removeIntermediateContainer) {
    this.removeIntermediateContainer = removeIntermediateContainer;
    return this;
  }

  /**
   * Always remove intermediate containers (includes removeIntermediateContainer).
   *
   * @param forceRemoveIntermediateContainers remove intermediate containers with force flag
   * @return this params instance
   */
  public BuildImageParams withForceRemoveIntermediateContainers(
      boolean forceRemoveIntermediateContainers) {
    this.forceRemoveIntermediateContainers = forceRemoveIntermediateContainers;
    return this;
  }

  /**
   * Map of string pairs for build-time variables. Users pass these values at build-time. Docker
   * uses the buildargs as the environment context for command(s) run via the Dockerfile’s RUN
   * instruction or for variable expansion in other Dockerfile instructions.
   *
   * @param buildArgs map of build arguments
   * @return this params instance
   */
  public BuildImageParams withBuildArgs(Map<String, String> buildArgs) {
    this.buildArgs = buildArgs;
    return this;
  }

  /**
   * Adds build variable to build args. See {@link #withBuildArgs(Map)}
   *
   * @param key variable name
   * @param value variable value
   * @return this params instance
   */
  public BuildImageParams addBuildArg(String key, String value) {
    if (buildArgs == null) {
      buildArgs = new HashMap<>();
    }
    buildArgs.put(key, value);
    return this;
  }

  /**
   * Sets path to alternate dockerfile in build context
   *
   * @param dockerfilePath path of alternate dockerfile
   * @return this params instance
   */
  public BuildImageParams withDockerfile(String dockerfilePath) {
    this.dockerfile = dockerfilePath;
    return this;
  }

  /**
   * Sets CPU cores config that is allowed to use for image build.
   *
   * @param cpusetCpus CPU cores config
   * @return this params instance
   */
  public BuildImageParams withCpusetCpus(String cpusetCpus) {
    this.cpusetCpus = cpusetCpus;
    return this;
  }

  /**
   * Sets length of a CPU period for CPU consumption limiting in microseconds.
   *
   * @param cpuPeriod length of a CPU period
   * @return this params instance
   */
  public BuildImageParams withCpuPeriod(Long cpuPeriod) {
    this.cpuPeriod = cpuPeriod;
    return this;
  }

  /**
   * Sets amount of CPU time that build container can get in a CPU period.
   *
   * @param cpuQuota amount of CPU time in microseconds
   * @return this params instance
   */
  public BuildImageParams withCpuQuota(Long cpuQuota) {
    this.cpuQuota = cpuQuota;
    return this;
  }

  public String getRepository() {
    return repository;
  }

  public String getTag() {
    return tag;
  }

  public AuthConfigs getAuthConfigs() {
    return authConfigs;
  }

  public Boolean isDoForcePull() {
    return doForcePull;
  }

  public Long getMemoryLimit() {
    return memoryLimit;
  }

  public Long getMemorySwapLimit() {
    return memorySwapLimit;
  }

  public List<File> getFiles() {
    return files;
  }

  public String getDockerfile() {
    return dockerfile;
  }

  public String getRemote() {
    return remote;
  }

  public Boolean isQuiet() {
    return quiet;
  }

  public Boolean isNoCache() {
    return noCache;
  }

  public Boolean isRemoveIntermediateContainer() {
    return removeIntermediateContainer;
  }

  public Boolean isForceRemoveIntermediateContainers() {
    return forceRemoveIntermediateContainers;
  }

  public Map<String, String> getBuildArgs() {
    return buildArgs;
  }

  public String getCpusetCpus() {
    return cpusetCpus;
  }

  public Long getCpuPeriod() {
    return cpuPeriod;
  }

  public Long getCpuQuota() {
    return cpuQuota;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BuildImageParams that = (BuildImageParams) o;
    return Objects.equals(repository, that.repository)
        && Objects.equals(tag, that.tag)
        && Objects.equals(authConfigs, that.authConfigs)
        && Objects.equals(doForcePull, that.doForcePull)
        && Objects.equals(memoryLimit, that.memoryLimit)
        && Objects.equals(memorySwapLimit, that.memorySwapLimit)
        && Objects.equals(files, that.files)
        && Objects.equals(dockerfile, that.dockerfile)
        && Objects.equals(remote, that.remote)
        && Objects.equals(quiet, that.quiet)
        && Objects.equals(noCache, that.noCache)
        && Objects.equals(removeIntermediateContainer, that.removeIntermediateContainer)
        && Objects.equals(forceRemoveIntermediateContainers, that.forceRemoveIntermediateContainers)
        && Objects.equals(cpusetCpus, that.cpusetCpus)
        && Objects.equals(cpuPeriod, that.cpuPeriod)
        && Objects.equals(cpuQuota, that.cpuQuota)
        && Objects.equals(buildArgs, that.buildArgs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        repository,
        tag,
        authConfigs,
        doForcePull,
        memoryLimit,
        memorySwapLimit,
        files,
        dockerfile,
        remote,
        quiet,
        noCache,
        removeIntermediateContainer,
        forceRemoveIntermediateContainers,
        cpusetCpus,
        cpuPeriod,
        cpuQuota,
        buildArgs);
  }

  @Override
  public String toString() {
    return "BuildImageParams{"
        + "repository='"
        + repository
        + '\''
        + ", tag='"
        + tag
        + '\''
        + ", authConfigs='"
        + authConfigs
        + '\''
        + ", doForcePull='"
        + doForcePull
        + '\''
        + ", memoryLimit='"
        + memoryLimit
        + '\''
        + ", memorySwapLimit='"
        + memorySwapLimit
        + '\''
        + ", files="
        + files
        + '\''
        + ", dockerfile='"
        + dockerfile
        + '\''
        + ", remote='"
        + remote
        + '\''
        + ", quiet='"
        + quiet
        + '\''
        + ", noCache='"
        + noCache
        + '\''
        + ", removeIntermediateContainer='"
        + removeIntermediateContainer
        + '\''
        + ", forceRemoveIntermediateContainers='"
        + forceRemoveIntermediateContainers
        + '\''
        + ", cpusetCpus='"
        + cpusetCpus
        + '\''
        + ", cpuPeriod='"
        + cpuPeriod
        + '\''
        + ", cpuQuota='"
        + cpuQuota
        + '\''
        + ", buildArgs='"
        + buildArgs
        + '\''
        + '}';
  }
}
