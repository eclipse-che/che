/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
