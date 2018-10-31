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
package org.eclipse.che.api.workspace.server.event;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;

/**
 * Should be propagated before Infrastructure stops a runtime because of any fatal error.
 *
 * @author Sergii Leshchenko
 */
public class RuntimeAbnormalStoppingEvent {

  private RuntimeIdentity runtimeId;
  private String reason;

  public RuntimeAbnormalStoppingEvent(RuntimeIdentity runtimeId, String reason) {
    this.runtimeId = runtimeId;
    this.reason = reason;
  }

  public RuntimeIdentity getIdentity() {
    return runtimeId;
  }

  public String getReason() {
    return reason;
  }
}
