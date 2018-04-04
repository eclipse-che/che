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
package org.eclipse.che.api.debug.shared.dto.action;

import org.eclipse.che.api.debug.shared.model.SuspendPolicy;
import org.eclipse.che.api.debug.shared.model.action.StepOverAction;
import org.eclipse.che.dto.shared.DTO;

/** @author Anatoliy Bazko */
@DTO
public interface StepOverActionDto extends ActionDto, StepOverAction {
  TYPE getType();

  void setType(TYPE type);

  StepOverActionDto withType(TYPE type);

  @Override
  SuspendPolicy getSuspendPolicy();

  void setSuspendPolicy(SuspendPolicy suspendPolicy);

  StepOverActionDto withSuspendPolicy(SuspendPolicy suspendPolicy);
}
