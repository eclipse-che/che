/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.panel;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.ui.tree.TreeNodeElement;

import javax.validation.constraints.NotNull;
import java.util.Collection;

/**
 * The class describes node in machine tree and contains special information about node.
 *
 * @author Dmitry Shnurenko
 */
public class MachineTreeNode {

    public final static String ROOT = "root";

    private final String                      id;
    private final String                      name;
    private final MachineTreeNode             parent;
    private final Collection<MachineTreeNode> children;

    private Object                      data;
    private TreeNodeElement<MachineTreeNode> treeNodeElement;

    @Inject
    public MachineTreeNode(@Assisted MachineTreeNode parent,
                           @Assisted("data") Object data,
                           @Assisted Collection<MachineTreeNode> children) {
        this.parent = parent;
        this.data = data;
        this.children = children;

        if (data instanceof Machine) {
            Machine machine = (Machine)data;
            id = machine.getId();
            name = machine.getConfig().getName();
        } else {
            id = ROOT;
            name = ROOT;
        }
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public MachineTreeNode getParent() {
        return parent;
    }

    @NotNull
    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Nullable
    public Collection<MachineTreeNode> getChildren() {
        return children;
    }

    @NotNull
    public TreeNodeElement<MachineTreeNode> getTreeNodeElement() {
        return treeNodeElement;
    }

    public void setTreeNodeElement(@NotNull TreeNodeElement<MachineTreeNode> treeNodeElement) {
        this.treeNodeElement = treeNodeElement;
    }
}
