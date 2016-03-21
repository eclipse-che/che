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

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.api.project.node.resource.DeleteProcessor;
import org.eclipse.che.ide.api.project.node.resource.RenameProcessor;
import org.eclipse.che.ide.api.project.node.settings.NodeSettings;
import org.eclipse.che.ide.project.node.resource.ModuleConfigProcessor;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Vlad Zhukovskiy
 * @author Dmitry Shnurenko
 * @deprecated will be removed after release GA
 */
@Deprecated
public class ModuleNode extends ResourceBasedNode<ProjectConfigDto> implements HasStorablePath {

    private final ModuleConfigProcessor resourceProcessor;
    private final ProjectConfigDto      projectConfigDto;

    @Inject
    public ModuleNode(@Assisted ProjectConfigDto projectConfigDto,
                      @Assisted NodeSettings nodeSettings,
                      EventBus eventBus,
                      NodeManager nodeManager,
                      ModuleConfigProcessor resourceProcessor) {
        super(projectConfigDto, projectConfigDto, nodeSettings, eventBus, nodeManager);
        this.resourceProcessor = resourceProcessor;
        this.projectConfigDto = projectConfigDto;
    }

    @NotNull
    @Override
    protected Promise<List<Node>> getChildrenImpl() {
        return nodeManager.getChildren(projectConfigDto.getPath(), getProjectConfig(), getSettings());
    }

    @Override
    public void updatePresentation(@NotNull NodePresentation presentation) {
        presentation.setPresentableText(getData().getName());
        presentation.setPresentableIcon(nodeManager.getNodesResources().moduleFolder());
        presentation.setPresentableTextCss("font-weight:bold");
    }

    @NotNull
    @Override
    public String getName() {
        return getData().getName();
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Nullable
    @Override
    public DeleteProcessor<ProjectConfigDto> getDeleteProcessor() {
        return resourceProcessor;
    }

    @Nullable
    @Override
    public RenameProcessor<ProjectConfigDto> getRenameProcessor() {
        return resourceProcessor;
    }

    @NotNull
    @Override
    public String getStorablePath() {
        if (getParent() == null || !(getParent() instanceof HasStorablePath)) {
            return getData().getPath();
        }

        return ((HasStorablePath)getParent()).getStorablePath() + "/" + getData().getName();
    }

    @Override
    public boolean supportGoInto() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HasStorablePath)) return false;

        HasStorablePath that = (HasStorablePath)o;

        if (!getStorablePath().equals(that.getStorablePath())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getStorablePath().hashCode();
    }
}
