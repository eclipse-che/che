/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
