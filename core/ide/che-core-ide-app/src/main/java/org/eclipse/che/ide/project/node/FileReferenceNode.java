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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.project.node.HasAction;
import org.eclipse.che.ide.api.project.node.HasProjectConfig;
import org.eclipse.che.ide.api.project.node.settings.NodeSettings;
import org.eclipse.che.ide.api.project.tree.VirtualFile;
import org.eclipse.che.ide.project.node.icon.NodeIconProvider;
import org.eclipse.che.ide.project.node.resource.ItemReferenceProcessor;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;

import static org.eclipse.che.api.promises.client.callback.PromiseHelper.newCallback;
import static org.eclipse.che.api.promises.client.callback.PromiseHelper.newPromise;

/**
 * @author Vlad Zhukovskiy
 */
public class FileReferenceNode extends ItemReferenceBasedNode implements VirtualFile, HasAction {

    public static final String GET_CONTENT_REL = "get content";

    /**
     * If you want to display another name different from origin, just set into attributes of this node this parameter.
     */
    public static final String DISPLAY_NAME_ATTR = "display";

    private final AppContext appContext;

    @Inject
    public FileReferenceNode(@Assisted ItemReference itemReference,
                             @Assisted ProjectConfigDto projectConfig,
                             @Assisted NodeSettings nodeSettings,
                             EventBus eventBus,
                             AppContext appContext,
                             NodeManager nodeManager,
                             ItemReferenceProcessor resourceProcessor) {
        super(itemReference, projectConfig, nodeSettings, eventBus, nodeManager, resourceProcessor);
        this.appContext = appContext;
    }

    @Override
    public void updatePresentation(@NotNull NodePresentation presentation) {
        presentation.setPresentableText(getData().getName());

        SVGResource icon = null;

        for (NodeIconProvider iconProvider : nodeManager.getNodeIconProvider()) {
            icon = iconProvider.getIcon(getData().getName());

            if (icon != null) {
                break;
            }
        }

        presentation.setPresentableIcon(icon != null ? icon : nodeManager.getNodesResources().file());
    }

    @NotNull
    @Override
    public String getPath() {
        return getStorablePath();
    }

    @Override
    public String getDisplayName() {
        if (getAttributes().containsKey(DISPLAY_NAME_ATTR)) {
            return getAttributes().get(DISPLAY_NAME_ATTR).get(0);
        }

        return getData().getName();
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Nullable
    @Override
    public HasProjectConfig getProject() {
        return this;
    }

    @Override
    public String getContentUrl() {
        Link link = getData().getLink(GET_CONTENT_REL);

        return link == null ? null : link.getHref();
    }

    @Override
    public Promise<Void> updateContent(final String content) {
        return newPromise(new AsyncPromiseHelper.RequestCall<Void>() {
            @Override
            public void makeCall(AsyncCallback<Void> callback) {
                nodeManager.projectService.updateFile(appContext.getDevMachine(), getStorablePath(), content, newCallback(callback));
            }
        });
    }

    @Override
    public void actionPerformed() {
        eventBus.fireEvent(new FileEvent(this, FileEvent.FileOperation.OPEN));
    }

    @Override
    public Promise<String> getContent() {
        return newPromise(new AsyncPromiseHelper.RequestCall<String>() {
            @Override
            public void makeCall(AsyncCallback<String> callback) {
                nodeManager.projectService.getFileContent(appContext.getDevMachine(), getStorablePath(), newCallback(callback, new StringUnmarshaller()));
            }
        });
    }
}
