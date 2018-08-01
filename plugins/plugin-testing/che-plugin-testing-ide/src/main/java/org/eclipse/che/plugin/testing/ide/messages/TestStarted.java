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

/** Data class represents test started message. */
public class TestStarted extends BaseTestMessage {

  TestStarted() {}

  @Override
  public void visit(TestingMessageVisitor visitor) {
    visitor.visitTestStarted(this);
  }

  public String getLocation() {
    return getAttributeValue("locationHint");
  }

  public boolean isConfig() {
    String config = getAttributeValue("config");
    return !(config == null || config.isEmpty()) && Boolean.parseBoolean(config);
  }
}
