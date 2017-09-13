/*
 * ******************************************************************************
 *  * Copyright (c) 2012-2017 Red Hat, Inc.
 *  * All rights reserved. This program and the accompanying materials
 *  * are made available under the terms of the Eclipse Public License v1.0
 *  * which accompanies this distribution, and is available at
 *  * http://www.eclipse.org/legal/epl-v10.html
 *  *
 *  * Contributors:
 *  *   Red Hat, Inc. - initial API and implementation
 *   ******************************************************************************
 */
package org.eclipse.che.api.git.shared;

import org.eclipse.che.dto.shared.DTO;

/**
 * Event for indicating that Git operation that can cause Git Status changes is called.
 *
 * @author Igor Vinokur.
 */
@DTO
public interface IndexChangedEvent extends GitEvent {
  /**
   * Returns actual Git status
   */
  Status getStatus();

  void setStatus(Status status);

  IndexChangedEvent withStatus(Status status);
}
