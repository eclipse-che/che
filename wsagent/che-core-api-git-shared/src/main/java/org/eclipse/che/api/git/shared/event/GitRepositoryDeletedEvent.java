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
package org.eclipse.che.api.git.shared.event;

import org.eclipse.che.dto.shared.DTO;

/**
 * Event for indicating that Git repository is deleted.
 *
 * @author Igor Vinokur.
 */
@DTO
public interface GitRepositoryDeletedEvent extends GitEvent {
  @Override
  String getProjectName();

  void setProjectName(String projectName);

  GitRepositoryDeletedEvent withProjectName(String projectName);

  void setProjectPath(String projectPath);

  String getProjectPath();

  GitRepositoryDeletedEvent withProjectPath(String projectPath);
}
