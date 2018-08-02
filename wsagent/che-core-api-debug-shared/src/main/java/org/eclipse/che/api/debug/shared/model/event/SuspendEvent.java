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
package org.eclipse.che.api.debug.shared.model.event;

import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.SuspendPolicy;

/**
 * Event is generated when debugger suspended at some location.
 *
 * @author Anatoliy Bazko
 */
public interface SuspendEvent extends DebuggerEvent {
  Location getLocation();

  SuspendPolicy getSuspendPolicy();
}
