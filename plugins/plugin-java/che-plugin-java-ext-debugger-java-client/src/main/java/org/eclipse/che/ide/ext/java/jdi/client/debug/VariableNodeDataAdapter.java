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
package org.eclipse.che.ide.ext.java.jdi.client.debug;

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
public class VariableNodeDataAdapter implements NodeDataAdapter<DebuggerVariable> {
    private HashMap<DebuggerVariable, TreeNodeElement<DebuggerVariable>> treeNodeElements = new HashMap<>();

    /** {@inheritDoc} */
    @Override
    public int compare(@NotNull DebuggerVariable a, @NotNull DebuggerVariable b) {
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
    public boolean hasChildren(@NotNull DebuggerVariable data) {
        return !data.isPrimitive();
    }

    /** {@inheritDoc} */
    @Override
    @NotNull
    public List<DebuggerVariable> getChildren(@NotNull DebuggerVariable data) {
        return data.getVariables();
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public String getNodeId(@NotNull DebuggerVariable data) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    @NotNull
    public String getNodeName(@NotNull DebuggerVariable data) {
        return data.getName() + ": " + data.getValue();
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public DebuggerVariable getParent(@NotNull DebuggerVariable data) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public TreeNodeElement<DebuggerVariable> getRenderedTreeNode(@NotNull DebuggerVariable data) {
        return treeNodeElements.get(data);
    }

    /** {@inheritDoc} */
    @Override
    public void setNodeName(@NotNull DebuggerVariable data,@NotNull String name) {
        // do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void setRenderedTreeNode(@NotNull DebuggerVariable data,@NotNull TreeNodeElement<DebuggerVariable> renderedNode) {
        treeNodeElements.put(data, renderedNode);
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public DebuggerVariable getDragDropTarget(@NotNull DebuggerVariable data) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    @NotNull
    public List<String> getNodePath(@NotNull DebuggerVariable data) {
        return new ArrayList<>(data.getVariablePath().getPath());
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public DebuggerVariable getNodeByPath(@NotNull DebuggerVariable root,@NotNull List<String> relativeNodePath) {
        DebuggerVariable localRoot = root;
        for (int i = 0; i < relativeNodePath.size(); i++) {
            String path = relativeNodePath.get(i);
            if (localRoot != null) {
                List<DebuggerVariable> variables = new ArrayList<>(localRoot.getVariables());
                localRoot = null;
                for (int j = 0; j < variables.size(); j++) {
                    DebuggerVariable variable = variables.get(i);
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
