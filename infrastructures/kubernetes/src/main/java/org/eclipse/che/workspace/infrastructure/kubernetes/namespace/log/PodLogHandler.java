/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace.log;

/**
 * Handle log message of the pod's container. Implementors must be also aware of pods that are
 * interest of this handler.
 */
public interface PodLogHandler {

  /**
   * Receives single log message and do something with it. It receives also containerName so we can
   * better format the message for the end-user.
   *
   * @param message single log message
   * @param containerName source container of this log message
   */
  void handle(String message, String containerName);
}
