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
