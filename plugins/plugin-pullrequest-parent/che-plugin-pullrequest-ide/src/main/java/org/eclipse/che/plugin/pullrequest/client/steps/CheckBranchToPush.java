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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.plugin.pullrequest.client.ContributeMessages;
import org.eclipse.che.plugin.pullrequest.client.workflow.Context;
import org.eclipse.che.plugin.pullrequest.client.workflow.Step;
import org.eclipse.che.plugin.pullrequest.client.workflow.WorkflowExecutor;

/**
 * Checks that working branch is different from the cloned branch.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class CheckBranchToPush implements Step {

  private final ContributeMessages messages;

  @Inject
  public CheckBranchToPush(final ContributeMessages messages) {
    this.messages = messages;
  }

  @Override
  public void execute(final WorkflowExecutor executor, final Context context) {
    if (context.getWorkBranchName().equals(context.getContributeToBranchName())) {
      executor.fail(this, context, messages.stepCheckBranchClonedBranchIsEqualToWorkBranch());
    } else {
      executor.done(this, context);
    }
  }
}
