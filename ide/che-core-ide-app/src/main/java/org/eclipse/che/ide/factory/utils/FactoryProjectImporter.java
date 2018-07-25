/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.factory.utils;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.eclipse.che.api.core.ErrorCodes.FAILED_CHECKOUT;
import static org.eclipse.che.api.core.ErrorCodes.FAILED_CHECKOUT_WITH_START_POINT;
import static org.eclipse.che.api.core.ErrorCodes.UNABLE_GET_PRIVATE_SSH_KEY;
import static org.eclipse.che.api.core.ErrorCodes.UNAUTHORIZED_GIT_OPERATION;
import static org.eclipse.che.api.core.ErrorCodes.UNAUTHORIZED_SVN_OPERATION;
import static org.eclipse.che.api.git.shared.ProviderInfo.AUTHENTICATE_URL;
import static org.eclipse.che.api.git.shared.ProviderInfo.PROVIDER_NAME;
import static org.eclipse.che.ide.api.jsonrpc.Constants.WS_AGENT_JSON_RPC_ENDPOINT_ID;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.model.workspace.config.SourceStorage;
import org.eclipse.che.api.git.shared.event.GitCheckoutEvent;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.auth.Credentials;
import org.eclipse.che.ide.api.factory.model.FactoryImpl;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.oauth.OAuth2Authenticator;
import org.eclipse.che.ide.api.oauth.OAuth2AuthenticatorRegistry;
import org.eclipse.che.ide.api.oauth.OAuth2AuthenticatorUrlProvider;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.workspace.model.ProjectConfigImpl;
import org.eclipse.che.ide.projectimport.AbstractImporter;
import org.eclipse.che.ide.projectimport.wizard.ImportProjectNotificationSubscriberFactory;
import org.eclipse.che.ide.projectimport.wizard.ProjectImportOutputJsonRpcNotifier;
import org.eclipse.che.ide.projectimport.wizard.ProjectNotificationSubscriber;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.askcredentials.AskCredentialsDialog;
import org.eclipse.che.ide.util.ExceptionUtils;
import org.eclipse.che.ide.util.StringUtils;
import org.eclipse.che.security.oauth.OAuthStatus;

/**
 * @author Sergii Leschenko
 * @author Valeriy Svydenko
 * @author Anton Korneta
 */
@Singleton
public class FactoryProjectImporter extends AbstractImporter {
  private final AskCredentialsDialog askCredentialsDialog;
  private final CoreLocalizationConstant locale;
  private final NotificationManager notificationManager;
  private final String restContext;
  private final DialogFactory dialogFactory;
  private final OAuth2AuthenticatorRegistry oAuth2AuthenticatorRegistry;
  private final RequestTransmitter requestTransmitter;
  private final ProjectImportOutputJsonRpcNotifier subscriber;

  private final Map<String, CheckoutContext> checkoutContextRegistry = new HashMap<>();

  private FactoryImpl factory;
  private AsyncCallback<Void> callback;

  @Inject
  public FactoryProjectImporter(
      AppContext appContext,
      NotificationManager notificationManager,
      AskCredentialsDialog askCredentialsDialog,
      CoreLocalizationConstant locale,
      ImportProjectNotificationSubscriberFactory subscriberFactory,
      DialogFactory dialogFactory,
      OAuth2AuthenticatorRegistry oAuth2AuthenticatorRegistry,
      RequestTransmitter requestTransmitter,
      ProjectImportOutputJsonRpcNotifier subscriber) {
    super(appContext, subscriberFactory);
    this.notificationManager = notificationManager;
    this.askCredentialsDialog = askCredentialsDialog;
    this.locale = locale;
    this.restContext = appContext.getMasterApiEndpoint();
    this.dialogFactory = dialogFactory;
    this.oAuth2AuthenticatorRegistry = oAuth2AuthenticatorRegistry;
    this.requestTransmitter = requestTransmitter;
    this.subscriber = subscriber;
  }

  @Inject
  private void configure(RequestHandlerConfigurator requestHandlerConfigurator) {
    requestHandlerConfigurator
        .newConfiguration()
        .methodName("git/checkoutOutput")
        .paramsAsDto(GitCheckoutEvent.class)
        .noResult()
        .withConsumer(this::consumeGitCheckoutEvent);
  }

  private void consumeGitCheckoutEvent(GitCheckoutEvent event) {
    CheckoutContext context =
        checkoutContextRegistry.get(event.getWorkspaceId() + event.getProjectName());
    if (context == null) {
      return;
    }

    String projectName = context.projectName;
    String reference = event.isCheckoutOnly() ? event.getBranchRef() : context.startPoint;
    String repository = context.repository;
    String branch = context.branch;

    String title = locale.clonedSource(projectName);
    String content = locale.clonedSourceWithCheckout(projectName, repository, reference, branch);

    notificationManager.notify(title, content, SUCCESS, FLOAT_MODE);
  }

