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
package org.eclipse.che.ide.ext.java.client.project.node.jar;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.api.project.node.settings.NodeSettings;
import org.eclipse.che.ide.ext.java.client.project.node.JavaNodeManager;
import org.eclipse.che.ide.project.node.SyntheticBasedNode;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Vlad Zhukovskiy
 */
public class ExternalLibrariesNode extends SyntheticBasedNode<String> {

    private final JavaNodeManager javaNodeManager;

    @Inject
    public ExternalLibrariesNode(@Assisted ProjectConfigDto projectConfig,
                                 @Assisted NodeSettings nodeSettings,
                                 JavaNodeManager javaNodeManager) {
        super("External-Libraries:" + projectConfig.getPath(), projectConfig, nodeSettings);
        this.javaNodeManager = javaNodeManager;
    }

    @Override
    protected Promise<List<Node>> getChildrenImpl() {
        return javaNodeManager.getExternalLibraries(getProjectConfig());
    }

    @Override
    public void updatePresentation(@NotNull NodePresentation presentation) {
        presentation.setPresentableIcon(javaNodeManager.getJavaNodesResources().externalLibraries());
        presentation.setPresentableText(getName());
    }

    @NotNull
    @Override
    public String getName() {
        return "External Libraries";
    }

    @Override
    public boolean isLeaf() {
        return false;
    }
}
