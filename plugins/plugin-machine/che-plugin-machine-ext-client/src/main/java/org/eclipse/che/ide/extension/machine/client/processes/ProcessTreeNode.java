/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.extension.machine.client.processes;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.ui.tree.TreeNodeElement;
import org.eclipse.che.ide.util.UUID;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;
import java.util.Collection;

/**
 * The class describes node in process tree (it can be machine, process or terminal).
 *
 * @author Anna Shumilova
 */
public class ProcessTreeNode {

    /** The set of nodes. */
    public enum ProcessNodeType {
        ROOT_NODE,
        MACHINE_NODE,
        COMMAND_NODE,
        TERMINAL_NODE
    }

    public final static String ROOT = "root";

    private final ProcessNodeType                  type;
    private final String                           id;
    private final String                           displayName;
    private final ProcessTreeNode                  parent;
    private final Object                           data;
    private final SVGResource                      icon;
    private final Collection<ProcessTreeNode>      children;
    private       TreeNodeElement<ProcessTreeNode> treeNodeElement;

    private boolean                                hasUnreadContent;
    private boolean                                hasTerminalAgent;
    private boolean                                hasSSHAgent;

    private boolean                                running;

    public ProcessTreeNode(ProcessNodeType type,
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
                if (data instanceof MachineEntity) {
                    MachineEntity machine = (MachineEntity)data;
                    id = machine.getId();
                    displayName = machine.getDisplayName();
                } else {
                    throw new IllegalArgumentException("Data type is not a machine setting default value");
                }

                break;
            case COMMAND_NODE:
                id = data + UUID.uuid();
                displayName = (String)data;
                break;
            case TERMINAL_NODE:
                id = data + UUID.uuid();
                displayName = (String)data;
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

    public boolean hasTerminalAgent() {
        return hasTerminalAgent;
    }

    public void setHasTerminalAgent(boolean hasTerminalAgent) {
        this.hasTerminalAgent = hasTerminalAgent;
    }

    public boolean hasSSHAgent() {
        return hasSSHAgent;
    }

    public void setHasSSHAgent(boolean hasSSHAgent) {
        this.hasSSHAgent = hasSSHAgent;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProcessTreeNode that = (ProcessTreeNode)o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
