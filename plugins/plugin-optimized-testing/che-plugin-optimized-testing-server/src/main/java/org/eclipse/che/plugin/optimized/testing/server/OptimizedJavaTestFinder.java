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
package org.eclipse.che.plugin.optimized.testing.server;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.api.SmartTesting;
import org.arquillian.smart.testing.configuration.Configuration;
import org.eclipse.che.api.testing.shared.OptimizedConstants;
import org.eclipse.che.api.testing.shared.TestExecutionContext;
import org.eclipse.che.plugin.java.testing.DefaultJavaTestFinder;
import org.eclipse.che.plugin.java.testing.JavaTestFinder;
import org.eclipse.jdt.core.IJavaProject;

public class OptimizedJavaTestFinder implements JavaTestFinder {

  @Override
  public List<String> findTests(
      TestExecutionContext context,
      IJavaProject javaProject,
      String methodAnnotation,
      String classAnnotation) {

    return findTests(
        context, javaProject, methodAnnotation, classAnnotation, new DefaultJavaTestFinder());
  }

  protected List<String> findTests(
      TestExecutionContext context,
      IJavaProject javaProject,
      String methodAnnotation,
      String classAnnotation,
      JavaTestFinder javaTestFinder) {

    List<String> suite =
        javaTestFinder.findTests(context, javaProject, methodAnnotation, classAnnotation);
    String shouldOptimize =
        context
            .getTestContextParameters()
            .get(OptimizedConstants.TEST_CONTEXT_PARAMETER_OPTIMIZED_IS_ENABLED);

    if (!suite.isEmpty() && Boolean.valueOf(shouldOptimize)) {
      return optimize(suite, context);
    }
    return suite;
  }

  @Override
  public String getName() {
    return OptimizedConstants.SMART_TESTING_FINDER_NAME;
  }

  private List<String> optimize(final List<String> suite, TestExecutionContext context) {
    Configuration configuration = SmartTestingConfigLoader.load(context.getProjectPath());
    Set<TestSelection> selection =
        SmartTesting.with(className -> suite.contains(className), configuration)
            .in(Paths.get("").toAbsolutePath() + context.getProjectPath())
            .applyOnNames(suite);
    return new ArrayList<>(SmartTesting.getNames(selection));
  }
}
