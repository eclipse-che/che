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
package org.eclipse.che.plugin.github.ide;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.ProductInfoDataProvider;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.auth.OAuthServiceClient;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.commons.exception.UnauthorizedException;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.plugin.ssh.key.client.SshKeyUploader;
import org.eclipse.che.security.oauth.JsOAuthWindow;
import org.eclipse.che.security.oauth.OAuthCallback;
import org.eclipse.che.security.oauth.OAuthStatus;
import org.eclipse.che.security.oauth.SecurityTokenProvider;

/**
 * Uploads SSH keys for github.com.
 *
 * @author Ann Shumilova
 */
@Singleton
public class GitHubSshKeyUploader implements SshKeyUploader, OAuthCallback {

  private final GitHubServiceClient gitHubService;
  private final String baseUrl;
  private final GitHubLocalizationConstant constant;
  private final NotificationManager notificationManager;
  private final ProductInfoDataProvider productInfoDataProvider;
  private final DialogFactory dialogFactory;
  private final AppContext appContext;
  private final SecurityTokenProvider securityTokenProvider;
  private final OAuthServiceClient oAuthServiceClient;

  private AsyncCallback<Void> callback;
  private String userId;

  @Inject
  public GitHubSshKeyUploader(
      GitHubServiceClient gitHubService,
      GitHubLocalizationConstant constant,
      NotificationManager notificationManager,
      ProductInfoDataProvider productInfoDataProvider,
      DialogFactory dialogFactory,
      AppContext appContext,
      SecurityTokenProvider securityTokenProvider,
      OAuthServiceClient oAuthServiceClient) {
    this.gitHubService = gitHubService;
    this.baseUrl = appContext.getMasterApiEndpoint();
    this.constant = constant;
    this.notificationManager = notificationManager;
    this.productInfoDataProvider = productInfoDataProvider;
    this.dialogFactory = dialogFactory;
    this.appContext = appContext;
    this.securityTokenProvider = securityTokenProvider;
    this.oAuthServiceClient = oAuthServiceClient;
  }

  /** {@inheritDoc} */
  @Override
  public void uploadKey(final String userId, final AsyncCallback<Void> callback) {
    this.callback = callback;
    this.userId = userId;

    oAuthServiceClient
        .getToken("github")
        .then(
            result -> {
              gitHubService.updatePublicKey(
                  result.getToken(),
                  new AsyncRequestCallback<Void>() {
                    @Override
                    protected void onSuccess(Void o) {
                      callback.onSuccess(o);
                    }

                    @Override
                    protected void onFailure(Throwable e) {
                      if (e instanceof UnauthorizedException) {
                        oAuthLoginStart();
                        return;
                      }
                      callback.onFailure(e);
                    }
                  });
            })
        .catchError(
            error -> {
              oAuthLoginStart();
            });
  }

  /** Log in github */
  private void oAuthLoginStart() {
    dialogFactory
        .createConfirmDialog(
            constant.authorizationDialogTitle(),
            constant.authorizationDialogText(productInfoDataProvider.getName()),
            () -> showPopUp(),
            () -> callback.onFailure(new Exception(constant.authorizationRequestRejected())))
        .show();
  }

  private void showPopUp() {
    String authUrl =
        baseUrl
            + "/oauth/authenticate?oauth_provider=github"
            + "&scope=user,repo,write:public_key&userId="
            + userId
            + "&redirect_after_login="
            + Window.Location.getProtocol()
            + "//"
            + Window.Location.getHost()
            + "/ws/"
            + appContext.getWorkspace().getConfig().getName();
    JsOAuthWindow authWindow =
        new JsOAuthWindow(authUrl, "error.url", 500, 980, this, securityTokenProvider);
    authWindow.loginWithOAuth();
  }

  /** {@inheritDoc} */
  @Override
  public void onAuthenticated(OAuthStatus authStatus) {
    if (OAuthStatus.LOGGED_IN.equals(authStatus)) {
      uploadKey(userId, callback);
    } else {
      notificationManager.notify(
          constant.authorizationFailed(), StatusNotification.Status.FAIL, FLOAT_MODE);
      callback.onFailure(new Exception(constant.authorizationFailed()));
    }
  }
}
