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
package org.eclipse.che.plugin.testing.testng.ide;

import com.google.common.collect.Sets;
import java.util.Set;
import org.eclipse.che.plugin.testing.ide.detector.TestFileExtension;

/** Describes file extensions for TestNg test framework. */
public class TestNgTestFileExtension implements TestFileExtension {
  private Set<String> testNgExtensions;

  public TestNgTestFileExtension() {
    testNgExtensions = Sets.newHashSet(".java", ".xml");
  }

  @Override
  public Set<String> getExtensions() {
    return testNgExtensions;
  }
}
