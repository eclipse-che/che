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
package org.eclipse.che.api.workspace.shared.dto;

/**
 * Statuses of a plugin broker that is used in the process of workspace start when sidecar-based
 * tooling is used.
 *
 * <p>This API is in <b>Beta</b> and is subject to changes or removal.
 *
 * @author Oleksandr Garagatyi
 */
public enum BrokerStatus {

  /** Means that broker is started but neither error nor success was achieved yet. */
  STARTED,

  /** Means that broker successfully finished execution. */
  DONE,

  /** Means that broker execution failed. */
  FAILED
}
