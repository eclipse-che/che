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
package org.eclipse.che.ide.api.resources.modification;

import com.google.common.annotations.Beta;
import org.eclipse.che.ide.api.resources.marker.Marker;

/**
 * Marker that indicates resource which is cut by {@link CutProvider}.
 *
 * @author Vlad Zhukovskiy
 * @since 4.4.0
 */
@Beta
public class CutResourceMarker implements Marker {

  public static final String ID = "cutResourceMarker";

  @Override
  public String getType() {
    return ID;
  }
}
