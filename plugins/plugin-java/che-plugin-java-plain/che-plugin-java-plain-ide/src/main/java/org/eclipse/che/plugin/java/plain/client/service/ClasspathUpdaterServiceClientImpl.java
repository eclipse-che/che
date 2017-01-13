/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.java.plain.client.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.ext.java.shared.dto.classpath.ClasspathEntryDto;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.MessageLoader;

import java.util.List;

/**
 * The implementation of {@link ClasspathUpdaterServiceClient}.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class ClasspathUpdaterServiceClientImpl implements ClasspathUpdaterServiceClient {

    private final String              pathToService;
    private final MessageLoader       loader;
    private final AsyncRequestFactory asyncRequestFactory;
    private final AppContext          appContext;

    @Inject
    public ClasspathUpdaterServiceClientImpl(AsyncRequestFactory asyncRequestFactory,
                                             AppContext appContext,
                                             LoaderFactory loaderFactory) {
        this.asyncRequestFactory = asyncRequestFactory;
        this.appContext = appContext;
        this.loader = loaderFactory.newLoader();

        this.pathToService = "/jdt/classpath/";
    }

    @Override
    public Promise<Void> setRawClasspath(String projectPath, List<ClasspathEntryDto> entries) {
        final String url = appContext.getDevMachine().getWsAgentBaseUrl() + pathToService + "update?projectpath=" + projectPath;
        return asyncRequestFactory.createPostRequest(url, entries)
                                  .loader(loader)
                                  .send();
    }
}
