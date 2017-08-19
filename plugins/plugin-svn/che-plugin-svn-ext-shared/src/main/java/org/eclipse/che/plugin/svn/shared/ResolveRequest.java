/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.svn.shared;

import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.dto.shared.DTO;

@DTO
public interface ResolveRequest {

  /** @return the project path the request is associated with. */
  String getProjectPath();

  /** @param projectPath the project path to set */
  void setProjectPath(@NotNull final String projectPath);

  /** @param projectPath the project path to use */
  ResolveRequest withProjectPath(@NotNull final String projectPath);

  /** @return the depth the request is associated with */
  String getDepth();

  /** @param depth the depth to set */
  void setDepth(@NotNull final String depth);

  /**
   * @param depth the depth
   * @return the request
   */
  ResolveRequest withDepth(@NotNull final String depth);

  /** @return the paths the request is associated with */
  Map<String, String> getConflictResolutions();

  /** @param conflictResolutions */
  void setConflictResolutions(@NotNull final Map<String, String> conflictResolutions);

  /** @param conflictResolutions */
  ResolveRequest withConflictResolutions(@NotNull final Map<String, String> conflictResolutions);
}
