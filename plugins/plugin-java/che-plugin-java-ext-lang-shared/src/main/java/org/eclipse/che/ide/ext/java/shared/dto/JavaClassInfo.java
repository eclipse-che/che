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
package org.eclipse.che.ide.ext.java.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * DTO represents the information about a java class.
 *
 * @author Roman Nikitenko
 */
@DTO
public interface JavaClassInfo {

  /** @return the FQN of java class */
  String getFQN();

  JavaClassInfo withFQN(String fqn);

  /** @return the project path */
  String getProjectPath();

  JavaClassInfo withProjectPath(String projectPath);
}
