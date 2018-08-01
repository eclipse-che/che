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
package org.eclipse.che.api.debug.shared.dto.action;

import org.eclipse.che.api.debug.shared.model.action.Action;
import org.eclipse.che.dto.shared.DTO;

/** @author Anatoliy Bazko */
@DTO
public interface ActionDto extends Action {
  Action.TYPE getType();

  void setType(Action.TYPE type);

  ActionDto withType(Action.TYPE type);
}
