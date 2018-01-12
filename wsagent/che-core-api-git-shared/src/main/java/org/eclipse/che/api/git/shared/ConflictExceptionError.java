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
