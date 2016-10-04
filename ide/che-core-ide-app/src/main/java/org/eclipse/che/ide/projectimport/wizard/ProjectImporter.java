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

import org.eclipse.che.api.core.model.project.SourceStorage;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.RequestCall;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.importer.AbstractImporter;
import org.eclipse.che.ide.api.oauth.OAuth2Authenticator;
import org.eclipse.che.ide.api.oauth.OAuth2AuthenticatorRegistry;
import org.eclipse.che.ide.api.oauth.OAuth2AuthenticatorUrlProvider;
import org.eclipse.che.ide.api.subversion.Credentials;
import org.eclipse.che.ide.api.subversion.SubversionCredentialsDialog;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.project.wizard.ImportProjectNotificationSubscriberFactory;
import org.eclipse.che.ide.api.project.wizard.ProjectNotificationSubscriber;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.wizard.Wizard.CompleteCallback;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.rest.RestContext;
import org.eclipse.che.ide.util.ExceptionUtils;
import org.eclipse.che.security.oauth.OAuthStatus;

import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.api.core.ErrorCodes.UNABLE_GET_PRIVATE_SSH_KEY;
import static org.eclipse.che.api.core.ErrorCodes.UNAUTHORIZED_GIT_OPERATION;
import static org.eclipse.che.api.core.ErrorCodes.UNAUTHORIZED_SVN_OPERATION;
import static org.eclipse.che.api.git.shared.ProviderInfo.AUTHENTICATE_URL;
import static org.eclipse.che.api.git.shared.ProviderInfo.PROVIDER_NAME;
import static org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.createFromAsyncRequest;
import static org.eclipse.che.ide.util.ExceptionUtils.getErrorCode;

/**
 * @author Dmitry Shnurenko
 * @author Vlad Zhukovskyi
 */
@Singleton
public class ProjectImporter extends AbstractImporter {

    private final CoreLocalizationConstant    localizationConstant;
    private final ProjectResolver             projectResolver;
    private final String                      restContext;
    private final SubversionCredentialsDialog credentialsDialog;
    private final OAuth2AuthenticatorRegistry oAuth2AuthenticatorRegistry;


    @Inject
    public ProjectImporter(CoreLocalizationConstant localizationConstant,
                           ImportProjectNotificationSubscriberFactory subscriberFactory,
                           AppContext appContext,
                           ProjectResolver projectResolver,
                           @RestContext String restContext,
                           SubversionCredentialsDialog credentialsDialog,
                           OAuth2AuthenticatorRegistry oAuth2AuthenticatorRegistry) {
        super(appContext, subscriberFactory);
        this.localizationConstant = localizationConstant;
        this.projectResolver = projectResolver;
        this.restContext = restContext;
        this.credentialsDialog = credentialsDialog;
        this.oAuth2AuthenticatorRegistry = oAuth2AuthenticatorRegistry;
    }

    public void importProject(final CompleteCallback callback, MutableProjectConfig projectConfig) {

        final Path path = !isNullOrEmpty(projectConfig.getPath())
                          ? Path.valueOf(projectConfig.getPath())
                          : !isNullOrEmpty(projectConfig.getName()) ? Path.valueOf(projectConfig.getName()).makeAbsolute()
                                                                    : null;

        checkState(path != null, "Import path is undefined");

        startImport(path, projectConfig.getSource()).then(new Operation<Project>() {
            @Override
            public void apply(Project arg) throws OperationException {
                if (callback != null) {
                    callback.onCompleted();
                }
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                if (callback != null) {
                    callback.onFailure(arg.getCause());
                }
            }
        });
    }

    @Override
    protected Promise<Project> importProject(Path path, SourceStorage sourceStorage) {
        return doImport(path, sourceStorage);
    }

