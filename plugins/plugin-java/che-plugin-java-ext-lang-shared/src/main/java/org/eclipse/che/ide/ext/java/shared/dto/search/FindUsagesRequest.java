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
package org.eclipse.che.ide.ext.java.shared.dto.search;

import org.eclipse.che.dto.shared.DTO;

/**
 * DTO for requesting find usage search.
 *
 * @author Evgen Vidolob
 */
@DTO
public interface FindUsagesRequest {

  /** @return the project path */
  String getProjectPath();

  /**
   * Set project path
   *
   * @param projectPath the path of the project
   */
  void setProjectPath(String projectPath);

  /** @return the path of the file where search invoked */
  String getFQN();

  /**
   * Set java class FQN.
   *
   * @param fqn the FQN where search invoked
   */
  void setFQN(String fqn);

  /** @return the offset (cursor position) in file */
  int getOffset();

  /**
   * Set offset (cursor position)
   *
   * @param offset the cursor position in file
   */
  void setOffset(int offset);
}