  public void startImporting(FactoryImpl factory, AsyncCallback<Void> callback) {
    this.callback = callback;
    this.factory = factory;
    importProjects();
  }

  /** Import source projects */
  private void importProjects() {
    final Project[] projects = appContext.getProjects();

    Set<String> projectNames = new HashSet<>();
    String createPolicy = factory.getPolicies() != null ? factory.getPolicies().getCreate() : null;
    for (Project project : projects) {
      if (project.getSource() == null || project.getSource().getLocation() == null) {
        continue;
      }

      if (project.exists()) {
        // to prevent warning when reusing same workspace
        if (!("perUser".equals(createPolicy) || "perAccount".equals(createPolicy))) {
          notificationManager.notify(
              "Import", locale.projectAlreadyImported(project.getName()), FAIL, FLOAT_MODE);
        }
        continue;
      }

      projectNames.add(project.getName());
    }
    importProjects(projectNames);
  }

  /**
   * Import source projects and if it's already exist in workspace then show warning notification
   *
   * @param projectsToImport set of project names that already exist in workspace and will be
   *     imported on file system
   */
  private void importProjects(Set<String> projectsToImport) {
    final List<Promise<Project>> promises = new ArrayList<>();
    for (final ProjectConfigImpl projectConfig : factory.getWorkspace().getProjects()) {
      if (projectsToImport.contains(projectConfig.getName())) {
        promises.add(
            startImport(Path.valueOf(projectConfig.getPath()), projectConfig.getSource())
                .thenPromise(
                    new Function<Project, Promise<Project>>() {
                      @Override
                      public Promise<Project> apply(Project project) throws FunctionException {
                        return project.update().withBody(projectConfig).send();
                      }
                    }));
      }
    }

    Promises.all(promises.toArray(new Promise<?>[promises.size()]))
        .then(
            arg -> {
              callback.onSuccess(null);
            })
        .catchError(
            promiseError -> {
              // If it is unable to import any number of projects then factory import status will be
              // success anyway
              callback.onSuccess(null);
            });
  }

  @Override
  protected Promise<Project> importProject(
      @NotNull final Path pathToProject, @NotNull final SourceStorage sourceStorage) {
    return doImport(pathToProject, sourceStorage);
  }

  private Promise<Project> doImport(
      @NotNull final Path pathToProject, @NotNull final SourceStorage sourceStorage) {
    final String projectName = pathToProject.lastSegment();
    final StatusNotification notification =
        notificationManager.notify(locale.cloningSource(projectName), null, PROGRESS, FLOAT_MODE);
    subscriber.subscribe(projectName, notification);
    String location = sourceStorage.getLocation();
    // it's needed for extract repository name from repository url e.g
    // https://github.com/codenvy/che-core.git
    // lastIndexOf('/') + 1 for not to capture slash and length - 4 for trim .git
    final String repository = location.substring(location.lastIndexOf('/') + 1).replace(".git", "");
    final Map<String, String> parameters =
        firstNonNull(sourceStorage.getParameters(), Collections.<String, String>emptyMap());
    final String branch = parameters.get("branch");
    final String startPoint = parameters.get("startPoint");

    subscribe(projectName, repository, branch, startPoint);

    MutableProjectConfig importConfig = new MutableProjectConfig();
    importConfig.setPath(pathToProject.toString());
    importConfig.setSource(sourceStorage);

    return appContext
        .getWorkspaceRoot()
        .importProject()
        .withBody(importConfig)
        .send()
        .then(
            new Function<Project, Project>() {
              @Override
              public Project apply(Project project) throws FunctionException {
                subscriber.onSuccess();
                unsubscribe(projectName);

                notification.setContent(locale.clonedSource(projectName));
                notification.setStatus(SUCCESS);

                return project;
              }
            })
        .catchErrorPromise(
            new Function<PromiseError, Promise<Project>>() {
              @Override
              public Promise<Project> apply(PromiseError err) throws FunctionException {
                final int errorCode = ExceptionUtils.getErrorCode(err.getCause());
                unsubscribe(projectName);
                switch (errorCode) {
                  case UNAUTHORIZED_GIT_OPERATION:
                    subscriber.onFailure(err.getMessage());

                    final Map<String, String> attributes =
                        ExceptionUtils.getAttributes(err.getCause());
                    final String providerName = attributes.get(PROVIDER_NAME);
                    final String authenticateUrl = attributes.get(AUTHENTICATE_URL);
                    final boolean authenticated =
                        Boolean.parseBoolean(attributes.get("authenticated"));
                    if (!StringUtils.isNullOrEmpty(providerName)
                        && !StringUtils.isNullOrEmpty(authenticateUrl)) {
                      if (!authenticated) {
                        return tryAuthenticateAndRepeatImport(
                            providerName,
                            authenticateUrl,
                            pathToProject,
                            sourceStorage,
                            subscriber);
                      } else {
                        dialogFactory
                            .createMessageDialog(
                                locale.cloningSourceSshKeyUploadFailedTitle(),
                                locale.cloningSourcesSshKeyUploadFailedText(),
                                null)
                            .show();
                      }
                    } else {
                      dialogFactory
                          .createMessageDialog(
                              locale.oauthFailedToGetAuthenticatorTitle(),
                              locale.oauthFailedToGetAuthenticatorText(),
                              null)
                          .show();
                    }

                    break;
                  case UNAUTHORIZED_SVN_OPERATION:
                    subscriber.onFailure(err.getMessage());
                    return recallSubversionImportWithCredentials(pathToProject, sourceStorage);
                  case UNABLE_GET_PRIVATE_SSH_KEY:
                    subscriber.onFailure(locale.acceptSshNotFoundText());
                    break;
                  case FAILED_CHECKOUT:
                    subscriber.onFailure(
                        locale.cloningSourceWithCheckoutFailed(branch, repository));
                    notification.setTitle(locale.cloningSourceFailedTitle(projectName));
                    break;
                  case FAILED_CHECKOUT_WITH_START_POINT:
                    subscriber.onFailure(locale.cloningSourceCheckoutFailed(branch, startPoint));
                    notification.setTitle(locale.cloningSourceFailedTitle(projectName));
                    break;
                  default:
                    subscriber.onFailure(err.getMessage());
                    notification.setTitle(locale.cloningSourceFailedTitle(projectName));
                    notification.setStatus(FAIL);
                }

                return Promises.resolve(null);
              }
            });
  }

