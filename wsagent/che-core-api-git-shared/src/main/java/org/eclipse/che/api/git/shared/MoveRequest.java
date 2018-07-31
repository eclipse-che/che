/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.git.shared;

import org.eclipse.che.dto.shared.DTO;

/**
 * Request to move or rename a file or directory.
 *
 * @author andrew00x
 */
@DTO
public interface MoveRequest {
  /** @return source */
  String getSource();

  void setSource(String source);

  /** @return target */
  String getTarget();

  void setTarget(String target);
}
