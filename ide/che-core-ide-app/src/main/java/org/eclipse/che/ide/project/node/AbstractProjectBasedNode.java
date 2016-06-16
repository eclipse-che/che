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
package org.eclipse.che.ide.project.node;

import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.data.tree.AbstractTreeNode;
import org.eclipse.che.ide.api.data.HasDataObject;
import org.eclipse.che.ide.api.project.node.HasProjectConfig;
import org.eclipse.che.ide.api.data.tree.settings.HasSettings;
import org.eclipse.che.ide.api.data.tree.settings.NodeSettings;
import org.eclipse.che.ide.ui.smartTree.presentation.HasPresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;

import javax.validation.constraints.NotNull;

/**
 * Base class for the project related nodes.
 *
 * @author Vlad Zhukovskiy
 */
public abstract class AbstractProjectBasedNode<DataObject> extends AbstractTreeNode implements HasDataObject<DataObject>,
                                                                                               HasPresentation,
                                                                                               HasProjectConfig,
                                                                                               HasSettings {
    public static final String CUSTOM_BACKGROUND_FILL = "background";

    private DataObject       dataObject;
    private ProjectConfigDto projectConfig;
    private NodeSettings     nodeSettings;
    private NodePresentation nodePresentation;

    public AbstractProjectBasedNode(DataObject dataObject,
                                    ProjectConfigDto projectConfig,
                                    NodeSettings nodeSettings) {
        this.dataObject = dataObject;
        this.projectConfig = projectConfig;
        this.nodeSettings = nodeSettings;
    }

    @Override
    public ProjectConfigDto getProjectConfig() {
        if (getParent() != null
            && getParent() instanceof HasProjectConfig
            && ((HasProjectConfig)getParent()).getProjectConfig().equals(projectConfig)) {
            return ((HasProjectConfig)getParent()).getProjectConfig();
        }

        return projectConfig;
    }

    @Override
    public void setProjectConfig(ProjectConfigDto projectConfig) {
        if (getParent() != null && getParent() instanceof HasProjectConfig) {
            ((HasProjectConfig)getParent()).setProjectConfig(projectConfig);
            return;
        }

        this.projectConfig = projectConfig;
    }

    @Override
    public NodeSettings getSettings() {
        return nodeSettings;
    }

    @NotNull
    @Override
    public DataObject getData() {
        return dataObject;
    }

    @Override
    public void setData(@NotNull DataObject data) {
        this.dataObject = data;
    }

    @Override
    public final NodePresentation getPresentation(boolean update) {
        if (nodePresentation == null) {
            nodePresentation = new NodePresentation();
            updatePresentation(nodePresentation);
        }

        if (update) {
            updatePresentation(nodePresentation);
        }
        return nodePresentation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractProjectBasedNode)) return false;

        AbstractProjectBasedNode that = (AbstractProjectBasedNode)o;

        if (!dataObject.equals(that.dataObject)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return dataObject.hashCode();
    }
}
