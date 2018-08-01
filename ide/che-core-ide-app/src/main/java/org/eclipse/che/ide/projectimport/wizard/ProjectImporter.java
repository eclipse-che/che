/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.projectimport.wizard;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.api.core.ErrorCodes.UNABLE_GET_PRIVATE_SSH_KEY;
import static org.eclipse.che.api.core.ErrorCodes.UNAUTHORIZED_GIT_OPERATION;
import static org.eclipse.che.api.core.ErrorCodes.UNAUTHORIZED_SVN_OPERATION;
import static org.eclipse.che.api.git.shared.ProviderInfo.AUTHENTICATE_URL;
import static org.eclipse.che.api.git.shared.ProviderInfo.PROVIDER_NAME;
import static org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.createFromAsyncRequest;
import static org.eclipse.che.ide.util.ExceptionUtils.getErrorCode;

import com.google.common.base.Strings;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Map;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.api.core.model.workspace.config.SourceStorage;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.RequestCall;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.auth.OAuthServiceClient;
import org.eclipse.che.ide.api.oauth.OAuth2Authenticator;
import org.eclipse.che.ide.api.oauth.OAuth2AuthenticatorRegistry;
import org.eclipse.che.ide.api.oauth.OAuth2AuthenticatorUrlProvider;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.wizard.Wizard.CompleteCallback;
import org.eclipse.che.ide.projectimport.AbstractImporter;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.dialogs.askcredentials.AskCredentialsDialog;
import org.eclipse.che.ide.util.ExceptionUtils;
import org.eclipse.che.security.oauth.OAuthStatus;

/**
 * @author Dmitry Shnurenko
 * @author Vlad Zhukovskyi
 */
@Singleton
public class ProjectImporter extends AbstractImporter {

  private final CoreLocalizationConstant localizationConstant;
  private final ProjectResolver projectResolver;
  private final AskCredentialsDialog credentialsDialog;
  private final OAuth2AuthenticatorRegistry oAuth2AuthenticatorRegistry;
  private final OAuthServiceClient oAuthServiceClient;
  private final DtoUnmarshallerFactory unmarshallerFactory;

  @Inject
  public ProjectImporter(
      CoreLocalizationConstant localizationConstant,
      ImportProjectNotificationSubscriberFactory subscriberFactory,
      AppContext appContext,
      ProjectResolver projectResolver,
      AskCredentialsDialog credentialsDialog,
      OAuth2AuthenticatorRegistry oAuth2AuthenticatorRegistry,
      DtoUnmarshallerFactory unmarshaller,
      OAuthServiceClient oAuthServiceClient) {
    super(appContext, subscriberFactory);
    this.localizationConstant = localizationConstant;
    this.projectResolver = projectResolver;
    this.credentialsDialog = credentialsDialog;
    this.unmarshallerFactory = unmarshaller;
    this.oAuth2AuthenticatorRegistry = oAuth2AuthenticatorRegistry;
    this.oAuthServiceClient = oAuthServiceClient;
  }

