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
package org.eclipse.che.infrastructure.docker.client;

import com.google.inject.ImplementedBy;

/**
 * Detects container OOM and put message about it to log processor of container.
 *
 * @author Alexander Garagatyi
 */
@ImplementedBy(DockerOOMDetector.NoOpDockerOOMDetector.class)
public interface DockerOOMDetector {

  /**
   * Stops detection of OOM for specified container.
   *
   * @param container container id to stop OOM detection for
   */
  void stopDetection(String container);

  /**
   * Starts detection of OOM for specified container. Does nothing if container is under OOM
   * detection already. Also puts message about OOM to processor of container logs.
   *
   * @param container container id to stop OOM detection for
   * @param startContainerLogProcessor processor of container logs to put message about OOM
   *     detection
   */
  void startDetection(String container, MessageProcessor<LogMessage> startContainerLogProcessor);

  DockerOOMDetector NOOP_DETECTOR = new NoOpDockerOOMDetector();

  class NoOpDockerOOMDetector implements DockerOOMDetector {
    @Override
    public void stopDetection(String container) {}

    @Override
    public void startDetection(
        String container, MessageProcessor<LogMessage> startContainerLogProcessor) {}
  }
}
