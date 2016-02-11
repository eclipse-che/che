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
package org.eclipse.che.api.vfs.gwt.client;

import com.google.gwt.http.client.RequestBuilder;
import com.google.inject.name.Named;

import org.eclipse.che.api.vfs.shared.dto.Item;
import org.eclipse.che.api.vfs.shared.dto.ReplacementSet;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.AsyncRequestLoader;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.List;

import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;

/**
 * Implementation for {@link VfsServiceClient}.
 *
 * @author Sergii Leschenko
 * @author Artem Zatsarynnyi
 */
public class VfsServiceClientImpl implements VfsServiceClient {
    private final AsyncRequestLoader  loader;
    private final AsyncRequestFactory asyncRequestFactory;
    private final String              extPath;

    @Inject
    public VfsServiceClientImpl(@Named("cheExtensionPath") String extPath,
                                LoaderFactory loaderFactory,
                                AsyncRequestFactory asyncRequestFactory) {
        this.loader = loaderFactory.newLoader();
        this.extPath = extPath;
        this.asyncRequestFactory = asyncRequestFactory;
    }

    @Override
    public void replace(@NotNull String workspaceId,
                        @NotNull String projectPath,
                        List<ReplacementSet> replacementSets,
                        AsyncRequestCallback<Void> callback) {
        String path = extPath + "/vfs/" + workspaceId + "/v2/replace" + normalizePath(projectPath);

        asyncRequestFactory.createRequest(RequestBuilder.POST, path, replacementSets, false)
                           .header(CONTENT_TYPE, APPLICATION_JSON)
                           .loader(loader)
                           .send(callback);
    }

    @Override
    public void getItemByPath(@NotNull String workspaceId, @NotNull String path, AsyncRequestCallback<Item> callback) {
        final String url = extPath + "/vfs/" + workspaceId + "/v2/itembypath" + normalizePath(path);

        asyncRequestFactory.createGetRequest(url)
                           .header(ACCEPT, APPLICATION_JSON)
                           .loader(loader)
                           .send(callback);
    }

    /**
     * Normalizes the path by adding a leading '/' if it doesn't exist.
     *
     * @param path
     *         path to normalize
     * @return normalized path
     */
    private String normalizePath(String path) {
        return path.startsWith("/") ? path : '/' + path;
    }
}
