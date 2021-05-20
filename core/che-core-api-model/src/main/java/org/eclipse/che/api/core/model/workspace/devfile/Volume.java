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
package org.eclipse.che.api.core.model.workspace.devfile;

public interface Volume {
  /**
   * Returns the volume name. If several components mount the same volume then they will reuse the
   * volume and will be able to access to the same files. It is mandatory.
   */
  String getName();

  /** Returns the path where volume should be mount to container. It is mandatory. */
  String getContainerPath();
}
