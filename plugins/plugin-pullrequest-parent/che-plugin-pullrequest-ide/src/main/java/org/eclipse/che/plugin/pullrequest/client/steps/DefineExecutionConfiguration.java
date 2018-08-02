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

import org.eclipse.che.plugin.pullrequest.client.workflow.Context;
import org.eclipse.che.plugin.pullrequest.client.workflow.Step;
import org.eclipse.che.plugin.pullrequest.client.workflow.WorkflowExecutor;

/**
 * This step defines ability to create forks.
 *
 * @author Mihail Kuznyetsov
 */
public class DefineExecutionConfiguration implements Step {

  @Override
  public void execute(WorkflowExecutor executor, Context context) {
    context.setForkAvailable(
        !context.getOriginRepositoryOwner().equals(context.getHostUserLogin()));
    executor.done(this, context);
  }
}
