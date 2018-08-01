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
package org.eclipse.che.plugin.testing.ide.model.info;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.plugin.testing.ide.model.Printer;

/** Describes information about failed tests. */
public class CompaundTestFailedInfo extends TestFailedInfo {

  private final List<TestFailedInfo> failedTests = new ArrayList<>();

  public CompaundTestFailedInfo(String message, String stackTrace) {
    super(message, stackTrace);
  }

  public void addFailedTest(TestFailedInfo failedInfo) {
    failedTests.add(failedInfo);
  }

  @Override
  public void print(Printer printer) {
    for (TestFailedInfo failedTest : failedTests) {
      failedTest.print(printer);
    }
  }
}
