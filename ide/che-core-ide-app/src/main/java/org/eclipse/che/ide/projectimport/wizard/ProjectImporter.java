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
package org.eclipse.che.ide.projectimport.wizard;

import com.google.common.base.Strings;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.project.ProjectServiceClient;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.project.CreateProjectEvent;
import org.eclipse.che.ide.api.importer.AbstractImporter;
import org.eclipse.che.ide.api.oauth.OAuth2Authenticator;
import org.eclipse.che.ide.api.oauth.OAuth2AuthenticatorRegistry;
import org.eclipse.che.ide.api.oauth.OAuth2AuthenticatorUrlProvider;
import org.eclipse.che.ide.api.project.wizard.ImportProjectNotificationSubscriberFactory;
import org.eclipse.che.ide.api.project.wizard.ProjectNotificationSubscriber;
import org.eclipse.che.ide.api.wizard.Wizard.CompleteCallback;
import org.eclipse.che.ide.rest.RestContext;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.util.ExceptionUtils;
import org.eclipse.che.security.oauth.OAuthStatus;

import javax.validation.constraints.NotNull;
import java.util.Map;

import static org.eclipse.che.api.core.ErrorCodes.UNABLE_GET_PRIVATE_SSH_KEY;
import static org.eclipse.che.api.core.ErrorCodes.UNAUTHORIZED_GIT_OPERATION;
import static org.eclipse.che.api.git.shared.ProviderInfo.AUTHENTICATE_URL;
import static org.eclipse.che.api.git.shared.ProviderInfo.PROVIDER_NAME;

/**
 * @author Dmitry Shnurenko
 */
@Singleton
public class ProjectImporter extends AbstractImporter {

    private final CoreLocalizationConstant localizationConstant;
    private final EventBus                 eventBus;
    private final AppContext               appContext;
    private final ProjectResolver projectResolver;
    private final DialogFactory               dialogFactory;
    private final String                      restContext;
    private final OAuth2AuthenticatorRegistry oAuth2AuthenticatorRegistry;

    private ProjectConfigDto projectConfig;
    private CompleteCallback callback;

    @Inject
    public ProjectImporter(ProjectServiceClient projectService,
                           CoreLocalizationConstant localizationConstant,
                           ImportProjectNotificationSubscriberFactory subscriberFactory,
                           AppContext appContext,
                           ProjectResolver projectResolver,
                           DialogFactory dialogFactory,
                           @RestContext String restContext,
                           OAuth2AuthenticatorRegistry oAuth2AuthenticatorRegistry,
                           EventBus eventBus) {
        super(appContext, projectService, subscriberFactory);
        this.localizationConstant = localizationConstant;
        this.appContext = appContext;
        this.projectResolver = projectResolver;
        this.dialogFactory = dialogFactory;
        this.restContext = restContext;
        this.oAuth2AuthenticatorRegistry = oAuth2AuthenticatorRegistry;
        this.eventBus = eventBus;
    }

    public void importProject(CompleteCallback callback, ProjectConfigDto projectConfig) {
        this.projectConfig = projectConfig;
        this.callback = callback;
        final String projectName = projectConfig.getName();
        final String pathToProject = projectConfig.getPath();
        String path = pathToProject == null ? projectName : pathToProject;
        startImport(path, projectName, projectConfig.getSource());
    }

    @Override
    protected Promise<Void> importProject(@NotNull final String pathToProject,
                                          @NotNull final String projectName,
                                          @NotNull final SourceStorageDto sourceStorage) {
        return doImport(pathToProject, projectName, sourceStorage);
    }


    private Promise<Void> doImport(@NotNull final String pathToProject,
                                   @NotNull final String projectName,
                                   @NotNull final SourceStorageDto sourceStorage) {
        final ProjectNotificationSubscriber subscriber = subscriberFactory.createSubscriber();
        subscriber.subscribe(projectName);
        Promise<Void> importPromise = projectService.importProject(appContext.getDevMachine(), pathToProject, false, sourceStorage);

        importPromise.then(new Operation<Void>() {
            @Override
            public void apply(Void arg) throws OperationException {
                eventBus.fireEvent(new CreateProjectEvent(projectConfig));
                projectResolver.resolveProject(callback, projectConfig);
                subscriber.onSuccess();
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError exception) throws OperationException {
                int errorCode = ExceptionUtils.getErrorCode(exception.getCause());
                // no ssh key found code. See org.eclipse.che.git.impl.nativegit.ssh.SshKeyProviderImpl.
                if (errorCode == UNABLE_GET_PRIVATE_SSH_KEY) {
                    subscriber.onFailure(exception.getCause().getMessage());
                    callback.onFailure(new Exception(localizationConstant.importProjectMessageUnableGetSshKey()));
                    return;
                }
                if (errorCode == UNAUTHORIZED_GIT_OPERATION) {
                    subscriber.onFailure(exception.getCause().getMessage());
                    final Map<String, String> attributes = ExceptionUtils.getAttributes(exception.getCause());
                    final String providerName = attributes.get(PROVIDER_NAME);
                    final String authenticateUrl = attributes.get(AUTHENTICATE_URL);
                    final boolean authenticated = Boolean.parseBoolean(attributes.get("authenticated"));
                    if (!Strings.isNullOrEmpty(providerName) && !Strings.isNullOrEmpty(authenticateUrl)) {
                        if (!authenticated) {
                            tryAuthenticateRepeatImport(providerName,
                                                        authenticateUrl,
                                                        pathToProject,
                                                        projectName,
                                                        sourceStorage,
                                                        subscriber);
                        } else {
                          dialogFactory.createMessageDialog(localizationConstant.importProjectSshKeyUploadFailedTitle(),
                                                            localizationConstant.importProjectSshKeyUploadFailedText(), null).show();
                        }
                    } else {
                        dialogFactory.createMessageDialog(localizationConstant.oauthFailedToGetAuthenticatorTitle(),
                                                          localizationConstant.oauthFailedToGetAuthenticatorText(), null).show();
                    }
                } else {
                    subscriber.onFailure(exception.getMessage());
                    callback.onFailure(new Exception(exception.getCause().getMessage()));
                }
            }
        });

        return importPromise;
    }

    private void tryAuthenticateRepeatImport(@NotNull final String providerName,
                                             @NotNull final String authenticateUrl,
                                             @NotNull final String pathToProject,
                                             @NotNull final String projectName,
                                             @NotNull final SourceStorageDto sourceStorage,
                                             @NotNull final ProjectNotificationSubscriber subscriber) {
        OAuth2Authenticator authenticator = oAuth2AuthenticatorRegistry.getAuthenticator(providerName);
        if (authenticator == null) {
            authenticator = oAuth2AuthenticatorRegistry.getAuthenticator("default");
        }
        authenticator.authenticate(OAuth2AuthenticatorUrlProvider.get(restContext, authenticateUrl),
                                   new AsyncCallback<OAuthStatus>() {
                                       @Override
                                       public void onFailure(Throwable caught) {
                                           callback.onFailure(new Exception(caught.getMessage()));
                                       }

                                       @Override
                                       public void onSuccess(OAuthStatus result) {
                                           if (!result.equals(OAuthStatus.NOT_PERFORMED)) {
                                               doImport(pathToProject, projectName, sourceStorage);
                                           } else {
                                               subscriber.onFailure("Authentication cancelled");
                                               callback.onCompleted();
                                           }
                                       }
                                   });

    }

}
