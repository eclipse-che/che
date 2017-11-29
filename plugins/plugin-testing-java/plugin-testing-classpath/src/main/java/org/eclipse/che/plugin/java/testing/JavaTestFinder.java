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
package org.eclipse.che.plugin.java.testing;

import java.util.List;
import org.eclipse.che.api.testing.shared.TestExecutionContext;
import org.eclipse.jdt.core.IJavaProject;

public interface JavaTestFinder {

  /**
   * Finds tests that should be executed.
   *
   * @param context information about test runner
   * @param javaProject current project
   * @param methodAnnotation java annotation which describes test method in the test framework
   * @param classAnnotation java annotation which describes test class in the test framework
   * @return list of full qualified names of test classes. If it is the declaration of a test method
   *     it should be: parent fqn + '#' + method name (a.b.c.ClassName#methodName)
   */
  List<String> findTests(
      TestExecutionContext context,
      IJavaProject javaProject,
      String methodAnnotation,
      String classAnnotation);

  /**
   * Returns name of the implementation of {@link JavaTestFinder} interface
   *
   * @return A name of the implementation of {@link JavaTestFinder} interface
   */
  String getName();
}
