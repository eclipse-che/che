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
package org.eclipse.che.plugin.optimized.testing.ide;

import static org.eclipse.che.api.testing.shared.OptimizedConstants.SMART_TESTING_FINDER_NAME;

import org.eclipse.che.api.testing.shared.OptimizedConstants;
import org.eclipse.che.api.testing.shared.TestExecutionContext;

public class OptimizedTestExecutionModifier {

  public static void modifyTestExecutionContext(TestExecutionContext testExecutionContext) {
    testExecutionContext
        .getTestContextParameters()
        .put(OptimizedConstants.TEST_CONTEXT_PARAMETER_OPTIMIZED_IS_ENABLED, String.valueOf(true));
    testExecutionContext.setTestFinderName(SMART_TESTING_FINDER_NAME);
  }
}
