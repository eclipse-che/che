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

import java.util.List;
import org.eclipse.che.dto.shared.DTO;

/**
 * Conflict Exception Error
 *
 * @author Yossi Balan (yossi.balan@sap.com)
 */
@DTO
public interface ConflictExceptionError {

  /**
   * Get conflict paths.
   *
   * @return conflict paths
   */
  List<String> getConflictingPaths();

  ConflictExceptionError withConflictingPaths(List<String> conflictPaths);

  /**
   * Set conflict paths.
   *
   * @param conflictPaths conflict paths
   */
  void setConflictingPaths(List<String> conflictPaths);

  /**
   * Get error message.
   *
   * @return error message
   */
  String getMessage();

  ConflictExceptionError withMessage(String message);

  /**
   * Set error message.
   *
   * @param message error message
   */
  void setMessage(String message);
}
