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

import org.eclipse.che.plugin.testing.ide.messages.TestStarted;

/** Event which informs about starting test. */
public class TestStartedEvent extends BaseStartEvent {

  private final boolean config;

  public TestStartedEvent(TestStarted testStarted) {
    super(
        getNodeId(testStarted),
        testStarted.getTestName(),
        getParantNodeId(testStarted),
        testStarted.getLocation(),
        getNodeType(testStarted),
        getNodeArg(testStarted),
        isNodeRunning(testStarted));
    config = testStarted.isConfig();
  }

  public TestStartedEvent(String name, String location) {
    super(null, name, null, location, null, null, true);
    config = false;
  }

  public boolean isConfig() {
    return config;
  }
}
