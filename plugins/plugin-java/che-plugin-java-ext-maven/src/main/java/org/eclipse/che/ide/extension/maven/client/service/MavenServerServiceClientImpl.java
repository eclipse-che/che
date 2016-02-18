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
package org.eclipse.che.ide.extension.maven.client.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;

/**
 * Implementation for {@link MavenServerServiceClient}.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class MavenServerServiceClientImpl implements MavenServerServiceClient {
    private final String              servicePath;
    private final LoaderFactory       loaderFactory;
    private final AsyncRequestFactory asyncRequestFactory;


    @Inject
    public MavenServerServiceClientImpl(@Named("cheExtensionPath") String extPath,
                                        AppContext appContext,
                                        LoaderFactory loaderFactory,
                                        AsyncRequestFactory asyncRequestFactory) {
        this.loaderFactory = loaderFactory;
        this.asyncRequestFactory = asyncRequestFactory;
        this.servicePath = extPath + "/maven/" + appContext.getWorkspace().getId() + "/server/";
    }

    @Override
    public Promise<String> getEffectivePom(String projectPath) {
        final String url = servicePath + "effective/pom?projectpath=" + projectPath;

        return asyncRequestFactory.createGetRequest(url)
                                  .loader(loaderFactory.newLoader("Generating effective pom..."))
                                  .send(new StringUnmarshaller());
    }
}
