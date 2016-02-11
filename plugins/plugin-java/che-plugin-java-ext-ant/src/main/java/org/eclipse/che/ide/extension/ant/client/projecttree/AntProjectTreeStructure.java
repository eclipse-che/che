/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.extension.ant.client.projecttree;

import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.api.project.tree.AbstractTreeNode;
import org.eclipse.che.ide.api.project.tree.TreeNode;
import org.eclipse.che.ide.ext.java.client.navigation.JavaNavigationService;
import org.eclipse.che.ide.ext.java.client.projecttree.JavaTreeStructure;
import org.eclipse.che.ide.ext.java.client.projecttree.nodes.JavaFolderNode;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Tree structure for Ant project.
 *
 * @author Vladyslav Zhukovskii
 */
public class AntProjectTreeStructure extends JavaTreeStructure {

    /** Create instance of {@link AntProjectTreeStructure}. */
    protected AntProjectTreeStructure(AntNodeFactory nodeFactory, EventBus eventBus, AppContext appContext,
                                      ProjectServiceClient projectServiceClient, IconRegistry iconRegistry,
                                      DtoUnmarshallerFactory dtoUnmarshallerFactory, JavaNavigationService service) {
        super(nodeFactory, eventBus, appContext, projectServiceClient, iconRegistry, dtoUnmarshallerFactory, service);
    }

    /** {@inheritDoc} */
    @Override
    public void getRootNodes(@NotNull AsyncCallback<List<TreeNode<?>>> callback) {
        if (projectNode == null) {
            final CurrentProject currentProject = appContext.getCurrentProject();
            if (currentProject != null) {
                projectNode = getNodeFactory().newAntProjectNode(null, currentProject.getRootProject(), this);
            } else {
                callback.onFailure(new IllegalStateException("No project is opened."));
                return;
            }
        }
        List<TreeNode<?>> parent = new ArrayList<>();
        parent.add(projectNode);
        callback.onSuccess(parent);
    }

    @Override
    public AntNodeFactory getNodeFactory() {
        return (AntNodeFactory)nodeFactory;
    }

    /** {@inheritDoc} */
    @Override
    public JavaFolderNode newJavaFolderNode(@NotNull AbstractTreeNode parent, @NotNull ItemReference data) {
        return getNodeFactory().newAntFolderNode(parent, data, this);
    }
}
