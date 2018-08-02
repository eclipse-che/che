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

import org.eclipse.che.plugin.testing.ide.messages.BaseTestMessage;

/** Event which informs about some output message from the test. */
public class TestOutputEvent extends TestNodeEvent {
  private final String text;
  private final boolean sdtout;

  public TestOutputEvent(BaseTestMessage message, String text, boolean sdtout) {
    super(getNodeId(message), message.getTestName());
    this.text = text;
    this.sdtout = sdtout;
  }

  public String getText() {
    return text;
  }

  public boolean isSdtout() {
    return sdtout;
  }
}
