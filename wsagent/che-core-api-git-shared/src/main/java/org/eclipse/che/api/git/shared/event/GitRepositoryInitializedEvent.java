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
package org.eclipse.che.api.git.shared.event;

import org.eclipse.che.dto.shared.DTO;

/**
 * Event for indicating that Git repository initialized.
 *
 * @author Igor Vinokur
 * @author Mykola Morhun
 */
@DTO
public interface GitRepositoryInitializedEvent extends GitEvent {
  @Override
  String getProjectName();

  void setProjectName(String projectName);

  GitRepositoryInitializedEvent withProjectName(String projectName);
}
