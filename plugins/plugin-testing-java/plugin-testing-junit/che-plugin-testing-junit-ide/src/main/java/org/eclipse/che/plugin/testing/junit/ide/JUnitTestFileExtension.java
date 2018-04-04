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
package org.eclipse.che.plugin.testing.junit.ide;

import com.google.common.collect.Sets;
import java.util.Set;
import org.eclipse.che.plugin.testing.ide.detector.TestFileExtension;

/** Describes file extensions for JUnit test framework. */
public class JUnitTestFileExtension implements TestFileExtension {
  private Set<String> jUnitExtensions;

  public JUnitTestFileExtension() {
    jUnitExtensions = Sets.newHashSet(".java");
  }

  @Override
  public Set<String> getExtensions() {
    return jUnitExtensions;
  }
}
