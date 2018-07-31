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
package org.eclipse.che.api.workspace.shared;

import org.eclipse.che.api.core.model.project.ProjectProblem;

/** @author Vitalii Parfonov */
public class ProjectProblemImpl implements ProjectProblem {

  public ProjectProblemImpl(ProjectProblem projectProblem) {
    this(projectProblem.getCode(), projectProblem.getMessage());
  }

  public ProjectProblemImpl(int code, String message) {
    this.code = code;
    this.message = message;
  }

  int code;
  String message;

  @Override
  public int getCode() {
    return code;
  }

  @Override
  public String getMessage() {
    return message;
  }
}