    private Promise<Project> doImport(final Path path, final SourceStorage sourceStorage) {
        final ProjectNotificationSubscriber subscriber = subscriberFactory.createSubscriber();
        subscriber.subscribe(path.lastSegment());

        MutableProjectConfig importConfig = new MutableProjectConfig();
        importConfig.setPath(path.toString());
        importConfig.setSource(sourceStorage);

        return appContext.getWorkspaceRoot()
                         .importProject()
                         .withBody(importConfig)
                         .send()
                         .thenPromise(new Function<Project, Promise<Project>>() {
                             @Override
                             public Promise<Project> apply(Project project) throws FunctionException {
                                 subscriber.onSuccess();

                                 return projectResolver.resolve(project);
                             }
                         })
                         .catchErrorPromise(new Function<PromiseError, Promise<Project>>() {
                             @Override
                             public Promise<Project> apply(PromiseError exception) throws FunctionException {
                                 subscriber.onFailure(exception.getCause().getMessage());

                                 switch (getErrorCode(exception.getCause())) {
                                     case UNABLE_GET_PRIVATE_SSH_KEY:
                                         throw new IllegalStateException(localizationConstant.importProjectMessageUnableGetSshKey());
                                     case UNAUTHORIZED_SVN_OPERATION:
                                         return recallImportWithCredentials(sourceStorage, path);
                                     case UNAUTHORIZED_GIT_OPERATION:
                                         final Map<String, String> attributes = ExceptionUtils.getAttributes(exception.getCause());
                                         final String providerName = attributes.get(PROVIDER_NAME);
                                         final String authenticateUrl = attributes.get(AUTHENTICATE_URL);
                                         if (!Strings.isNullOrEmpty(providerName) && !Strings.isNullOrEmpty(authenticateUrl)) {
                                             return authUserAndRecallImport(providerName,
                                                                            authenticateUrl,
                                                                            path,
                                                                            sourceStorage,
                                                                            subscriber);
                                         } else {
                                             throw new IllegalStateException(localizationConstant.oauthFailedToGetAuthenticatorText());
                                         }
                                     default:
                                         throw new IllegalStateException(exception.getCause());
                                 }
                             }
                         });
    }

    private Promise<Project> recallImportWithCredentials(final SourceStorage sourceStorage, final Path path) {
        return createFromAsyncRequest(new RequestCall<Project>() {
            @Override
            public void makeCall(final AsyncCallback<Project> callback) {
                credentialsDialog.askCredentials().then(new Operation<Credentials>() {
                    @Override
                    public void apply(Credentials credentials) throws OperationException {
                        sourceStorage.getParameters().put("username", credentials.getUsername());
                        sourceStorage.getParameters().put("password", credentials.getPassword());
                        doImport(path, sourceStorage).then(new Operation<Project>() {
                            @Override
                            public void apply(Project project) throws OperationException {
                                callback.onSuccess(project);
                            }
                        }).catchError(new Operation<PromiseError>() {
                            @Override
                            public void apply(PromiseError error) throws OperationException {
                                callback.onFailure(error.getCause());
                            }
                        });
                    }
                });
            }
        });
    }

    private Promise<Project> authUserAndRecallImport(final String providerName,
                                                     final String authenticateUrl,
                                                     final Path path,
                                                     final SourceStorage sourceStorage,
                                                     final ProjectNotificationSubscriber subscriber) {

        return createFromAsyncRequest(new RequestCall<Project>() {
            @Override
            public void makeCall(final AsyncCallback<Project> callback) {
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
                                                       doImport(path, sourceStorage).then(new Operation<Project>() {
                                                           @Override
                                                           public void apply(Project project) throws OperationException {
                                                               callback.onSuccess(project);
                                                           }
                                                       }).catchError(new Operation<PromiseError>() {
                                                           @Override
                                                           public void apply(PromiseError error) throws OperationException {
                                                               callback.onFailure(error.getCause());
                                                           }
                                                       });
                                                   } else {
                                                       subscriber.onFailure("Authentication cancelled");
                                                       callback.onFailure(new IllegalStateException("Authentication cancelled"));
                                                   }
                                               }
                                           });
            }
        });
    }

}
