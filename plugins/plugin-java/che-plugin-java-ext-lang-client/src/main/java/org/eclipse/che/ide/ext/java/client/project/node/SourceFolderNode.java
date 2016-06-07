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
package org.eclipse.che.ide.ext.java.client.project.node;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.project.node.resource.RenameProcessor;
import org.eclipse.che.ide.ext.java.client.project.settings.JavaNodeSettings;
import org.eclipse.che.ide.ext.java.shared.ContentRoot;
import org.eclipse.che.ide.project.node.FolderReferenceNode;
import org.eclipse.che.ide.project.node.resource.ItemReferenceProcessor;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Node that represent a java source folder.
 * It may be source, test source, resource and test resource folder type.
 *
 * @author Vlad Zhukovskiy
 */
public class SourceFolderNode extends FolderReferenceNode {
    private final ContentRoot     contentRootType;
    private final JavaNodeManager nodeManager;

    @Inject
    public SourceFolderNode(@Assisted ItemReference itemReference,
                            @Assisted ProjectConfigDto projectConfig,
                            @Assisted JavaNodeSettings nodeSettings,
                            @Assisted ContentRoot contentRootType,
                            @NotNull EventBus eventBus,
                            @NotNull JavaNodeManager nodeManager,
                            @NotNull ItemReferenceProcessor resourceProcessor) {
        super(itemReference, projectConfig, nodeSettings, eventBus, nodeManager, resourceProcessor);
        this.contentRootType = contentRootType;
        this.nodeManager = nodeManager;
    }

    public ContentRoot getContentRootType() {
        return contentRootType;
    }

    @Override
    protected Promise<List<Node>> getChildrenImpl() {
        return nodeManager.getChildren(getStorablePath(), getProjectConfig(), getSettings());
    }

    @Override
    public void updatePresentation(@NotNull NodePresentation presentation) {
        switch (contentRootType) {
            case SOURCE:
                presentation.setPresentableIcon(nodeManager.getJavaNodesResources().sourceFolder());
                break;
            case TEST_SOURCE:
                presentation.setPresentableIcon(nodeManager.getJavaNodesResources().testFolder());
                break;
            case RESOURCE:
            case TEST_RESOURCE:
                presentation.setPresentableIcon(nodeManager.getNodesResources().simpleFolder());
        }

        presentation.setPresentableText(getData().getName());
    }

    @Override
    public RenameProcessor<ItemReference> getRenameProcessor() {
        return null;
    }
}
