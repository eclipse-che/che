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
package org.eclipse.che.ide.search.presentation;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.project.node.HasAction;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.api.project.node.settings.NodeSettings;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.FileReferenceNode;
import org.eclipse.che.ide.project.node.NodeManager;
import org.eclipse.che.ide.project.node.resource.ItemReferenceProcessor;

/**
 * Node for representing found result.
 *
 * @author Vlad Zhukovskiy
 */
public class FindResultNode extends FileReferenceNode {

    private final ProjectExplorerPresenter projectExplorer;

    @Inject
    public FindResultNode(@Assisted ItemReference itemReference,
                          @Assisted ProjectConfigDto projectConfig,
                          @Assisted NodeSettings nodeSettings,
                          EventBus eventBus,
                          AppContext appContext,
                          NodeManager nodeManager,
                          ItemReferenceProcessor resourceProcessor,
                          ProjectExplorerPresenter projectExplorer) {
        super(itemReference, projectConfig, nodeSettings, eventBus, appContext, nodeManager, resourceProcessor);
        this.projectExplorer = projectExplorer;
    }


    /** {@inheritDoc} */
    @Override
    public void actionPerformed() {
        projectExplorer.getNodeByPath(this, false).then(new Operation<Node>() {
            @Override
            public void apply(Node node) throws OperationException {
                projectExplorer.select(node, false);
                projectExplorer.scrollToNode(node);
                if (node instanceof HasAction) {
                    ((HasAction)node).actionPerformed();
                }
            }
        });
    }
}
