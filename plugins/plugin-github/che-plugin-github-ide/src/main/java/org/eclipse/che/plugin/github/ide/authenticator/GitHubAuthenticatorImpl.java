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
package org.eclipse.che.plugin.github.ide.authenticator;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.oauth.OAuth2Authenticator;
import org.eclipse.che.ide.api.oauth.OAuth2AuthenticatorUrlProvider;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.plugin.github.ide.GitHubLocalizationConstant;
import org.eclipse.che.plugin.ssh.key.client.SshKeyManager;
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

  private final SshKeyManager sshKeyManager;
  private final DialogFactory dialogFactory;
  private final GitHubAuthenticatorView view;
  private final NotificationManager notificationManager;
  private final GitHubLocalizationConstant locale;
  private final String baseUrl;
  private final AppContext appContext;
  private final SecurityTokenProvider securityTokenProvider;
  private String authenticationUrl;

  @Inject
  public GitHubAuthenticatorImpl(
      SshKeyManager sshKeyManager,
      GitHubAuthenticatorView view,
      DialogFactory dialogFactory,
      GitHubLocalizationConstant locale,
      NotificationManager notificationManager,
      AppContext appContext,
      SecurityTokenProvider securityTokenProvider) {
    this.sshKeyManager = sshKeyManager;
    this.view = view;
    this.securityTokenProvider = securityTokenProvider;
    this.view.setDelegate(this);
    this.locale = locale;
    this.baseUrl = appContext.getMasterApiEndpoint();
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
          new JsOAuthWindow(getAuthUrl(), "error.url", 500, 980, this, securityTokenProvider);
    } else {
      authWindow =
          new JsOAuthWindow(authenticationUrl, "error.url", 500, 980, this, securityTokenProvider);
    }
    authWindow.loginWithOAuth();
  }

  private String getAuthUrl() {
    return OAuth2AuthenticatorUrlProvider.get(
        baseUrl,
        "github",
        appContext.getCurrentUser().getId(),
        Lists.asList("user", new String[] {"repo", "write:public_key"}));
  }

  private void generateSshKeys(final OAuthStatus authStatus) {
    sshKeyManager
        .generateSshKey(appContext.getCurrentUser().getId(), GITHUB_HOST)
        .then(
            arg -> {
              callback.onSuccess(authStatus);
              notificationManager.notify(locale.authMessageKeyUploadSuccess(), SUCCESS, FLOAT_MODE);
            })
        .catchError(
            arg -> {
              dialogFactory
                  .createMessageDialog(
                      locale.authorizationDialogTitle(),
                      locale.authMessageUnableCreateSshKey(),
                      null)
                  .show();
              callback.onFailure(new Exception(locale.authMessageUnableCreateSshKey()));
            });
  }
}
