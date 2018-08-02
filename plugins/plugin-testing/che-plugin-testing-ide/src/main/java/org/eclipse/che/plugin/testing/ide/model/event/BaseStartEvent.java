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

import org.eclipse.che.plugin.testing.ide.messages.ClientTestingMessage;

/** Base start event. */
public class BaseStartEvent extends TestNodeEvent {
  private final String parentId;
  private final String location;
  private final String nodeType;
  private final String nodeArg;
  private final boolean isRunning;

  public BaseStartEvent(
      String id,
      String name,
      String parentId,
      String location,
      String nodeType,
      String nodeArg,
      boolean isRunning) {
    super(id, name);
    this.parentId = parentId;
    this.location = location;
    this.nodeType = nodeType;
    this.nodeArg = nodeArg;
    this.isRunning = isRunning;
  }

  protected static String getParantNodeId(ClientTestingMessage message) {
    return message.getAttributes().get("parentNodeId");
  }

  protected static String getNodeType(ClientTestingMessage message) {
    return message.getAttributes().get("nodeType");
  }

  protected static String getNodeArg(ClientTestingMessage message) {
    return message.getAttributes().get("nodeArgs");
  }

  protected static Boolean isNodeRunning(ClientTestingMessage message) {
    String running = message.getAttributes().get("running");
    return running == null || running.isEmpty() || Boolean.parseBoolean(running);
  }

  public String getParentId() {
    return parentId;
  }

  public String getLocation() {
    return location;
  }

  public String getNodeType() {
    return nodeType;
  }

  public String getNodeArg() {
    return nodeArg;
  }

  public boolean isRunning() {
    return isRunning;
  }
}
