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
package org.eclipse.che.plugin.pullrequest.client.steps;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

import com.google.inject.Singleton;
import javax.inject.Inject;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.commons.exception.UnauthorizedException;
import org.eclipse.che.plugin.pullrequest.client.ContributeMessages;
import org.eclipse.che.plugin.pullrequest.client.workflow.Context;
import org.eclipse.che.plugin.pullrequest.client.workflow.Step;
import org.eclipse.che.plugin.pullrequest.client.workflow.WorkflowExecutor;
import org.eclipse.che.plugin.pullrequest.shared.dto.HostUser;

/**
 * This step authorizes Codenvy on the VCS Host.
 *
 * @author Kevin Pollet
 * @author Yevhenii Voevodin
 */
@Singleton
public class AuthorizeCodenvyOnVCSHostStep implements Step {
  private final NotificationManager notificationManager;
  private final AppContext appContext;
  private final ContributeMessages messages;

  @Inject
  public AuthorizeCodenvyOnVCSHostStep(
      final NotificationManager notificationManager,
      final AppContext appContext,
      final ContributeMessages messages) {
    this.notificationManager = notificationManager;
    this.appContext = appContext;
    this.messages = messages;
  }

  @Override
  public void execute(final WorkflowExecutor executor, Context context) {
    context
        .getVcsHostingService()
        .getUserInfo()
        .then(authSuccessOp(executor, context))
        .catchError(getUserErrorOp(executor, context));
  }

  private Operation<HostUser> authSuccessOp(
      final WorkflowExecutor executor, final Context context) {
    return new Operation<HostUser>() {
      @Override
      public void apply(HostUser user) throws OperationException {
        context.setHostUserLogin(user.getLogin());
        executor.done(AuthorizeCodenvyOnVCSHostStep.this, context);
      }
    };
  }

  private Operation<PromiseError> getUserErrorOp(
      final WorkflowExecutor executor, final Context context) {
    return new Operation<PromiseError>() {
      @Override
      public void apply(PromiseError error) throws OperationException {
        try {
          throw error.getCause();
        } catch (UnauthorizedException unEx) {
          authenticate(executor, context);
        } catch (Throwable thr) {
          handleThrowable(thr, executor, context);
        }
      }
    };
  }

  private void authenticate(final WorkflowExecutor executor, final Context context) {
    context
        .getVcsHostingService()
        .authenticate(appContext.getCurrentUser())
        .then(authSuccessOp(executor, context))
        .catchError(
            new Operation<PromiseError>() {
              @Override
              public void apply(PromiseError err) throws OperationException {
                try {
                  throw err.getCause();
                } catch (UnauthorizedException unEx) {
                  notificationManager.notify(
                      messages.stepAuthorizeCodenvyOnVCSHostErrorCannotAccessVCSHostTitle(),
                      messages.stepAuthorizeCodenvyOnVCSHostErrorCannotAccessVCSHostContent(),
                      FAIL,
                      FLOAT_MODE);
                  executor.fail(
                      AuthorizeCodenvyOnVCSHostStep.this, context, unEx.getLocalizedMessage());
                } catch (Throwable thr) {
                  handleThrowable(thr, executor, context);
                }
              }
            });
  }

  private void handleThrowable(
      final Throwable thr, final WorkflowExecutor workflow, final Context context) {
    notificationManager.notify(thr.getLocalizedMessage(), FAIL, FLOAT_MODE);
    workflow.fail(this, context, thr.getLocalizedMessage());
  }
}
