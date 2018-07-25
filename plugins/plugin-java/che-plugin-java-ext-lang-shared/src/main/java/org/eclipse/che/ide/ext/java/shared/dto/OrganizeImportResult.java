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
package org.eclipse.che.ide.ext.java.shared.dto;

import java.util.List;
import org.eclipse.che.dto.shared.DTO;

/** Result of Organize import request. */
@DTO
public interface OrganizeImportResult {

  /**
   * The list of organize imports conflicts.
   *
   * @return the organize import conflicts
   */
  List<ConflictImportDTO> getConflicts();

  void setConflicts(List<ConflictImportDTO> conflicts);

  /**
   * The changes that should be apply on organize imports
   *
   * @return the change list
   */
  List<Change> getChanges();

  void setChanges(List<Change> changes);
}
