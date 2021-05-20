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
import org.eclipse.che.api.core.model.workspace.devfile.UserDevfile;
import org.eclipse.che.api.core.notification.EventOrigin;

/** Informs about persisted devfile update. */
@EventOrigin("devfile")
@Beta
public class DevfileUpdatedEvent {
  private final UserDevfile userDevfile;

  public DevfileUpdatedEvent(UserDevfile userDevfile) {
    this.userDevfile = userDevfile;
  }

  public UserDevfile getUserDevfile() {
    return userDevfile;
  }
}
