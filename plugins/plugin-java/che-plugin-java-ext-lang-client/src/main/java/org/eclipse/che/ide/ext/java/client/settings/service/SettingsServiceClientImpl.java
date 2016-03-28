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

import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.dto.JsonSerializable;
import org.eclipse.che.ide.json.JsonHelper;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.StringMapUnmarshaller;

import javax.validation.constraints.NotNull;
import java.util.Map;

import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;

/**
 * @author Dmitry Shnurenko
 */
public class SettingsServiceClientImpl implements SettingsServiceClient {

    private final String              extPath;
    private final AsyncRequestFactory asyncRequestFactory;
    private final String              workspaceId;

    @Inject
    public SettingsServiceClientImpl(AppContext appContext,
                                     AsyncRequestFactory asyncRequestFactory,
                                     @Named("cheExtensionPath") String extPath) {
        this.extPath = extPath;
        this.workspaceId = appContext.getWorkspace().getId();
        this.asyncRequestFactory = asyncRequestFactory;
    }

    /** {@inheritDoc} */
    @Override
    public Promise<Void> applyCompileParameters(@NotNull final Map<String, String> parameters) {
        String url = extPath + "/jdt/" + workspaceId + "/compiler-settings/set";

        JsonSerializable data = new JsonSerializable() {
            @Override
            public String toJson() {
                return JsonHelper.toJson(parameters);
            }
        };

        return asyncRequestFactory.createPostRequest(url, data)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .header(CONTENT_TYPE, APPLICATION_JSON)
                                  .send();
    }

    /** {@inheritDoc} */
    @Override
    public Promise<Map<String, String>> getCompileParameters() {
        String url = extPath + "/jdt/" + workspaceId + "/compiler-settings/all";

        return asyncRequestFactory.createGetRequest(url)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .send(new StringMapUnmarshaller());
    }
}
