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
package org.eclipse.che.api.devfile.shared.event;

import com.google.common.annotations.Beta;
import org.eclipse.che.api.core.notification.EventOrigin;

/** Informs about persisted devfile removal. */
@EventOrigin("devfile")
@Beta
public class DevfileDeletedEvent {
  private final String id;

  public DevfileDeletedEvent(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }
}
