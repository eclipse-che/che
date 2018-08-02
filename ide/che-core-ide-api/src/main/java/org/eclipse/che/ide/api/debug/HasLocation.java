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
package org.eclipse.che.ide.api.debug;

import org.eclipse.che.api.debug.shared.model.Location;

/**
 * Marker to indicate that given implementation can deal with debug {@link Location}.
 *
 * @author Anatolii Bazko
 */
public interface HasLocation {
  Location toLocation(int lineNumber);
}
