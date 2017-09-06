/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.projectimport;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Boolean.parseBoolean;
import static java.util.Collections.emptyMap;
import static org.eclipse.che.api.core.ErrorCodes.FAILED_CHECKOUT;
import static org.eclipse.che.api.core.ErrorCodes.FAILED_CHECKOUT_WITH_START_POINT;
import static org.eclipse.che.api.core.ErrorCodes.UNABLE_GET_PRIVATE_SSH_KEY;
import static org.eclipse.che.api.core.ErrorCodes.UNAUTHORIZED_GIT_OPERATION;
import static org.eclipse.che.api.core.ErrorCodes.UNAUTHORIZED_SVN_OPERATION;
import static org.eclipse.che.api.git.shared.ProviderInfo.AUTHENTICATE_URL;
import static org.eclipse.che.api.git.shared.ProviderInfo.PROVIDER_NAME;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;
import static org.eclipse.che.ide.util.ExceptionUtils.getAttributes;
import static org.eclipse.che.ide.util.StringUtils.isNullOrEmpty;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.SourceStorage;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.oauth.OAuth2Authenticator;
import org.eclipse.che.ide.api.oauth.OAuth2AuthenticatorRegistry;
import org.eclipse.che.ide.api.oauth.OAuth2AuthenticatorUrlProvider;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.workspace.WorkspaceReadyEvent;
import org.eclipse.che.ide.projectimport.wizard.ImportProjectNotificationSubscriberFactory;
import org.eclipse.che.ide.projectimport.wizard.ProjectImportOutputJsonRpcNotifier;
import org.eclipse.che.ide.projectimport.wizard.ProjectNotificationSubscriber;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.askcredentials.AskCredentialsDialog;
import org.eclipse.che.ide.util.ExceptionUtils;
import org.eclipse.che.security.oauth.OAuthStatus;

/** Imports projects on file system. */
@Singleton
public class InitialProjectImporter extends AbstractImporter {

  private final OAuth2AuthenticatorRegistry oAuth2AuthenticatorRegistry;
  private final ProjectImportOutputJsonRpcNotifier subscriber;
  private final NotificationManager notificationManager;
  private final CoreLocalizationConstant locale;
  private final AskCredentialsDialog askCredentialsDialog;
  private final DialogFactory dialogFactory;
  private final PromiseProvider promises;

  private AsyncCallback<Void> callback;

  @Inject
  public InitialProjectImporter(
      ImportProjectNotificationSubscriberFactory subscriberFactory,
      AppContext appContext,
      OAuth2AuthenticatorRegistry oAuth2AuthenticatorRegistry,
      ProjectImportOutputJsonRpcNotifier subscriber,
      NotificationManager notificationManager,
      CoreLocalizationConstant locale,
      AskCredentialsDialog askCredentialsDialog,
      DialogFactory dialogFactory,
      PromiseProvider promises,
      EventBus eventBus) {

    super(appContext, subscriberFactory);

    this.oAuth2AuthenticatorRegistry = oAuth2AuthenticatorRegistry;
    this.subscriber = subscriber;
    this.notificationManager = notificationManager;
    this.locale = locale;
    this.askCredentialsDialog = askCredentialsDialog;
    this.dialogFactory = dialogFactory;
    this.promises = promises;

    eventBus.addHandler(WorkspaceReadyEvent.getType(), e -> importProjects());
  }

  /** Imports all projects described in workspace configuration but not existed on file system. */
  private void importProjects() {
    if (appContext.getFactory() != null) {
      return;
    }

    Project[] projects = appContext.getProjects();

    List<Project> importProjects = new ArrayList<>();
    for (Project project : projects) {
      if (project.exists()
          || project.getSource() == null
          || project.getSource().getLocation() == null) {
        continue;
      }

      importProjects.add(project);
    }

    importProjects(importProjects, null);
  }

