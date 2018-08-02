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

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.infrastructure.docker.auth.dto.AuthConfigs;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Mykola Morhun
 * @author Alexander Garagatyi
 */
public class BuildImageParamsTest {

  private static final String REPOSITORY = "repository";
  private static final String TAG = "tag";
  private static final AuthConfigs AUTH_CONFIGS = mock(AuthConfigs.class);
  private static final Boolean DO_FORCE_PULL = true;
  private static final Long MEMORY_LIMIT = 12345L;
  private static final Long MEMORY_SWAP_LIMIT = 67890L;
  private static final File FILE = new File(".");
  private static final String DOCKERFILE = "/tmp/Dockerfile";
  private static final String REMOTE = "https://github.com/someuser/remote.git";
  private static final Boolean QUIET = false;
  private static final Boolean NO_CACHE = false;
  private static final Boolean REMOVE_INTERMEDIATE_CONTAINER = true;
  private static final Boolean FORCE_REMOVE_INTERMEDIATE_CONTAINERS = true;
  private static final Map<String, String> BUILD_ARGS = new HashMap<>();
  private static final String CPUSET_CPUS = "0-3";
  private static final Long CPU_PERIOD = 50000L;
  private static final Long CPU_QUOTA = 150000L;
  private static final List<File> FILES;

  static {
    FILES = new ArrayList<>();
    FILES.add(FILE);
  }

  private BuildImageParams buildImageParams;

  @BeforeMethod
  private void prepare() {
    buildImageParams = BuildImageParams.create(FILE);
  }

  @Test
  public void shouldCreateParamsObjectWithRequiredParametersFromFiles() {
    buildImageParams = BuildImageParams.create(FILE);

    assertEquals(buildImageParams.getFiles(), FILES);

    assertNull(buildImageParams.getRepository());
    assertNull(buildImageParams.getTag());
    assertNull(buildImageParams.getAuthConfigs());
    assertNull(buildImageParams.isDoForcePull());
    assertNull(buildImageParams.getMemoryLimit());
    assertNull(buildImageParams.getMemorySwapLimit());
    assertNull(buildImageParams.getDockerfile());
    assertNull(buildImageParams.getRemote());
    assertNull(buildImageParams.isQuiet());
    assertNull(buildImageParams.isNoCache());
    assertNull(buildImageParams.isRemoveIntermediateContainer());
    assertNull(buildImageParams.isForceRemoveIntermediateContainers());
    assertNull(buildImageParams.getBuildArgs());
    assertNull(buildImageParams.getCpusetCpus());
    assertNull(buildImageParams.getCpuPeriod());
    assertNull(buildImageParams.getCpuQuota());
  }

  @Test
  public void shouldCreateParamsObjectWithRequiredParametersFromRemote() {
    buildImageParams = BuildImageParams.create(REMOTE);

    assertEquals(buildImageParams.getRemote(), REMOTE);

    assertNull(buildImageParams.getRepository());
    assertNull(buildImageParams.getTag());
    assertNull(buildImageParams.getAuthConfigs());
    assertNull(buildImageParams.isDoForcePull());
    assertNull(buildImageParams.getMemoryLimit());
    assertNull(buildImageParams.getMemorySwapLimit());
    assertNull(buildImageParams.getFiles());
    assertNull(buildImageParams.getDockerfile());
    assertNull(buildImageParams.isQuiet());
    assertNull(buildImageParams.isNoCache());
    assertNull(buildImageParams.isRemoveIntermediateContainer());
    assertNull(buildImageParams.isForceRemoveIntermediateContainers());
    assertNull(buildImageParams.getBuildArgs());
    assertNull(buildImageParams.getCpusetCpus());
    assertNull(buildImageParams.getCpuPeriod());
    assertNull(buildImageParams.getCpuQuota());
  }

