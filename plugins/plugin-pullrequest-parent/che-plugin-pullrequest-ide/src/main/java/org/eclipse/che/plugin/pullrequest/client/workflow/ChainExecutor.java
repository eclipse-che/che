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

import static com.google.common.base.Optional.fromNullable;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Optional;
import java.util.Iterator;
import org.eclipse.che.ide.util.loging.Log;

/**
 * Executor for the {@link StepsChain}. If the chain is modified after executor is created (e.g. new
 * step added to the chain) executor state won't be affected and newly added steps will be ignored
 * by executor.
 *
 * @author Yevhenii Voevodin
 */
public final class ChainExecutor {

  private final Iterator<Step> chainIt;

  private Step currentStep;

  public ChainExecutor(final StepsChain chain) {
    chainIt = requireNonNull(chain, "Expected non-null steps chain").getSteps().iterator();
  }

  /**
   * Executes the next chain step, does nothing - if there are no steps left .
   *
   * @param workflow the contribution workflow
   * @param context the context for current chain execution
   */
  public void execute(final WorkflowExecutor workflow, final Context context) {
    if (chainIt.hasNext()) {
      currentStep = chainIt.next();
      Log.debug(
          getClass(),
          "Executing :: " + context.getProject().getName() + " ::  =>  " + currentStep.getClass());
      currentStep.execute(workflow, context);
    }
  }

  /**
   * Returns an empty optional when current step is null, otherwise returns the optional which
   * contains current step value.
   */
  public Optional<Step> getCurrentStep() {
    return fromNullable(currentStep);
  }
}
