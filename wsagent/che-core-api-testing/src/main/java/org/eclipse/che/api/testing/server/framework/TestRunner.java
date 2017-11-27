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
package org.eclipse.che.api.testing.server.framework;

import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.testing.shared.TestDetectionContext;
import org.eclipse.che.api.testing.shared.TestExecutionContext;
import org.eclipse.che.api.testing.shared.TestPosition;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.execution.ProcessHandler;

/**
 * Interface for defining test frameworks for the test runner. All test framework implementations
 * should implement this interface in order to register for the test runner service.
 *
 * @author Mirage Abeysekara
 */
public interface TestRunner {

  /**
   * Executes tests.
   *
   * @param context information about test execution
   * @return handler for tests executing process
   * @throws Exception if something happened before tests were executed
   */
  @Nullable
  ProcessHandler execute(TestExecutionContext context) throws Exception;

  /**
   * The test runner framework will call this method to get the framework name for registration.
   *
   * @return the implementation framework name
   */
  String getName();

  /** @return port for connecting to the debugger */
  int getDebugPort();

  /**
   * Detect is any test present at given context
   *
   * @param context the current context
   * @return list of the test positions if any test present, empty list otherwise
   */
  @NotNull
  List<TestPosition> detectTests(TestDetectionContext context);
}
