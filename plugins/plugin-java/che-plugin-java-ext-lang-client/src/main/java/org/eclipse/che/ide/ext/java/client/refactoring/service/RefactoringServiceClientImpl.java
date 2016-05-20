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
package org.eclipse.che.ide.ext.java.client.refactoring.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangeCreationResult;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangeEnabledState;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangePreview;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.CreateMoveRefactoring;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.CreateRenameRefactoring;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.LinkedRenameRefactoringApply;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.MoveSettings;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringChange;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringPreview;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringResult;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringSession;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RenameRefactoringSession;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RenameSettings;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ReorgDestination;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ValidateNewName;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.MessageLoader;

import static org.eclipse.che.api.promises.client.callback.PromiseHelper.newCallback;
import static org.eclipse.che.api.promises.client.callback.PromiseHelper.newPromise;
import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.MimeType.TEXT_PLAIN;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;

/**
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
@Singleton
final class RefactoringServiceClientImpl implements RefactoringServiceClient {

    private final AsyncRequestFactory    asyncRequestFactory;
    private final DtoUnmarshallerFactory unmarshallerFactory;
    private final AppContext             appContext;
    private final String                 pathToService;
    private final MessageLoader          loader;

    @Inject
    public RefactoringServiceClientImpl(AsyncRequestFactory asyncRequestFactory,
                                        DtoUnmarshallerFactory unmarshallerFactory,
                                        AppContext appContext,
                                        LoaderFactory loaderFactory) {
        this.asyncRequestFactory = asyncRequestFactory;
        this.unmarshallerFactory = unmarshallerFactory;
        this.appContext = appContext;
        this.loader = loaderFactory.newLoader();
        this.pathToService = "/java/refactoring/";
    }

    /** {@inheritDoc} */
    @Override
    public Promise<String> createMoveRefactoring(final CreateMoveRefactoring moveRefactoring) {
        return newPromise(new AsyncPromiseHelper.RequestCall<String>() {
            @Override
            public void makeCall(AsyncCallback<String> callback) {

                asyncRequestFactory
                        .createPostRequest(appContext.getDevMachine().getWsAgentBaseUrl() + pathToService + "move/create", moveRefactoring)
                        .header(ACCEPT, TEXT_PLAIN)
                        .header(CONTENT_TYPE, APPLICATION_JSON)
                        .loader(loader)
                        .send(newCallback(callback, new StringUnmarshaller()));
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public Promise<RenameRefactoringSession> createRenameRefactoring(final CreateRenameRefactoring settings) {
        final String url = appContext.getDevMachine().getWsAgentBaseUrl() + pathToService + "rename/create";
        return newPromise(new AsyncPromiseHelper.RequestCall<RenameRefactoringSession>() {
            @Override
            public void makeCall(AsyncCallback<RenameRefactoringSession> callback) {
                asyncRequestFactory.createPostRequest(url, settings)
                                   .header(ACCEPT, APPLICATION_JSON)
                                   .header(CONTENT_TYPE, APPLICATION_JSON)
                                   .loader(loader)
                                   .send(newCallback(callback, unmarshallerFactory.newUnmarshaller(RenameRefactoringSession.class)));
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public Promise<RefactoringResult> applyLinkedModeRename(final LinkedRenameRefactoringApply refactoringApply) {
        final String url = appContext.getDevMachine().getWsAgentBaseUrl() + pathToService + "rename/linked/apply";
        return newPromise(new AsyncPromiseHelper.RequestCall<RefactoringResult>() {
            @Override
            public void makeCall(AsyncCallback<RefactoringResult> callback) {
                asyncRequestFactory.createPostRequest(url, refactoringApply)
                                   .header(ACCEPT, APPLICATION_JSON)
                                   .header(CONTENT_TYPE, APPLICATION_JSON)
                                   .loader(loader)
                                   .send(newCallback(callback, unmarshallerFactory.newUnmarshaller(RefactoringResult.class)));
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public Promise<RefactoringStatus> setDestination(final ReorgDestination destination) {
        final String url = appContext.getDevMachine().getWsAgentBaseUrl() + pathToService + "set/destination";

        return newPromise(new AsyncPromiseHelper.RequestCall<RefactoringStatus>() {
            @Override
            public void makeCall(AsyncCallback<RefactoringStatus> callback) {

                asyncRequestFactory.createPostRequest(url, destination)
                                   .header(ACCEPT, APPLICATION_JSON)
                                   .header(CONTENT_TYPE, APPLICATION_JSON)
                                   .loader(loader)
                                   .send(newCallback(callback, unmarshallerFactory.newUnmarshaller(RefactoringStatus.class)));
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public Promise<Void> setMoveSettings(final MoveSettings settings) {
        final String url = appContext.getDevMachine().getWsAgentBaseUrl() + pathToService + "set/move/setting";

        return newPromise(new AsyncPromiseHelper.RequestCall<Void>() {
            @Override
            public void makeCall(AsyncCallback<Void> callback) {

                asyncRequestFactory.createPostRequest(url, settings)
                                   .header(ACCEPT, APPLICATION_JSON)
                                   .header(CONTENT_TYPE, APPLICATION_JSON)
                                   .loader(loader)
                                   .send(newCallback(callback));
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public Promise<ChangeCreationResult> createChange(final RefactoringSession session) {
        final String url = appContext.getDevMachine().getWsAgentBaseUrl() + pathToService + "create/change";

        return newPromise(new AsyncPromiseHelper.RequestCall<ChangeCreationResult>() {
            @Override
            public void makeCall(AsyncCallback<ChangeCreationResult> callback) {

                asyncRequestFactory.createPostRequest(url, session)
                                   .header(ACCEPT, APPLICATION_JSON)
                                   .header(CONTENT_TYPE, APPLICATION_JSON)
                                   .loader(loader)
                                   .send(newCallback(callback, unmarshallerFactory.newUnmarshaller(ChangeCreationResult.class)));
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public Promise<RefactoringPreview> getRefactoringPreview(final RefactoringSession session) {
        final String url = appContext.getDevMachine().getWsAgentBaseUrl() + pathToService + "get/preview";

        return newPromise(new AsyncPromiseHelper.RequestCall<RefactoringPreview>() {
            @Override
            public void makeCall(AsyncCallback<RefactoringPreview> callback) {

                asyncRequestFactory.createPostRequest(url, session)
                                   .header(ACCEPT, APPLICATION_JSON)
                                   .header(CONTENT_TYPE, APPLICATION_JSON)
                                   .loader(loader)
                                   .send(newCallback(callback, unmarshallerFactory.newUnmarshaller(RefactoringPreview.class)));
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public Promise<RefactoringResult> applyRefactoring(final RefactoringSession session) {
        final String url = appContext.getDevMachine().getWsAgentBaseUrl() + pathToService + "apply";

        return newPromise(new AsyncPromiseHelper.RequestCall<RefactoringResult>() {
            @Override
            public void makeCall(AsyncCallback<RefactoringResult> callback) {

                asyncRequestFactory.createPostRequest(url, session)
                                   .header(ACCEPT, APPLICATION_JSON)
                                   .header(CONTENT_TYPE, APPLICATION_JSON)
                                   .loader(loader)
                                   .send(newCallback(callback, unmarshallerFactory.newUnmarshaller(RefactoringResult.class)));
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public Promise<Void> changeChangeEnabledState(final ChangeEnabledState state) {
        final String url = appContext.getDevMachine().getWsAgentBaseUrl() + pathToService + "change/enabled";

        return newPromise(new AsyncPromiseHelper.RequestCall<Void>() {
            @Override
            public void makeCall(AsyncCallback<Void> callback) {

                asyncRequestFactory.createPostRequest(url, state)
                                   .header(ACCEPT, APPLICATION_JSON)
                                   .header(CONTENT_TYPE, APPLICATION_JSON)
                                   .loader(loader)
                                   .send(newCallback(callback));
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public Promise<ChangePreview> getChangePreview(final RefactoringChange change) {
        final String url = appContext.getDevMachine().getWsAgentBaseUrl() + pathToService + "change/preview";

        return newPromise(new AsyncPromiseHelper.RequestCall<ChangePreview>() {
            @Override
            public void makeCall(AsyncCallback<ChangePreview> callback) {

                asyncRequestFactory.createPostRequest(url, change)
                                   .header(ACCEPT, APPLICATION_JSON)
                                   .header(CONTENT_TYPE, APPLICATION_JSON)
                                   .loader(loader)
                                   .send(newCallback(callback, unmarshallerFactory.newUnmarshaller(ChangePreview.class)));
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public Promise<RefactoringStatus> validateNewName(final ValidateNewName newName) {
        final String url = appContext.getDevMachine().getWsAgentBaseUrl() + pathToService + "rename/validate/name";

        return newPromise(new AsyncPromiseHelper.RequestCall<RefactoringStatus>() {
            @Override
            public void makeCall(AsyncCallback<RefactoringStatus> callback) {

                asyncRequestFactory.createPostRequest(url, newName)
                                   .header(ACCEPT, APPLICATION_JSON)
                                   .header(CONTENT_TYPE, APPLICATION_JSON)
                                   .loader(loader)
                                   .send(newCallback(callback, unmarshallerFactory.newUnmarshaller(RefactoringStatus.class)));
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public Promise<Void> setRenameSettings(final RenameSettings settings) {
        final String url = appContext.getDevMachine().getWsAgentBaseUrl() + pathToService + "set/rename/settings";

        return newPromise(new AsyncPromiseHelper.RequestCall<Void>() {
            @Override
            public void makeCall(AsyncCallback<Void> callback) {

                asyncRequestFactory.createPostRequest(url, settings)
                                   .header(ACCEPT, APPLICATION_JSON)
                                   .header(CONTENT_TYPE, APPLICATION_JSON)
                                   .loader(loader)
                                   .send(newCallback(callback));
            }
        });
    }

    @Override
    public Promise<Void> reindexProject(String projectPath) {
        final String url = appContext.getDevMachine().getWsAgentBaseUrl() + pathToService + "reindex?projectpath=" + projectPath;

        return asyncRequestFactory.createGetRequest(url)
                                  .loader(loader)
                                  .send();
    }
}
