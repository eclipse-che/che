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
package org.eclipse.che.api.git.shared;

import org.eclipse.che.dto.shared.DTO;

/**
 * Request to revert commit
 *
 * @author Dmitrii Bocharov (bdshadow)
 */
@DTO
public interface RevertRequest {

  /** @return the commit to return */
  String getCommit();

  /**
   * Set the commit to revert
   *
   * @param commit the commit to return
   */
  RevertRequest withCommit(String commit);

  void setCommit(String commit);
}
