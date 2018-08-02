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

import org.eclipse.che.plugin.testing.ide.messages.TestIgnored;

/** Event which informs about ignoring test. */
public class TestIgnoredEvent extends TestNodeEvent {

  private final String ignoreComment;
  private final String stackStrace;

  public TestIgnoredEvent(TestIgnored message) {
    super(getNodeId(message), message.getTestName());
    ignoreComment = message.getIgnoreComment();
    stackStrace = message.getStackStrace();
  }

  public String getIgnoreComment() {
    return ignoreComment;
  }

  public String getStackStrace() {
    return stackStrace;
  }
}
