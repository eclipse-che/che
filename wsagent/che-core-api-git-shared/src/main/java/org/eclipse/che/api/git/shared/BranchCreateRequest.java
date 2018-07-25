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
 * Request to create new branch.
 *
 * @author andrew00x
 */
@DTO
public interface BranchCreateRequest {
  /** @return name of branch to be created */
  String getName();

  void setName(String name);

  BranchCreateRequest withName(String name);

  /** @return hash commit from which to start new branch. If <code>null</code> HEAD will be used */
  String getStartPoint();

  void setStartPoint(String startPoint);

  BranchCreateRequest withStartPoint(String startPoint);
}
