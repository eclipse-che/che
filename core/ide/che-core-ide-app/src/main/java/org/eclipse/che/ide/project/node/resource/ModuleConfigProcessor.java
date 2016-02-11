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
package org.eclipse.che.ide.project.node.resource;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.api.promises.client.js.JsPromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.project.node.HasDataObject;
import org.eclipse.che.ide.api.project.node.HasProjectConfig;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.project.node.ModuleNode;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;

/**
 * @author Dmitry Shnurenko
 */
public class ModuleConfigProcessor extends AbstractResourceProcessor<ProjectConfigDto> {
    
    private final String workspaceId;

    @Inject
    public ModuleConfigProcessor(EventBus eventBus,
                                 ProjectServiceClient projectServiceClient,
                                 AppContext appContext,
                                 DtoUnmarshallerFactory unmarshallerFactory) {
        super(eventBus, projectServiceClient, unmarshallerFactory);
        
        workspaceId = appContext.getWorkspace().getId();
    }

    @Override
    public Promise<ProjectConfigDto> delete(final HasDataObject<ProjectConfigDto> node) {
        if (node instanceof ModuleNode) {
            Node parent = ((ModuleNode)node).getParent();
            if (!(parent instanceof HasProjectConfig)) {
                return Promises.reject(JsPromiseError.create("Failed to search parent project descriptor"));
            }

            final String parentPath = ((HasProjectConfig)parent).getProjectConfig().getPath();
            final String modulePath = node.getData().getPath();

            return AsyncPromiseHelper.createFromAsyncRequest(new AsyncPromiseHelper.RequestCall<ProjectConfigDto>() {
                @Override
                public void makeCall(final AsyncCallback<ProjectConfigDto> callback) {
                    projectService.deleteModule(workspaceId, parentPath, modulePath, new AsyncRequestCallback<Void>() {
                        @Override
                        protected void onSuccess(Void result) {
                            callback.onSuccess(node.getData());
                        }

                        @Override
                        protected void onFailure(Throwable exception) {
                            callback.onFailure(exception);
                        }
                    });
                }
            });
        }
        return Promises.reject(JsPromiseError.create("Internal error"));
    }

    @Override
    public Promise<ProjectConfigDto> rename(HasStorablePath parent,
                                            HasDataObject<ProjectConfigDto> node,
                                            String newName) {
        return Promises.reject(JsPromiseError.create(""));
    }
}
