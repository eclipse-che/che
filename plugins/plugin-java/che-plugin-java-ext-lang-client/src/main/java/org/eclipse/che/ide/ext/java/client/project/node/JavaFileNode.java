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
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.project.node.MutableNode;
import org.eclipse.che.ide.api.project.node.resource.DeleteProcessor;
import org.eclipse.che.ide.api.project.node.resource.RenameProcessor;
import org.eclipse.che.ide.ext.java.client.project.settings.JavaNodeSettings;
import org.eclipse.che.ide.project.node.FileReferenceNode;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;

import javax.validation.constraints.NotNull;

/**
 * @author Vlad Zhukovskiy
 */
public class JavaFileNode extends FileReferenceNode implements MutableNode {

    @Inject
    public JavaFileNode(@Assisted ItemReference itemReference,
                        @Assisted ProjectConfigDto projectConfig,
                        @Assisted JavaNodeSettings nodeSettings,
                        @NotNull EventBus eventBus,
                        @NotNull AppContext appContext,
                        @NotNull JavaNodeManager nodeManager,
                        @NotNull JavaItemReferenceProcessor resourceProcessor) {
        super(itemReference, projectConfig, nodeSettings, eventBus, appContext, nodeManager, resourceProcessor);
    }

    @Override
    public void updatePresentation(@NotNull NodePresentation presentation) {
        super.updatePresentation(presentation);
        presentation.setPresentableText(getDisplayName());
    }

    @Override
    public boolean isLeaf() {
        //TODO this field will be configurable if we'll have ability to get java file members
        return true;
    }

    @Nullable
    @Override
    public RenameProcessor<ItemReference> getRenameProcessor() {
        return null;
    }

    @Nullable
    @Override
    public DeleteProcessor<ItemReference> getDeleteProcessor() {
        return resourceProcessor;
    }

    @Override
    public String getDisplayName() {
        if (getData().getName().endsWith(JavaNodeManager.JAVA_EXT)) {
            return getData().getName().replace(".java", "");
        } else {
            return getData().getName();
        }
    }

    @Override
    public void setLeaf(boolean leaf) {
        throw new UnsupportedOperationException("Not implemented");
    }


}
