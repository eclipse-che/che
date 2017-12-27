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
package org.eclipse.che.ide.ext.git.client;

import static org.eclipse.che.api.git.shared.ProviderInfo.PROVIDER_NAME;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.util.ExceptionUtils.getAttributes;
import static org.eclipse.che.ide.util.ExceptionUtils.getErrorCode;

import java.util.Map;
import org.eclipse.che.api.core.ErrorCodes;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.auth.Credentials;
import org.eclipse.che.ide.api.auth.OAuthServiceClient;
import org.eclipse.che.ide.api.notification.NotificationManager;

/**
 * Base presenter class for authenticated Git operations.
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
public class GitAuthActionPresenter {

  protected NotificationManager notificationManager;
  protected GitLocalizationConstant locale;
  protected OAuthServiceClient oAuthServiceClient;

  public GitAuthActionPresenter(
      NotificationManager notificationManager,
      GitLocalizationConstant locale,
      OAuthServiceClient oAuthServiceClient) {
    this.notificationManager = notificationManager;
    this.locale = locale;
    this.oAuthServiceClient = oAuthServiceClient;
  }

  /**
   * Performs git operation. If this operations fails with authorization error the operation will be
   * recalled with requested credentials
   *
   * @param operation operation that might require auth
   */
  protected <Y> Promise<Y> performOperationWithTokenRequestIfNeeded(
      final RemoteGitOperation<Y> operation) {
    return operation
        .perform(null)
        .catchErrorPromise(
            new Function<PromiseError, Promise<Y>>() {
              @Override
              public Promise<Y> apply(PromiseError error) throws FunctionException {
                if (getErrorCode(error.getCause()) == ErrorCodes.UNAUTHORIZED_GIT_OPERATION) {
                  Map<String, String> attributes = getAttributes(error.getCause());
                  String providerName = attributes.get(PROVIDER_NAME);

                  return oAuthServiceClient
                      .getToken(providerName)
                      .thenPromise(
                          token ->
                              Promises.resolve(new Credentials(token.getToken(), token.getToken())))
                      .thenPromise(operation::perform)
                      .catchError(
                          (Operation<PromiseError>)
                              err ->
                                  notificationManager.notify(
                                      locale.messagesNotAuthorizedContent(), FAIL, FLOAT_MODE));
                }
                return Promises.reject(error);
              }
            });
  }

  /** Remote git operation that can require credentials. */
  protected interface RemoteGitOperation<Y> {
    Promise<Y> perform(Credentials credentials);
  }
}
