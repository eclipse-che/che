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
package org.eclipse.che.workspace.infrastructure.docker;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.workspace.infrastructure.docker.DockerMachine.LATEST_TAG;
import static org.slf4j.LoggerFactory.getLogger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.assistedinject.Assisted;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.util.FileCleaner;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.eclipse.che.infrastructure.docker.client.DockerConnector;
import org.eclipse.che.infrastructure.docker.client.DockerFileException;
import org.eclipse.che.infrastructure.docker.client.ProgressMonitor;
import org.eclipse.che.infrastructure.docker.client.UserSpecificDockerRegistryCredentialsProvider;
import org.eclipse.che.infrastructure.docker.client.exception.ImageNotFoundException;
import org.eclipse.che.infrastructure.docker.client.json.Filters;
import org.eclipse.che.infrastructure.docker.client.params.BuildImageParams;
import org.eclipse.che.infrastructure.docker.client.params.ListImagesParams;
import org.eclipse.che.infrastructure.docker.client.params.PullParams;
import org.eclipse.che.infrastructure.docker.client.params.TagParams;
import org.eclipse.che.infrastructure.docker.client.parser.DockerImageIdentifier;
import org.eclipse.che.infrastructure.docker.client.parser.DockerImageIdentifierParser;
import org.eclipse.che.workspace.infrastructure.docker.exception.SourceNotFoundException;
import org.eclipse.che.workspace.infrastructure.docker.logs.MachineLoggersFactory;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.slf4j.Logger;

