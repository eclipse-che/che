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
