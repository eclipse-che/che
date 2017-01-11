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
package org.eclipse.che.plugin.debugger.ide.debug;

import org.eclipse.che.api.debug.shared.model.MutableVariable;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.ui.tree.NodeDataAdapter;
import org.eclipse.che.ide.ui.tree.TreeNodeElement;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The adapter for debug variable node.
 *
 * @author Andrey Plotnikov
 * @@author Dmitry Shnurenko
 */
public class VariableNodeDataAdapter implements NodeDataAdapter<MutableVariable> {
    private HashMap<MutableVariable, TreeNodeElement<MutableVariable>> treeNodeElements = new HashMap<>();

    /** {@inheritDoc} */
    @Override
    public int compare(@NotNull MutableVariable a, @NotNull MutableVariable b) {
        List<String> pathA = a.getVariablePath().getPath();
        List<String> pathB = b.getVariablePath().getPath();

        for (int i = 0; i < pathA.size(); i++) {
            String elementA = pathA.get(i);
            String elementB = pathB.get(i);

            int compare = elementA.compareTo(elementB);
            if (compare != 0) {
                return compare;
            }
        }

        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasChildren(@NotNull MutableVariable data) {
        return !data.isPrimitive();
    }

    /** {@inheritDoc} */
    @Override
    @NotNull
    public List<MutableVariable> getChildren(@NotNull MutableVariable data) {
        return data.getVariables();
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public String getNodeId(@NotNull MutableVariable data) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    @NotNull
    public String getNodeName(@NotNull MutableVariable data) {
        return data.getName() + ": " + data.getValue();
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public MutableVariable getParent(@NotNull MutableVariable data) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public TreeNodeElement<MutableVariable> getRenderedTreeNode(@NotNull MutableVariable data) {
        return treeNodeElements.get(data);
    }

    /** {@inheritDoc} */
    @Override
    public void setNodeName(@NotNull MutableVariable data,@NotNull String name) {
        // do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void setRenderedTreeNode(@NotNull MutableVariable data,@NotNull TreeNodeElement<MutableVariable> renderedNode) {
        treeNodeElements.put(data, renderedNode);
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public MutableVariable getDragDropTarget(@NotNull MutableVariable data) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    @NotNull
    public List<String> getNodePath(@NotNull MutableVariable data) {
        return new ArrayList<>(data.getVariablePath().getPath());
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public MutableVariable getNodeByPath(@NotNull MutableVariable root,@NotNull List<String> relativeNodePath) {
        MutableVariable localRoot = root;
        for (int i = 0; i < relativeNodePath.size(); i++) {
            String path = relativeNodePath.get(i);
            if (localRoot != null) {
                List<MutableVariable> variables = new ArrayList<>(localRoot.getVariables());
                localRoot = null;
                for (int j = 0; j < variables.size(); j++) {
                    MutableVariable variable = variables.get(i);
                    if (variable.getName().equals(path)) {
                        localRoot = variable;
                        break;
                    }
                }

                if (i == (relativeNodePath.size() - 1)) {
                    return localRoot;
                }
            }
        }
        return null;
    }

}
