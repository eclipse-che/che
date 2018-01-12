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
