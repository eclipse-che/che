/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
