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
package org.eclipse.che.plugin.pullrequest.client.workflow;

import org.eclipse.che.plugin.pullrequest.client.events.StepEvent;
import org.eclipse.che.plugin.pullrequest.client.steps.PushBranchStep;

/**
 * Contract for a step in the contribution workflow.
 *
 * <p>Step should not depend on another steps as it is a workflow part but workflow is defined by
 * {@link ContributionWorkflow} implementations. Each step should end its execution with either
 * {@link WorkflowExecutor#done(Step, Context)} or {@link WorkflowExecutor#fail(Step, Context,
 * String)} method.
 *
 * <p>{@link WorkflowExecutor} fires {@link StepEvent} for each done/fail step if this step is not
 * {@link SyntheticStep}.
 *
 * <p>If step contains common logic for several steps then this logic should be either extracted to
 * the other step or used along with factory (e.g. {@link PushBranchStep}).
 *
 * @author Yevhenii Voevodin
 */
public interface Step {

  /**
   * Executes this step.
   *
   * @param executor contribution workflow executor
   */
  void execute(final WorkflowExecutor executor, final Context context);
}