/**
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
public class ParallelDockerImagesResolver {

  private static final Logger LOG = getLogger(ParallelDockerImagesResolver.class);

  private final RuntimeIdentity identity;
  private final MachineLoggersFactory machineLoggersFactory;
  private final boolean doForcePullImage;
  private final UserSpecificDockerRegistryCredentialsProvider dockerCredentials;
  private final DockerConnector dockerConnector;

  @Inject
  public ParallelDockerImagesResolver(
      @Assisted RuntimeIdentity identity,
      @Named("che.docker.always_pull_image") boolean doForcePullImage,
      UserSpecificDockerRegistryCredentialsProvider dockerCredentials,
      DockerConnector dockerConnector,
      MachineLoggersFactory machineLoggersFactory) {
    this.identity = identity;
    this.doForcePullImage = doForcePullImage;
    this.dockerCredentials = dockerCredentials;
    this.dockerConnector = dockerConnector;
    this.machineLoggersFactory = machineLoggersFactory;
  }

  public Map<String, String> resolveImages(Map<String, DockerContainerConfig> containers)
      throws InterruptedException, InfrastructureException {
    ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
    ThreadFactory factory =
        builder
            .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
            .setNameFormat(getClass().getSimpleName())
            .build();
    ExecutorService executor = Executors.newFixedThreadPool(6, factory); // why 6 ?
    Map<String, String> imagesMap = new ConcurrentHashMap<>(containers.size());

    CompletableFuture<Void> firstFailed = new CompletableFuture<>();
    List<CompletableFuture<String>> taskFutures =
        containers
            .entrySet()
            .stream()
            .map(
                e ->
                    CompletableFuture.supplyAsync(
                        () -> {
                          String imageName = null;
                          try {
                            imageName =
                                prepareImage(
                                    e.getKey(),
                                    e.getValue(),
                                    machineLoggersFactory.newProgressMonitor(e.getKey(), identity));
                          } catch (InternalInfrastructureException | SourceNotFoundException ex) {
                            firstFailed.completeExceptionally(ex);
                          }
                          return imagesMap.put(e.getKey(), imageName);
                        },
                        executor))
            .collect(toList());

    CompletableFuture all =
        CompletableFuture.allOf(taskFutures.toArray(new CompletableFuture[taskFutures.size()]));
    try {
      CompletableFuture.anyOf(all, firstFailed).get();
    } catch (ExecutionException e) {
      try {
        throw e.getCause();
      } catch (InfrastructureException rethrow) {
        throw rethrow;
      } catch (Throwable thr) {
        throw new InternalInfrastructureException("Unable to build image", thr);
      }
    }
    return imagesMap;
  }

  private String prepareImage(
      String machineName, DockerContainerConfig container, ProgressMonitor progressMonitor)
      throws SourceNotFoundException, InternalInfrastructureException {

    String imageName = "eclipse-che/" + container.getContainerName();
    if ((container.getBuild() == null
        || (container.getBuild().getContext() == null
        && container.getBuild().getDockerfileContent() == null))
        && container.getImage() == null) {

      throw new InternalInfrastructureException(
          format("Che container '%s' doesn't have neither build nor image fields", machineName));
    }

    if (container.getBuild() != null
        && (container.getBuild().getContext() != null
        || container.getBuild().getDockerfileContent() != null)) {
      buildImage(container, imageName, doForcePullImage, progressMonitor);
    } else {
      pullImage(container, imageName, progressMonitor);
    }

    return imageName;
  }

  /**
   * Builds Docker image for container creation.
   *
   * @param containerConfig configuration of container
   * @param machineImageName name of image that should be applied to built image
   * @param doForcePullOnBuild whether re-pulling of base image should be performed when it exists
   * locally
   * @param progressMonitor consumer of build logs
   * @throws InternalInfrastructureException when any error occurs
   */
  protected void buildImage(
      DockerContainerConfig containerConfig,
      String machineImageName,
      boolean doForcePullOnBuild,
      ProgressMonitor progressMonitor)
      throws InternalInfrastructureException {

    File workDir = null;
    try {
      BuildImageParams buildImageParams;
      if (containerConfig.getBuild() != null
          && containerConfig.getBuild().getDockerfileContent() != null) {

        workDir = Files.createTempDirectory(null).toFile();
        final File dockerfileFile = new File(workDir, "Dockerfile");
        try (FileWriter output = new FileWriter(dockerfileFile)) {
          output.append(containerConfig.getBuild().getDockerfileContent());
        }

        buildImageParams = BuildImageParams.create(dockerfileFile);
      } else {
        buildImageParams =
            BuildImageParams.create(containerConfig.getBuild().getContext())
                .withDockerfile(containerConfig.getBuild().getDockerfilePath());
      }
      buildImageParams
          .withForceRemoveIntermediateContainers(true)
          .withRepository(machineImageName)
          .withAuthConfigs(dockerCredentials.getCredentials())
          .withDoForcePull(doForcePullOnBuild)
          .withMemoryLimit(containerConfig.getMemLimit())
          .withMemorySwapLimit(-1)
          .withBuildArgs(containerConfig.getBuild().getArgs());

      dockerConnector.buildImage(buildImageParams, progressMonitor);
    } catch (IOException e) {
      throw new InternalInfrastructureException(e.getLocalizedMessage(), e);
    } finally {
      if (workDir != null) {
        FileCleaner.addFile(workDir);
      }
    }
  }

  /**
   * Pulls docker image for container creation.
   *
   * @param container container that provides description of image that should be pulled
   * @param machineImageName name of the image that should be assigned on pull
   * @param progressMonitor consumer of output
   * @throws SourceNotFoundException if image for pulling not found in registry
   * @throws InternalInfrastructureException if any other error occurs
   */
  protected void pullImage(
      DockerContainerConfig container, String machineImageName, ProgressMonitor progressMonitor)
      throws InternalInfrastructureException, SourceNotFoundException {
    final DockerImageIdentifier dockerImageIdentifier;
    try {
      dockerImageIdentifier = DockerImageIdentifierParser.parse(container.getImage());
    } catch (DockerFileException e) {
      throw new InternalInfrastructureException(
          "Try to build a docker machine source with an invalid location/content. It is not in the expected format",
          e);
    }
    if (dockerImageIdentifier.getRepository() == null) {
      throw new InternalInfrastructureException(
          format(
              "Machine creation failed. Machine source is invalid. No repository is defined. Found '%s'.",
              dockerImageIdentifier.getRepository()));
    }
    try {
      boolean isImageExistLocally =
          isDockerImageExistLocally(dockerImageIdentifier.getRepository());
      if (doForcePullImage || !isImageExistLocally) {
        PullParams pullParams =
            PullParams.create(dockerImageIdentifier.getRepository())
                .withTag(MoreObjects.firstNonNull(dockerImageIdentifier.getTag(), LATEST_TAG))
                .withRegistry(dockerImageIdentifier.getRegistry())
                .withAuthConfigs(dockerCredentials.getCredentials());
        dockerConnector.pull(pullParams, progressMonitor);
      }

      String fullNameOfPulledImage = container.getImage();
      try {
        // tag image with generated name to allow sysadmin recognize it
        dockerConnector.tag(TagParams.create(fullNameOfPulledImage, machineImageName));
      } catch (ImageNotFoundException nfEx) {
        throw new SourceNotFoundException(nfEx.getLocalizedMessage(), nfEx);
      }
    } catch (IOException e) {
      throw new InternalInfrastructureException(
          "Can't create machine from image. Cause: " + e.getLocalizedMessage(), e);
    }
  }

  @VisibleForTesting
  boolean isDockerImageExistLocally(String imageName) {
    try {
      return !dockerConnector
          .listImages(
              ListImagesParams.create()
                  .withFilters(new Filters().withFilter("reference", imageName)))
          .isEmpty();
    } catch (IOException e) {
      LOG.warn("Failed to check image {} availability. Cause: {}", imageName, e.getMessage(), e);
      return false; // consider that image doesn't exist locally
    }
  }
}
