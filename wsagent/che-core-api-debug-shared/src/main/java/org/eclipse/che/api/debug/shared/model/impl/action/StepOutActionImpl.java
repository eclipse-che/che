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
package org.eclipse.che.api.debug.shared.model.impl.action;

import org.eclipse.che.api.debug.shared.model.SuspendPolicy;
import org.eclipse.che.api.debug.shared.model.action.Action;
import org.eclipse.che.api.debug.shared.model.action.StepOutAction;

/** @author Anatoliy Bazko */
public class StepOutActionImpl extends ActionImpl implements StepOutAction {
  private final SuspendPolicy suspendPolicy;

  public StepOutActionImpl(SuspendPolicy suspendPolicy) {
    super(Action.TYPE.STEP_OUT);
    this.suspendPolicy = suspendPolicy;
  }

  @Override
  public SuspendPolicy getSuspendPolicy() {
    return suspendPolicy;
  }
}