  public void importProject(final CompleteCallback callback, MutableProjectConfig projectConfig) {

    final Path path =
        !isNullOrEmpty(projectConfig.getPath())
            ? Path.valueOf(projectConfig.getPath())
            : !isNullOrEmpty(projectConfig.getName())
                ? Path.valueOf(projectConfig.getName()).makeAbsolute()
                : null;

    checkState(path != null, "Import path is undefined");

    startImport(path, projectConfig.getSource())
        .then(
            project -> {
              if (callback != null) {
                callback.onCompleted();
              }
            })
        .catchError(
            error -> {
              if (callback != null) {
                callback.onFailure(error.getCause());
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

    return appContext
        .getWorkspaceRoot()
        .importProject()
        .withBody(importConfig)
        .send()
        .thenPromise(
            project -> {
              subscriber.onSuccess();
              return projectResolver.resolve(project);
            })
        .catchErrorPromise(
            new Function<PromiseError, Promise<Project>>() {
              @Override
              public Promise<Project> apply(PromiseError exception) throws FunctionException {
                subscriber.onFailure(exception.getCause().getMessage());

                switch (getErrorCode(exception.getCause())) {
                  case UNABLE_GET_PRIVATE_SSH_KEY:
                    throw new IllegalStateException(
                        localizationConstant.importProjectMessageUnableGetSshKey());
                  case UNAUTHORIZED_SVN_OPERATION:
                    return recallImportWithCredentials(sourceStorage, path);
                  case UNAUTHORIZED_GIT_OPERATION:
                    final Map<String, String> attributes =
                        ExceptionUtils.getAttributes(exception.getCause());
                    final String providerName = attributes.get(PROVIDER_NAME);
                    final String authenticateUrl = attributes.get(AUTHENTICATE_URL);
                    if (!Strings.isNullOrEmpty(providerName)
                        && !Strings.isNullOrEmpty(authenticateUrl)) {
                      return authUserAndRecallImport(
                          providerName, authenticateUrl, path, sourceStorage, subscriber);
                    } else {
                      throw new IllegalStateException(
                          localizationConstant.oauthFailedToGetAuthenticatorText());
                    }
                  default:
                    throw new IllegalStateException(exception.getCause());
                }
              }
            });
  }

  private Promise<Project> recallImportWithCredentials(
      final SourceStorage sourceStorage, final Path path) {
    return createFromAsyncRequest(
        new RequestCall<Project>() {
          @Override
          public void makeCall(final AsyncCallback<Project> callback) {
            credentialsDialog
                .askCredentials()
                .then(
                    credentials -> {
                      sourceStorage.getParameters().put("username", credentials.getUsername());
                      sourceStorage.getParameters().put("password", credentials.getPassword());
                      doImport(path, sourceStorage)
                          .then(
                              project -> {
                                callback.onSuccess(project);
                              })
                          .catchError(
                              error -> {
                                callback.onFailure(error.getCause());
                              });
                    });
          }
        });
  }

  private Promise<Project> authUserAndRecallImport(
      final String providerName,
      final String authenticateUrl,
      final Path path,
      final SourceStorage sourceStorage,
      final ProjectNotificationSubscriber subscriber) {

    return createFromAsyncRequest(
        new RequestCall<Project>() {
          @Override
          public void makeCall(final AsyncCallback<Project> callback) {
            OAuth2Authenticator authenticator =
                oAuth2AuthenticatorRegistry.getAuthenticator(providerName);
            if (authenticator == null) {
              authenticator = oAuth2AuthenticatorRegistry.getAuthenticator("default");
            }

            authenticator.authenticate(
                OAuth2AuthenticatorUrlProvider.get(
                    appContext.getMasterApiEndpoint(), authenticateUrl),
                new AsyncCallback<OAuthStatus>() {
                  @Override
                  public void onFailure(Throwable caught) {
                    callback.onFailure(new Exception(caught.getMessage()));
                  }

                  @Override
                  public void onSuccess(OAuthStatus result) {
                    if (!result.equals(OAuthStatus.NOT_PERFORMED)) {
                      oAuthServiceClient.getToken(
                          providerName,
                          new AsyncRequestCallback<OAuthToken>(
                              unmarshallerFactory.newUnmarshaller(OAuthToken.class)) {
                            @Override
                            protected void onSuccess(OAuthToken result) {
                              sourceStorage.getParameters().put("username", result.getToken());
                              sourceStorage.getParameters().put("password", result.getToken());

                              doImport(path, sourceStorage)
                                  .then(
                                      project -> {
                                        callback.onSuccess(project);
                                      })
                                  .catchError(
                                      error -> {
                                        callback.onFailure(error.getCause());
                                      });
                            }

                            @Override
                            protected void onFailure(Throwable exception) {
                              callback.onFailure(new Exception(exception.getMessage()));
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
