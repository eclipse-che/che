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
package org.eclipse.che.plugin.pullrequest.client.vcs.hosting;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.js.Executor;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.api.promises.client.js.RejectFunction;
import org.eclipse.che.api.promises.client.js.ResolveFunction;
import org.eclipse.che.plugin.pullrequest.shared.dto.HostUser;
import org.eclipse.che.security.oauth.JsOAuthWindow;
import org.eclipse.che.security.oauth.OAuthCallback;
import org.eclipse.che.security.oauth.OAuthStatus;
import org.eclipse.che.security.oauth.SecurityTokenProvider;

/**
 * Utils for {@link VcsHostingService} implementations.
 *
 * @author Yevhenii Voevodin
 */
public final class ServiceUtil {

  /**
   * Performs {@link JsOAuthWindow} authentication and tries to get current user.
   *
   * @param service hosting service, used to authorized user
   * @param authUrl url to perform authentication
   * @return the promise which resolves authorized user or rejects with an error
   */
  public static Promise<HostUser> performWindowAuth(
      final VcsHostingService service,
      final String authUrl,
      final SecurityTokenProvider securityTokenProvider) {
    final Executor.ExecutorBody<HostUser> exBody =
        new Executor.ExecutorBody<HostUser>() {
          @Override
          public void apply(final ResolveFunction<HostUser> resolve, final RejectFunction reject) {
            new JsOAuthWindow(
                    authUrl,
                    "error.url",
                    500,
                    980,
                    new OAuthCallback() {
                      @Override
                      public void onAuthenticated(final OAuthStatus authStatus) {
                        // maybe it's possible to avoid this request if authStatus contains the vcs host user.
                        service
                            .getUserInfo()
                            .then(
                                new Operation<HostUser>() {
                                  @Override
                                  public void apply(HostUser user) throws OperationException {
                                    resolve.apply(user);
                                  }
                                })
                            .catchError(
                                new Operation<PromiseError>() {
                                  @Override
                                  public void apply(PromiseError error) throws OperationException {
                                    reject.apply(error);
                                  }
                                });
                      }
                    },
                    securityTokenProvider)
                .loginWithOAuth();
          }
        };
    return Promises.create(Executor.create(exBody));
  }

  private ServiceUtil() {}
}