  /**
   * Import source projects and if it's already exist in workspace then show warning notification
   *
   * @param projects list of projects that already exist in workspace and will be imported on file
   *     system
   */
  public void importProjects(List<Project> projects, AsyncCallback<Void> callback) {
    this.callback = callback;

    if (projects.isEmpty()) {
      if (callback != null) {
        callback.onSuccess(null);
      }
      return;
    }

    List<Promise<Project>> imports = newArrayList();

    for (Project projectConfig : projects) {
      imports.add(
          startImport(Path.valueOf(projectConfig.getPath()), projectConfig.getSource())
              .thenPromise(project -> project.update().withBody(projectConfig).send()));
    }

    promises
        .all2(imports.toArray(new Promise<?>[imports.size()]))
        .then(
            ignored -> {
              if (callback != null) {
                callback.onSuccess(null);
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

  private Promise<Project> doImport(Path pathToProject, SourceStorage sourceStorage) {
    String projectName = pathToProject.lastSegment();
    StatusNotification notification =
        notificationManager.notify(locale.cloningSource(projectName), null, PROGRESS, FLOAT_MODE);
    subscriber.subscribe(projectName, notification);
    String location = sourceStorage.getLocation();

    // it's needed for extract repository name from repository url e.g https://github.com/a/b.git
    // lastIndexOf('/') + 1 for not to capture slash and length - 4 for trim .git
    String repository = location.substring(location.lastIndexOf('/') + 1).replace(".git", "");
    Map<String, String> parameters = firstNonNull(sourceStorage.getParameters(), emptyMap());
    String branch = parameters.get("branch");
    String startPoint = parameters.get("startPoint");

    MutableProjectConfig importConfig = new MutableProjectConfig();
    importConfig.setPath(pathToProject.toString());
    importConfig.setSource(sourceStorage);

    return appContext
        .getWorkspaceRoot()
        .importProject()
        .withBody(importConfig)
        .send()
        .thenPromise(
            project -> {
              subscriber.onSuccess();

              notification.setContent(locale.clonedSource(projectName));
              notification.setStatus(SUCCESS);

              return promises.resolve(project);
            })
        .catchErrorPromise(
            caught -> {
              int errorCode = ExceptionUtils.getErrorCode(caught.getCause());
              switch (errorCode) {
                case UNAUTHORIZED_GIT_OPERATION:
                  subscriber.onFailure(caught.getMessage());

                  Map<String, String> attributes = getAttributes(caught.getCause());
                  String providerName = attributes.get(PROVIDER_NAME);
                  String authenticateUrl = attributes.get(AUTHENTICATE_URL);
                  boolean authenticated = parseBoolean(attributes.get("authenticated"));

                  if (!(isNullOrEmpty(providerName) || isNullOrEmpty(authenticateUrl))) {
                    if (!authenticated) {
                      return tryAuthenticateAndRepeatImport(
                          providerName, authenticateUrl, pathToProject, sourceStorage, subscriber);
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
                  subscriber.onFailure(caught.getMessage());
                  return recallSubversionImportWithCredentials(pathToProject, sourceStorage);
                case UNABLE_GET_PRIVATE_SSH_KEY:
                  subscriber.onFailure(locale.acceptSshNotFoundText());
                  break;
                case FAILED_CHECKOUT:
                  subscriber.onFailure(locale.cloningSourceWithCheckoutFailed(branch, repository));
                  notification.setTitle(locale.cloningSourceFailedTitle(projectName));
                  break;
                case FAILED_CHECKOUT_WITH_START_POINT:
                  subscriber.onFailure(locale.cloningSourceCheckoutFailed(branch, startPoint));
                  notification.setTitle(locale.cloningSourceFailedTitle(projectName));
                  break;
                default:
                  subscriber.onFailure(caught.getMessage());
                  notification.setTitle(locale.cloningSourceFailedTitle(projectName));
                  notification.setStatus(FAIL);
              }

              return promises.resolve(null);
            });
  }

  private Promise<Project> tryAuthenticateAndRepeatImport(
      String providerName,
      String authenticateUrl,
      Path pathToProject,
      SourceStorage sourceStorage,
      ProjectNotificationSubscriber subscriber) {
    OAuth2Authenticator authenticator = oAuth2AuthenticatorRegistry.getAuthenticator(providerName);
    if (authenticator == null) {
      authenticator = oAuth2AuthenticatorRegistry.getAuthenticator("default");
    }

    return authenticator
        .authenticate(
            OAuth2AuthenticatorUrlProvider.get(appContext.getMasterApiEndpoint(), authenticateUrl))
        .thenPromise(
            result -> {
              if (!result.equals(OAuthStatus.NOT_PERFORMED)) {
                return doImport(pathToProject, sourceStorage);
              } else {
                subscriber.onFailure("Authentication cancelled");
                callback.onSuccess(null);
              }

              return promises.resolve(null);
            })
        .catchError(
            caught -> {
              callback.onFailure(caught.getCause());
            });
  }

  private Promise<Project> recallSubversionImportWithCredentials(
      Path path, SourceStorage sourceStorage) {
    return askCredentialsDialog
        .askCredentials()
        .thenPromise(
            credentials -> {
              sourceStorage.getParameters().put("username", credentials.getUsername());
              sourceStorage.getParameters().put("password", credentials.getPassword());
              return doImport(path, sourceStorage);
            })
        .catchError(
            caught -> {
              callback.onFailure(caught.getCause());
            });
  }
}
