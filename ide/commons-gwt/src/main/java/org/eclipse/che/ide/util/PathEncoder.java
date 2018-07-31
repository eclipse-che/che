/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.util;

import static com.google.gwt.http.client.URL.encodePathSegment;
import static org.eclipse.che.ide.resource.Path.SEPARATOR;

import org.eclipse.che.ide.resource.Path;

/**
 * @author Alexander Andrienko
 * @author Mykola Morhun
 */
public class PathEncoder {

  private PathEncoder() {}

  /** Returns path encoded by segments without device. */
  public static String encodePath(Path path) {
    StringBuilder encodedPath = new StringBuilder();

    if (path.hasLeadingSeparator()) {
      encodedPath.append(SEPARATOR);
    }

    String segment;
    for (int i = 0; i < path.segmentCount(); i++) {
      segment = path.segment(i);
      encodedPath.append(encodePathSegment(segment));
      encodedPath.append(SEPARATOR);
    }

    if (!path.isEmpty() && !path.isRoot() && !path.hasTrailingSeparator()) {
      encodedPath.deleteCharAt(encodedPath.length() - 1);
    }

    return encodedPath.toString();
  }
}
