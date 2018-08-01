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

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.plugin.testing.ide.messages.ClientTestingMessage;

/** Core event for the test events. */
public abstract class TestNodeEvent {
  private String id;
  private String name;

  public TestNodeEvent(String id, String name) {
    this.id = id;
    this.name = name;
  }

  protected static String getNodeId(ClientTestingMessage message) {
    return message.getAttributes().get("nodeId");
  }

  @Nullable
  public String getId() {
    return id;
  }

  @Nullable
  public String getName() {
    return name;
  }
}
