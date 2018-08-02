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

import org.eclipse.che.plugin.testing.ide.messages.TestFailed;

/** Event when test was failed. */
public class TestFailedEvent extends TestNodeEvent {

  private final String failureMessage;
  private final String stackTrace;
  private final boolean error;

  public TestFailedEvent(TestFailed message) {
    super(getNodeId(message), message.getTestName());
    failureMessage = message.getFailureMessage();
    stackTrace = message.getStackTrace();
    error = message.isError();
    // TODO add additional info about failed test
  }

  public String getFailureMessage() {
    return failureMessage;
  }

  public boolean isError() {
    return error;
  }

  public String getStackTrace() {
    return stackTrace;
  }
}
