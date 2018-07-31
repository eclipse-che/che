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
package org.eclipse.che.api.search.server.excludes;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import javax.inject.Singleton;

@Singleton
public class DotNumberSignExcludeMatcher implements PathMatcher {

  @Override
  public boolean matches(Path fsPath) {
    for (Path pathElement : fsPath) {
      if (pathElement == null || ".#".equals(pathElement.toString())) {
        return true;
      }
    }
    return false;
  }
}
