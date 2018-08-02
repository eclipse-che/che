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
package org.eclipse.che.plugin.testing.ide.model.event;

import org.eclipse.che.plugin.testing.ide.messages.TestSuiteStarted;

/** Event which informs about starting test suite. */
public class TestSuiteStartedEvent extends BaseStartEvent {
  public TestSuiteStartedEvent(TestSuiteStarted testSuiteStarted) {
    super(
        getNodeId(testSuiteStarted),
        testSuiteStarted.getSuiteName(),
        getParantNodeId(testSuiteStarted),
        testSuiteStarted.getLocation(),
        getNodeType(testSuiteStarted),
        getNodeArg(testSuiteStarted),
        isNodeRunning(testSuiteStarted));
  }
}
