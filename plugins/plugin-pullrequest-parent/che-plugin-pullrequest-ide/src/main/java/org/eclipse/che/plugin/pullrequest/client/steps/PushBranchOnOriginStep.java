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
 * Push the local contribution branch to origin repository
 *
 * @author Mihail Kuznyetsov
 */
@Singleton
public class PushBranchOnOriginStep implements Step {

  private static final String ORIGIN_REMOTE_NAME = "origin";

  private final PushBranchStepFactory pushBranchStepFactory;

  @Inject
  public PushBranchOnOriginStep(PushBranchStepFactory pushBranchStepFactory) {
    this.pushBranchStepFactory = pushBranchStepFactory;
  }

  @Override
  public void execute(final WorkflowExecutor executor, final Context context) {
    context.setForkedRemoteName(ORIGIN_REMOTE_NAME);
    pushBranchStepFactory
        .create(this, context.getOriginRepositoryOwner(), context.getOriginRepositoryName())
        .execute(executor, context);
  }
}
