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
package org.eclipse.che.plugin.maven.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * Data object for <code>MavenProjectProblem</code>
 *
 * @author Evgen Vidolob
 */
@DTO
public interface MavenProblem {

  String getPomPath();

  void setPomPath(String pomPath);

  String getDescription();

  void setDescription(String description);

  ProblemType getProblemType();

  void setProblemType(ProblemType problemType);

  enum ProblemType {
    DEPENDENCY,
    PARENT,
    SYNTAX,
    STRUCTURE,
    SETTINGS
  }
}
