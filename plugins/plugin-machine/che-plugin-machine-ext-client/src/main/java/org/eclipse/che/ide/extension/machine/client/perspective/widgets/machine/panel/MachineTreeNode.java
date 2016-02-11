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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.panel;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.machine.shared.dto.MachineStateDto;
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
    private final Object                      data;
    private final Collection<MachineTreeNode> children;

    private TreeNodeElement<MachineTreeNode> treeNodeElement;

    @Inject
    public MachineTreeNode(@Assisted MachineTreeNode parent,
                           @Assisted("data") Object data,
                           @Assisted Collection<MachineTreeNode> children) {
        this.parent = parent;
        this.data = data;
        this.children = children;

        boolean isMachine = data instanceof MachineStateDto;

        id = isMachine ? ((MachineStateDto)data).getId() : ROOT;
        name = isMachine ? ((MachineStateDto)data).getName() : ROOT;
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
