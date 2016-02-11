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
package org.eclipse.che.ide.ext.java.client.settings.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.dto.JsonSerializable;
import org.eclipse.che.ide.json.JsonHelper;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.StringMapUnmarshaller;

import javax.validation.constraints.NotNull;
import java.util.Map;

import static org.eclipse.che.api.promises.client.callback.PromiseHelper.newCallback;
import static org.eclipse.che.api.promises.client.callback.PromiseHelper.newPromise;
import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;

/**
 * @author Dmitry Shnurenko
 */
public class SettingsServiceClientImpl implements SettingsServiceClient {

    private final String              extPath;
    private final AsyncRequestFactory asyncRequestFactory;
    private final AppContext          appContext;
    private final String              workspaceId;

    @Inject
    public SettingsServiceClientImpl(AppContext appContext,
                                     AsyncRequestFactory asyncRequestFactory,
                                     @Named("cheExtensionPath") String extPath) {
        this.appContext = appContext;
        this.extPath = extPath;
        this.workspaceId = appContext.getWorkspace().getId();
        this.asyncRequestFactory = asyncRequestFactory;
    }

    private String getPathToProject() {
        CurrentProject currentProject = appContext.getCurrentProject();

        if (currentProject == null) {
            return "";
        }

        return currentProject.getProjectConfig().getPath();
    }

    /** {@inheritDoc} */
    @Override
    public Promise<Void> applyCompileParameters(@NotNull final Map<String, String> parameters) {
        final String pathToProject = getPathToProject();

        return newPromise(new AsyncPromiseHelper.RequestCall<Void>() {
            @Override
            public void makeCall(AsyncCallback<Void> callback) {
                String url = extPath  + "/jdt/" + workspaceId + "/compiler-settings/set?projectpath=" + pathToProject;

                JsonSerializable data = new JsonSerializable() {
                    @Override
                    public String toJson() {
                        return JsonHelper.toJson(parameters);
                    }
                };

                asyncRequestFactory.createPostRequest(url, data)
                                   .header(ACCEPT, APPLICATION_JSON)
                                   .header(CONTENT_TYPE, APPLICATION_JSON)
                                   .send(newCallback(callback));
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public Promise<Map<String, String>> getCompileParameters() {
        final String pathToProject = getPathToProject();

        return newPromise(new AsyncPromiseHelper.RequestCall<Map<String, String>>() {
            @Override
            public void makeCall(AsyncCallback<Map<String, String>> callback) {
                String url = extPath  + "/jdt/" + workspaceId + "/compiler-settings/all?projectpath=" + pathToProject;

                asyncRequestFactory.createGetRequest(url)
                                   .header(ACCEPT, APPLICATION_JSON)
                                   .send(newCallback(callback, new StringMapUnmarshaller()));
            }
        });
    }
}
