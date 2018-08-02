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

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.inject.Singleton;
import javax.inject.Inject;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.factory.FactoryServiceClient;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.plugin.pullrequest.client.ContributeMessages;
import org.eclipse.che.plugin.pullrequest.client.utils.FactoryHelper;
import org.eclipse.che.plugin.pullrequest.client.workflow.Context;
import org.eclipse.che.plugin.pullrequest.client.workflow.Step;
import org.eclipse.che.plugin.pullrequest.client.workflow.WorkflowExecutor;

/** Generates a factory for the contribution reviewer. */
@Singleton
public class GenerateReviewFactoryStep implements Step {
  private final ContributeMessages messages;
  private final AppContext appContext;
  private final NotificationManager notificationManager;
  private final FactoryServiceClient factoryService;

  @Inject
  public GenerateReviewFactoryStep(
      final ContributeMessages messages,
      final AppContext appContext,
      final NotificationManager notificationManager,
      final FactoryServiceClient factoryService) {
    this.messages = messages;
    this.appContext = appContext;
    this.notificationManager = notificationManager;
    this.factoryService = factoryService;
  }

  @Override
  public void execute(final WorkflowExecutor executor, final Context context) {
    factoryService
        .getFactoryJson(appContext.getWorkspaceId(), null)
        .then(updateProjectAttributes(context))
        .then(
            new Operation<FactoryDto>() {
              @Override
              public void apply(FactoryDto factory) throws OperationException {
                factoryService
                    .saveFactory(factory)
                    .then(
                        new Operation<FactoryDto>() {
                          @Override
                          public void apply(FactoryDto factory) throws OperationException {
                            context.setReviewFactoryUrl(FactoryHelper.getAcceptFactoryUrl(factory));
                            executor.done(GenerateReviewFactoryStep.this, context);
                          }
                        })
                    .catchError(
                        new Operation<PromiseError>() {
                          @Override
                          public void apply(PromiseError arg) throws OperationException {
                            notificationManager.notify(
                                messages.stepGenerateReviewFactoryErrorCreateFactory(),
                                FAIL,
                                NOT_EMERGE_MODE);
                            executor.done(GenerateReviewFactoryStep.this, context);
                          }
                        });
              }
            })
        .catchError(
            new Operation<PromiseError>() {
              @Override
              public void apply(PromiseError arg) throws OperationException {
                notificationManager.notify(
                    messages.stepGenerateReviewFactoryErrorCreateFactory(), FAIL, NOT_EMERGE_MODE);
                executor.done(GenerateReviewFactoryStep.this, context);
              }
            });
  }

  private Function<FactoryDto, FactoryDto> updateProjectAttributes(final Context context) {
    return new Function<FactoryDto, FactoryDto>() {
      @Override
      public FactoryDto apply(FactoryDto factory) throws FunctionException {
        final Optional<ProjectConfigDto> projectOpt =
            FluentIterable.from(factory.getWorkspace().getProjects())
                .filter(
                    new Predicate<ProjectConfigDto>() {
                      @Override
                      public boolean apply(ProjectConfigDto project) {
                        return project.getName().equals(context.getProject().getName());
                      }
                    })
                .first();
        if (projectOpt.isPresent()) {
          final ProjectConfigDto project = projectOpt.get();
          project.getSource().getParameters().put("branch", context.getWorkBranchName());

          if (context.isForkAvailable()) {
            project
                .getSource()
                .setLocation(
                    context
                        .getVcsHostingService()
                        .makeHttpRemoteUrl(
                            context.getHostUserLogin(), context.getOriginRepositoryName()));
          }
        }
        return factory;
      }
    };
  }
}
