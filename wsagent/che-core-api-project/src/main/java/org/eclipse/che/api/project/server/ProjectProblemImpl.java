package org.eclipse.che.api.project.server;

import org.eclipse.che.api.core.model.project.ProjectProblem;

/** @author Vitalii Parfonov */
public class ProjectProblemImpl implements ProjectProblem {

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
