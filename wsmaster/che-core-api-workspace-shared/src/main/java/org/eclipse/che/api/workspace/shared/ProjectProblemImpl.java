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