  @Test
  public void shouldCreateParamsObjectWithAllPossibleParametersFromFiles() {
    buildImageParams =
        BuildImageParams.create(FILE)
            .withRepository(REPOSITORY)
            .withTag(TAG)
            .withAuthConfigs(AUTH_CONFIGS)
            .withDoForcePull(DO_FORCE_PULL)
            .withMemoryLimit(MEMORY_LIMIT)
            .withMemorySwapLimit(MEMORY_SWAP_LIMIT)
            .withDockerfile(DOCKERFILE)
            .withQuiet(QUIET)
            .withNoCache(NO_CACHE)
            .withRemoveIntermediateContainers(REMOVE_INTERMEDIATE_CONTAINER)
            .withForceRemoveIntermediateContainers(FORCE_REMOVE_INTERMEDIATE_CONTAINERS)
            .withCpusetCpus(CPUSET_CPUS)
            .withCpuPeriod(CPU_PERIOD)
            .withCpuQuota(CPU_QUOTA)
            .withBuildArgs(BUILD_ARGS);

    assertNull(buildImageParams.getRemote());

    assertEquals(buildImageParams.getFiles(), FILES);
    assertEquals(buildImageParams.getTag(), TAG);
    assertEquals(buildImageParams.getRepository(), REPOSITORY);
    assertEquals(buildImageParams.getAuthConfigs(), AUTH_CONFIGS);
    assertEquals(buildImageParams.isDoForcePull(), DO_FORCE_PULL);
    assertEquals(buildImageParams.getMemoryLimit(), MEMORY_LIMIT);
    assertEquals(buildImageParams.getMemorySwapLimit(), MEMORY_SWAP_LIMIT);
    assertEquals(buildImageParams.getDockerfile(), DOCKERFILE);
    assertEquals(buildImageParams.isQuiet(), QUIET);
    assertEquals(buildImageParams.isNoCache(), NO_CACHE);
    assertEquals(buildImageParams.isRemoveIntermediateContainer(), REMOVE_INTERMEDIATE_CONTAINER);
    assertEquals(
        buildImageParams.isForceRemoveIntermediateContainers(),
        FORCE_REMOVE_INTERMEDIATE_CONTAINERS);
    assertEquals(buildImageParams.getBuildArgs(), BUILD_ARGS);
    assertEquals(buildImageParams.getCpusetCpus(), CPUSET_CPUS);
    assertEquals(buildImageParams.getCpuPeriod(), CPU_PERIOD);
    assertEquals(buildImageParams.getCpuQuota(), CPU_QUOTA);
  }

