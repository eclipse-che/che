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
package org.eclipse.che.plugin.testing.ide.messages;

/** Data class represents test failed message. */
public class TestFailed extends BaseTestMessage {

  TestFailed() {}

  @Override
  public void visit(TestingMessageVisitor visitor) {
    visitor.visitTestFailed(this);
  }

  public String getFailureMessage() {
    return getAttributeValue("message");
  }

  public String getStackTrace() {
    return getAttributeValue("details");
  }

  public boolean isError() {
    String error = getAttributeValue("error");
    if (error == null) {
      return false;
    }
    return Boolean.valueOf(error);
  }

  // TODO there should be more info about failure like comparison result
}
