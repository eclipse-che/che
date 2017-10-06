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
package org.eclipse.che.plugin.github.ide.authenticator;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.api.ssh.shared.dto.SshPairDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.oauth.OAuth2Authenticator;
import org.eclipse.che.ide.api.oauth.OAuth2AuthenticatorUrlProvider;
import org.eclipse.che.ide.api.ssh.SshServiceClient;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.plugin.github.ide.GitHubLocalizationConstant;
import org.eclipse.che.plugin.ssh.key.client.SshKeyUploader;
import org.eclipse.che.plugin.ssh.key.client.SshKeyUploaderRegistry;
import org.eclipse.che.plugin.ssh.key.client.manage.SshKeyManagerPresenter;
import org.eclipse.che.security.oauth.JsOAuthWindow;
import org.eclipse.che.security.oauth.OAuthCallback;
import org.eclipse.che.security.oauth.OAuthStatus;
import org.eclipse.che.security.oauth.SecurityTokenProvider;

/** @author Roman Nikitenko */
public class GitHubAuthenticatorImpl
    implements OAuth2Authenticator, OAuthCallback, GitHubAuthenticatorViewImpl.ActionDelegate {
  public static final String GITHUB_HOST = "github.com";
  public static final String GITHUB = "github";

  AsyncCallback<OAuthStatus> callback;

  private final SshKeyUploaderRegistry registry;
  private final SshServiceClient sshServiceClient;
  private final DialogFactory dialogFactory;
  private final GitHubAuthenticatorView view;
  private final NotificationManager notificationManager;
  private final GitHubLocalizationConstant locale;
  private final String baseUrl;
  private final AppContext appContext;
  private final SecurityTokenProvider securityTokenPærovider;
  private String authenticationUrl;

  @Inject
  public GitHubAuthenticatorImpl(
      SshKeyUploaderRegistry registry,
      SshServiceClient sshServiceClient,
      GitHubAuthenticatorView view,
      DialogFactory dialogFactory,
      GitHubLocalizationConstant locale,
      NotificationManager notificationManager,
      AppContext appContext,
      SecurityTokenProvider securityTokenPærovider) {
    this.registry = registry;
    this.sshServiceClient = sshServiceClient;
    this.view = view;
    this.securityTokenPærovider = securityTokenPærovider;
    this.view.setDelegate(this);
    this.locale = locale;
    this.baseUrl = appContext.getMasterEndpoint();
    this.dialogFactory = dialogFactory;
    this.notificationManager = notificationManager;
    this.appContext = appContext;
  }

  @Override
  public void authenticate(
      String authenticationUrl, @NotNull final AsyncCallback<OAuthStatus> callback) {
    this.authenticationUrl = authenticationUrl;
    this.callback = callback;
    view.showDialog();
  }

  public Promise<OAuthStatus> authenticate(String authenticationUrl) {
    this.authenticationUrl = authenticationUrl;

    return AsyncPromiseHelper.createFromAsyncRequest(
        new AsyncPromiseHelper.RequestCall<OAuthStatus>() {
          @Override
          public void makeCall(AsyncCallback<OAuthStatus> callback) {
            GitHubAuthenticatorImpl.this.callback = callback;
            view.showDialog();
          }
        });
  }

  @Override
  public String getProviderName() {
    return GITHUB;
  }

  @Override
  public void onCancelled() {
    callback.onFailure(new Exception("Authorization request rejected by user."));
  }

  @Override
  public void onAccepted() {
    showAuthWindow();
  }

  @Override
  public void onAuthenticated(OAuthStatus authStatus) {
    if (view.isGenerateKeysSelected()) {
      generateSshKeys(authStatus);
      return;
    }
    callback.onSuccess(authStatus);
  }

  private void showAuthWindow() {
    JsOAuthWindow authWindow;
    if (authenticationUrl == null) {
      authWindow =
          new JsOAuthWindow(getAuthUrl(), "error.url", 500, 980, this, securityTokenPærovider);
    } else {
      authWindow =
          new JsOAuthWindow(authenticationUrl, "error.url", 500, 980, this, securityTokenPærovider);
    }
    authWindow.loginWithOAuth();
  }

  private String getAuthUrl() {
    return OAuth2AuthenticatorUrlProvider.get(
        baseUrl,
        "github",
        appContext.getCurrentUser().getProfile().getUserId(),
        Lists.asList("user", new String[] {"repo", "write:public_key"}));
  }

  private void generateSshKeys(final OAuthStatus authStatus) {
    final SshKeyUploader githubKeyUploader = registry.getUploader(GITHUB_HOST);
    if (githubKeyUploader != null) {
      String userId = appContext.getCurrentUser().getProfile().getUserId();
      githubKeyUploader.uploadKey(
          userId,
          new AsyncCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
              callback.onSuccess(authStatus);
              notificationManager.notify(locale.authMessageKeyUploadSuccess(), SUCCESS, FLOAT_MODE);
            }

            @Override
            public void onFailure(Throwable exception) {
              dialogFactory
                  .createMessageDialog(
                      locale.authorizationDialogTitle(),
                      locale.authMessageUnableCreateSshKey(),
                      null)
                  .show();
              callback.onFailure(new Exception(locale.authMessageUnableCreateSshKey()));
              getFailedKey();
            }
          });
    } else {
      dialogFactory
          .createMessageDialog(
              locale.authorizationDialogTitle(), locale.authMessageUnableCreateSshKey(), null)
          .show();
      callback.onFailure(new Exception(locale.authMessageUnableCreateSshKey()));
    }
  }

  /** Need to remove failed uploaded pair from local storage if they can't be uploaded to github */
  private void getFailedKey() {
    sshServiceClient
        .getPairs(SshKeyManagerPresenter.VCS_SSH_SERVICE)
        .then(
            new Operation<List<SshPairDto>>() {
              @Override
              public void apply(List<SshPairDto> result) throws OperationException {
                for (SshPairDto key : result) {
                  if (key.getName().equals(GITHUB_HOST)) {
                    removeFailedKey(key);
                    return;
                  }
                }
              }
            })
        .catchError(
            new Operation<PromiseError>() {
              @Override
              public void apply(PromiseError arg) throws OperationException {
                Log.error(OAuth2Authenticator.class, arg.getCause());
              }
            });
  }

  /**
   * Remove failed pair.
   *
   * @param pair failed pair
   */
  private void removeFailedKey(@NotNull final SshPairDto pair) {
    sshServiceClient
        .deletePair(pair.getService(), pair.getName())
        .catchError(
            new Operation<PromiseError>() {
              @Override
              public void apply(PromiseError arg) throws OperationException {
                Log.error(OAuth2Authenticator.class, arg.getCause());
              }
            });
  }
}
