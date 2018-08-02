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

import org.eclipse.che.infrastructure.docker.client.json.ProgressStatus;

/**
 * Receives updated progress statuses to be able to show user beatified progress info.
 *
 * @author andrew00x
 * @author Alexander Garagatyi
 */
public interface ProgressMonitor {
  void updateProgress(ProgressStatus currentProgressStatus);

  ProgressMonitor DEV_NULL =
      new ProgressMonitor() {
        @Override
        public void updateProgress(ProgressStatus currentProgressStatus) {}
      };
}
