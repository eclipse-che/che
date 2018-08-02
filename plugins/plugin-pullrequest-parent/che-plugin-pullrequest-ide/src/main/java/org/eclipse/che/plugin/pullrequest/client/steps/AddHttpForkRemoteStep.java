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

import com.google.inject.Singleton;
import javax.inject.Inject;
import org.eclipse.che.plugin.pullrequest.client.workflow.Context;
import org.eclipse.che.plugin.pullrequest.client.workflow.Step;
import org.eclipse.che.plugin.pullrequest.client.workflow.WorkflowExecutor;

/**
 * Add HTTP fork remote URL to repository.
 *
 * @author Mihail Kuznyetsov
 */
@Singleton
public class AddHttpForkRemoteStep implements Step {
  private final AddForkRemoteStepFactory addForkRemoteStepFactory;

  @Inject
  public AddHttpForkRemoteStep(AddForkRemoteStepFactory addForkRemoteStepFactory) {
    this.addForkRemoteStepFactory = addForkRemoteStepFactory;
  }

  @Override
  public void execute(final WorkflowExecutor executor, final Context context) {
    String remoteUrl =
        context
            .getVcsHostingService()
            .makeHttpRemoteUrl(context.getHostUserLogin(), context.getForkedRepositoryName());
    addForkRemoteStepFactory.create(this, remoteUrl).execute(executor, context);
  }
}
