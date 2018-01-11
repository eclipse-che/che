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
package org.eclipse.che.plugin.testing.ide.messages;

/** Data class represents test finished message. */
public class TestFinished extends BaseTestMessage {

  TestFinished() {}

  @Override
  public void visit(TestingMessageVisitor visitor) {
    visitor.visitTestFinished(this);
  }

  public Integer getTestDuration() {
    try {
      String duration = getAttributeValue("duration");
      if (duration == null) {
        return 0;
      }
      return Integer.valueOf(duration);
    } catch (NumberFormatException e) {
      return 0;
    }
  }
}
