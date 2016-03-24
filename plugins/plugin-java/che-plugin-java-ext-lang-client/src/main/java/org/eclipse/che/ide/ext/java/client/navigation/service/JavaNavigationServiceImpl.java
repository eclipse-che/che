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
package org.eclipse.che.ide.ext.java.client.navigation.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.ext.java.shared.Jar;
import org.eclipse.che.ide.ext.java.shared.JarEntry;
import org.eclipse.che.ide.ext.java.shared.OpenDeclarationDescriptor;
import org.eclipse.che.ide.ext.java.shared.dto.ImplementationsDescriptorDTO;
import org.eclipse.che.ide.ext.java.shared.dto.model.CompilationUnit;
import org.eclipse.che.ide.ext.java.shared.dto.model.JavaProject;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;

import java.util.List;

import static org.eclipse.che.api.promises.client.callback.PromiseHelper.newCallback;
import static org.eclipse.che.api.promises.client.callback.PromiseHelper.newPromise;
import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class JavaNavigationServiceImpl implements JavaNavigationService {

    private final String                 restContext;
    private final LoaderFactory          loaderFactory;
    private final AsyncRequestFactory    requestFactory;
    private final String                 workspaceId;
    private final DtoUnmarshallerFactory unmarshallerFactory;

    @Inject
    public JavaNavigationServiceImpl(@Named("cheExtensionPath") String restContext,
                                     AppContext appContext,
                                     LoaderFactory loaderFactory,
                                     DtoUnmarshallerFactory unmarshallerFactory,
                                     AsyncRequestFactory asyncRequestFactory) {
        this.restContext = restContext;
        this.loaderFactory = loaderFactory;
        this.requestFactory = asyncRequestFactory;
        this.workspaceId = appContext.getWorkspace().getId();
        this.unmarshallerFactory = unmarshallerFactory;
    }

    @Override
    public void findDeclaration(String projectPath, String fqn, int offset, AsyncRequestCallback<OpenDeclarationDescriptor> callback) {
        String url = restContext + "/jdt/" + workspaceId + "/navigation/find-declaration?projectpath=" + projectPath + "&fqn=" + fqn +
                     "&offset=" + offset;
        requestFactory.createGetRequest(url).send(callback);
    }

    public void getExternalLibraries(String projectPath, AsyncRequestCallback<List<Jar>> callback) {
        String url = restContext + "/jdt/" + workspaceId + "/navigation/libraries?projectpath=" + projectPath;
        requestFactory.createGetRequest(url).send(callback);
    }

    @Override
    public void getLibraryChildren(String projectPath, int libId, AsyncRequestCallback<List<JarEntry>> callback) {
        String url = restContext + "/jdt/" + workspaceId + "/navigation/lib/children?projectpath=" + projectPath + "&root=" + libId;
        requestFactory.createGetRequest(url).send(callback);
    }

    @Override
    public void getChildren(String projectPath, int libId, String path, AsyncRequestCallback<List<JarEntry>> callback) {
        String url = restContext + "/jdt/" + workspaceId + "/navigation/children?projectpath=" + projectPath + "&root=" + libId + "&path=" +
                     path;
        requestFactory.createGetRequest(url).send(callback);
    }

    @Override
    public void getEntry(String projectPath, int libId, String path, AsyncRequestCallback<JarEntry> callback) {
        String url =
                restContext + "/jdt/" + workspaceId + "/navigation/entry?projectpath=" + projectPath + "&root=" + libId + "&path=" + path;
        requestFactory.createGetRequest(url).send(callback);
    }

    @Override
    public void getContent(String projectPath, int libId, String path, AsyncRequestCallback<String> callback) {
        String url = getContentUrl(projectPath, libId, path);

        requestFactory.createGetRequest(url).send(callback);
    }

    @Override
    public void getContent(String projectPath, String fqn, AsyncRequestCallback<String> callback) {
        String url = restContext + "/jdt/" + workspaceId + "/navigation/contentbyfqn?projectpath=" + projectPath + "&fqn=" + fqn;
        requestFactory.createGetRequest(url).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public Promise<CompilationUnit> getCompilationUnit(String projectPath, String fqn, boolean showInherited) {
        final String url = restContext + "/jdt/" + workspaceId + "/navigation/compilation-unit?projectpath=" + projectPath + "&fqn=" + fqn +
                           "&showinherited=" + showInherited;

        return newPromise(new AsyncPromiseHelper.RequestCall<CompilationUnit>() {
            @Override
            public void makeCall(AsyncCallback<CompilationUnit> callback) {
                requestFactory.createGetRequest(url)
                              .header(ACCEPT, APPLICATION_JSON)
                              .send(newCallback(callback, unmarshallerFactory.newUnmarshaller(CompilationUnit.class)));
            }
        });
    }

    @Override
    public Promise<ImplementationsDescriptorDTO> getImplementations(String projectPath, String fqn, int offset) {
        final String url = restContext + "/jdt/" + workspaceId + "/navigation/implementations?projectpath=" + projectPath + "&fqn=" + fqn +
                           "&offset=" + offset;

        return requestFactory.createGetRequest(url)
                             .header(ACCEPT, APPLICATION_JSON)
                             .loader(loaderFactory.newLoader())
                             .send(unmarshallerFactory.newUnmarshaller(ImplementationsDescriptorDTO.class));
    }

    @Override
    public Promise<List<JavaProject>> getProjectsAndPackages(boolean includePackage) {
        final String url = restContext + "/jdt/" + workspaceId +
                           "/navigation/get/projects/and/packages"
                           + "?includepackages=" + includePackage;

        return newPromise(new AsyncPromiseHelper.RequestCall<List<JavaProject>>() {
            @Override
            public void makeCall(AsyncCallback<List<JavaProject>> callback) {

                requestFactory.createGetRequest(url)
                              .header(ACCEPT, APPLICATION_JSON)
                              .loader(loaderFactory.newLoader())
                              .send(newCallback(callback, unmarshallerFactory.newListUnmarshaller(JavaProject.class)));
            }
        });
    }

    @Override
    public String getContentUrl(String projectPath, int libId, String path) {
        return restContext + "/jdt/" + workspaceId + "/navigation/content?projectpath=" + projectPath + "&root=" + libId + "&path=" + path;
    }
}
