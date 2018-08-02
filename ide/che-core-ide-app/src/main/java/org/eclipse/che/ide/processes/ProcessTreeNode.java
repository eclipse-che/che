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
package org.eclipse.che.ide.processes;

import java.util.Collection;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.ui.tree.TreeNodeElement;
import org.eclipse.che.ide.util.UUID;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * The class describes node in process tree (it can be machine, process or terminal).
 *
 * @author Anna Shumilova
 */
public class ProcessTreeNode {

  public static final String ROOT = "root";
  private final ProcessNodeType type;
  private final String id;
  private final String displayName;
  private final Object data;
  private final SVGResource icon;
  private final Collection<ProcessTreeNode> children;
  private ProcessTreeNode parent;
  private TreeNodeElement<ProcessTreeNode> treeNodeElement;
  private boolean hasUnreadContent;
  private boolean terminalServerRunning;
  private boolean sshServerRunning;

  public ProcessTreeNode(
      ProcessNodeType type,
      ProcessTreeNode parent,
      Object data,
      SVGResource icon,
      Collection<ProcessTreeNode> children) {
    this.type = type;
    this.parent = parent;
    this.data = data;
    this.icon = icon;
    this.children = children;

    switch (type) {
      case MACHINE_NODE:
        if (data instanceof String) {
          String machineName = (String) data;
          id = machineName;
          displayName = machineName;
        } else {
          throw new IllegalArgumentException("Data type is not a machine setting default value");
        }

        break;
      case COMMAND_NODE:
        id = data + UUID.uuid();
        displayName = (String) data;
        break;
      case TERMINAL_NODE:
        id = data + UUID.uuid();
        displayName = (String) data;
        break;
      default:
        id = ROOT;
        displayName = ROOT;
    }
  }

  @NotNull
  public String getId() {
    return id;
  }

  @NotNull
  public String getName() {
    return displayName;
  }

  @NotNull
  public ProcessNodeType getType() {
    return type;
  }

  @NotNull
  public ProcessTreeNode getParent() {
    return parent;
  }

  public void setParent(ProcessTreeNode parent) {
    this.parent = parent;
  }

  @NotNull
  public Object getData() {
    return data;
  }

  @Nullable
  public SVGResource getTitleIcon() {
    return icon;
  }

  @Nullable
  public Collection<ProcessTreeNode> getChildren() {
    return children;
  }

  @NotNull
  public TreeNodeElement<ProcessTreeNode> getTreeNodeElement() {
    return treeNodeElement;
  }

  public void setTreeNodeElement(@NotNull TreeNodeElement<ProcessTreeNode> treeNodeElement) {
    this.treeNodeElement = treeNodeElement;
  }

  public boolean hasUnreadContent() {
    return hasUnreadContent;
  }

  public void setHasUnreadContent(boolean hasUnreadContent) {
    this.hasUnreadContent = hasUnreadContent;
  }

  public boolean isTerminalServerRunning() {
    return terminalServerRunning;
  }

  public void setTerminalServerRunning(boolean terminalServerRunning) {
    this.terminalServerRunning = terminalServerRunning;
  }

  public boolean isSshServerRunning() {
    return sshServerRunning;
  }

  public void setSshServerRunning(boolean sshServerRunning) {
    this.sshServerRunning = sshServerRunning;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ProcessTreeNode that = (ProcessTreeNode) o;

    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  /** The set of nodes. */
  public enum ProcessNodeType {
    ROOT_NODE("root"),
    MACHINE_NODE("machine"),
    COMMAND_NODE("command"),
    TERMINAL_NODE("terminal");

    private String value;

    ProcessNodeType(String value) {
      this.value = value;
    }

    public String getStringValue() {
      return value;
    }
  }
}