  @Test
  public void shouldCreateParamsObjectWithAllPossibleParametersFromRemote() {
    buildImageParams =
        BuildImageParams.create(REMOTE)
            .withRepository(REPOSITORY)
            .withTag(TAG)
            .withAuthConfigs(AUTH_CONFIGS)
            .withDoForcePull(DO_FORCE_PULL)
            .withMemoryLimit(MEMORY_LIMIT)
            .withMemorySwapLimit(MEMORY_SWAP_LIMIT)
            .withDockerfile(DOCKERFILE)
            .withQuiet(QUIET)
            .withNoCache(NO_CACHE)
            .withRemoveIntermediateContainers(REMOVE_INTERMEDIATE_CONTAINER)
            .withForceRemoveIntermediateContainers(FORCE_REMOVE_INTERMEDIATE_CONTAINERS)
            .withCpusetCpus(CPUSET_CPUS)
            .withCpuPeriod(CPU_PERIOD)
            .withCpuQuota(CPU_QUOTA)
            .withBuildArgs(BUILD_ARGS);

    assertNull(buildImageParams.getFiles());

    assertEquals(buildImageParams.getRemote(), REMOTE);
    assertEquals(buildImageParams.getTag(), TAG);
    assertEquals(buildImageParams.getRepository(), REPOSITORY);
    assertEquals(buildImageParams.getAuthConfigs(), AUTH_CONFIGS);
    assertEquals(buildImageParams.isDoForcePull(), DO_FORCE_PULL);
    assertEquals(buildImageParams.getMemoryLimit(), MEMORY_LIMIT);
    assertEquals(buildImageParams.getMemorySwapLimit(), MEMORY_SWAP_LIMIT);
    assertEquals(buildImageParams.getDockerfile(), DOCKERFILE);
    assertEquals(buildImageParams.isQuiet(), QUIET);
    assertEquals(buildImageParams.isNoCache(), NO_CACHE);
    assertEquals(buildImageParams.isRemoveIntermediateContainer(), REMOVE_INTERMEDIATE_CONTAINER);
    assertEquals(
        buildImageParams.isForceRemoveIntermediateContainers(),
        FORCE_REMOVE_INTERMEDIATE_CONTAINERS);
    assertEquals(buildImageParams.getBuildArgs(), BUILD_ARGS);
    assertEquals(buildImageParams.getCpusetCpus(), CPUSET_CPUS);
    assertEquals(buildImageParams.getCpuPeriod(), CPU_PERIOD);
    assertEquals(buildImageParams.getCpuQuota(), CPU_QUOTA);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfFilesRequiredParameterIsNull() {
    File file = null;
    buildImageParams = BuildImageParams.create(file);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfFilesRequiredParameterResetWithNull() {
    File file = null;
    buildImageParams.withFiles(file);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void shouldThrowIllegalArgumentExceptionIfSetEmptyFilesArray() {
    File[] files = new File[0];
    buildImageParams.withFiles(files);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfSetFilesArrayWithNullElement() {
    buildImageParams.withFiles(FILE, null, FILE);
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void shouldThrowIllegalStateExceptionIfSetRemoteAfterSetFiles() {
    buildImageParams = BuildImageParams.create(FILE).withRemote(REMOTE);
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void shouldThrowIllegalStateExceptionIfSetFileAfterSetRemote() {
    buildImageParams = BuildImageParams.create(REMOTE).withFiles(FILE);
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void shouldThrowIllegalStateExceptionIfAddFileAfterSetRemote() {
    buildImageParams = BuildImageParams.create(REMOTE).addFiles(FILE);
  }

  @Test
  public void repositoryParameterShouldEqualsNullIfItNotSet() {
    assertNull(buildImageParams.getRepository());
  }

  @Test
  public void tagParameterShouldEqualsNullIfItNotSet() {
    assertNull(buildImageParams.getTag());
  }

  @Test
  public void authConfigParameterShouldEqualsNullIfItNotSet() {
    assertNull(buildImageParams.getAuthConfigs());
  }

  @Test
  public void doForcePullParameterShouldEqualsNullIfItNotSet() {
    assertNull(buildImageParams.isDoForcePull());
  }

  @Test
  public void memoryLimitParameterShouldEqualsNullIfItNotSet() {
    assertNull(buildImageParams.getMemoryLimit());
  }

  @Test
  public void memorySwapLimitParameterShouldEqualsNullIfItNotSet() {
    assertNull(buildImageParams.getMemorySwapLimit());
  }

  @Test
  public void dockerfileParameterShouldEqualsNullIfItNotSet() {
    buildImageParams.withRepository(REPOSITORY);

    assertNull(buildImageParams.getDockerfile());
  }

  @Test
  public void quietParameterShouldEqualsNullIfItNotSet() {
    assertNull(buildImageParams.isQuiet());
  }

  @Test
  public void noCacheParameterShouldEqualsNullIfItNotSet() {
    assertNull(buildImageParams.isNoCache());
  }

  @Test
  public void removeIntermediateContainerParameterShouldEqualsNullIfItNotSet() {
    assertNull(buildImageParams.isRemoveIntermediateContainer());
  }

  @Test
  public void forceRemoveIntermediateContainersParameterShouldEqualsNullIfItNotSet() {
    assertNull(buildImageParams.isForceRemoveIntermediateContainers());
  }

  @Test
  public void buildArgsParameterShouldEqualsNullIfItNotSet() {
    assertNull(buildImageParams.getBuildArgs());
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfAddNullAsFile() {
    File file = null;
    buildImageParams.addFiles(file);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfAddFilesArrayWithNullElement() {
    buildImageParams.addFiles(FILE, null, FILE);
  }

  @Test
  public void shouldAddFileToFilesList() {
    File file = new File("../");
    List<File> files = new ArrayList<>();
    files.add(FILE);
    files.add(file);

    buildImageParams.addFiles(file);

    assertEquals(buildImageParams.getFiles(), files);
  }

  @Test
  public void shouldAddBuildArgToBuildArgs() {
    String key = "ba_key";
    String value = "ba_value";

    buildImageParams.addBuildArg(key, value);

    assertNotNull(buildImageParams.getBuildArgs());
    assertEquals(buildImageParams.getBuildArgs().get(key), value);
  }

  @Test
  public void cpuQuotaParameterShouldBeNullIfItNotSet() {
    assertNull(buildImageParams.getCpuQuota());
  }

  @Test
  public void cpuPeriodParameterShouldBeNullIfItNotSet() {
    assertNull(buildImageParams.getCpuPeriod());
  }

  @Test
  public void cpusetCpusParameterShouldBeNullIfItNotSet() {
    assertNull(buildImageParams.getCpusetCpus());
  }
}
