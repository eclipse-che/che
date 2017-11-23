/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.project.server.matchers;

import static org.eclipse.che.api.project.shared.Constants.CHE_DIR;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import javax.inject.Singleton;

/**
 * Performs match operation on paths to test whether specified item is .che folder.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class DotCheMatcher implements PathMatcher {

  @Override
  public boolean matches(Path fsPath) {
    for (Path pathElement : fsPath) {
      if (pathElement != null && CHE_DIR.equals(pathElement.toString())) {
        return true;
      }
    }
    return false;
  }
}