  private void subscribe(String projectName, String repository, String branch, String startPoint) {
    String key = appContext.getWorkspaceId() + projectName;

    checkoutContextRegistry.put(
        key, new CheckoutContext(projectName, repository, branch, startPoint));
    requestTransmitter
        .newRequest()
        .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
        .methodName("git/checkoutOutput/subscribe")
        .paramsAsString(key)
        .sendAndSkipResult();
  }

  private void unsubscribe(String projectName) {
    String key = appContext.getWorkspaceId() + projectName;

    checkoutContextRegistry.remove(key);
    requestTransmitter
        .newRequest()
        .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
        .methodName("git/checkoutOutput/unsubscribe")
        .paramsAsString(key)
        .sendAndSkipResult();
  }

  private Promise<Project> tryAuthenticateAndRepeatImport(
      @NotNull final String providerName,
      @NotNull final String authenticateUrl,
      @NotNull final Path pathToProject,
      @NotNull final SourceStorage sourceStorage,
      @NotNull final ProjectNotificationSubscriber subscriber) {
    OAuth2Authenticator authenticator = oAuth2AuthenticatorRegistry.getAuthenticator(providerName);
    if (authenticator == null) {
      authenticator = oAuth2AuthenticatorRegistry.getAuthenticator("default");
    }
    return authenticator
        .authenticate(OAuth2AuthenticatorUrlProvider.get(restContext, authenticateUrl))
        .thenPromise(
            new Function<OAuthStatus, Promise<Project>>() {
              @Override
              public Promise<Project> apply(OAuthStatus result) throws FunctionException {
                if (!result.equals(OAuthStatus.NOT_PERFORMED)) {
                  return doImport(pathToProject, sourceStorage);
                } else {
                  subscriber.onFailure("Authentication cancelled");
                  callback.onSuccess(null);
                }

                return Promises.resolve(null);
              }
            })
        .catchError(
            caught -> {
              callback.onFailure(new Exception(caught.getMessage()));
            });
  }

  private Promise<Project> recallSubversionImportWithCredentials(
      final Path path, final SourceStorage sourceStorage) {
    return askCredentialsDialog
        .askCredentials()
        .thenPromise(
            new Function<Credentials, Promise<Project>>() {
              @Override
              public Promise<Project> apply(Credentials credentials) throws FunctionException {
                sourceStorage.getParameters().put("username", credentials.getUsername());
                sourceStorage.getParameters().put("password", credentials.getPassword());
                return doImport(path, sourceStorage);
              }
            })
        .catchError(
            error -> {
              callback.onFailure(error.getCause());
            });
  }

  private class CheckoutContext {
    private final String projectName;
    private final String repository;
    private final String branch;
    private final String startPoint;

    private CheckoutContext(
        String projectName, String repository, String branch, String startPoint) {
      this.projectName = projectName;
      this.repository = repository;
      this.branch = branch;
      this.startPoint = startPoint;
    }
  }
}
