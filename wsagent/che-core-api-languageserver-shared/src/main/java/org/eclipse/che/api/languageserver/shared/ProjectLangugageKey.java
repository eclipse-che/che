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
package org.eclipse.che.api.languageserver.shared;

import java.util.Objects;

/** @author Anatoliy Bazko */
public class ProjectLangugageKey {
  public static final String ALL_PROJECT_MARKER = "*";

  private String project;
  private String languageId;

  private ProjectLangugageKey(String project, String languageId) {
    this.project = project;
    this.languageId = languageId;
  }

  public ProjectLangugageKey() {}

  public static ProjectLangugageKey createProjectKey(String project, String languageId) {
    return new ProjectLangugageKey(project, languageId);
  }

  public String getProject() {
    return project;
  }

  public void setProject(String project) {
    this.project = project;
  }

  public String getLanguageId() {
    return languageId;
  }

  public void setLanguageId(String languageId) {
    this.languageId = languageId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ProjectLangugageKey)) return false;
    ProjectLangugageKey that = (ProjectLangugageKey) o;
    return Objects.equals(languageId, that.languageId) && Objects.equals(project, that.project);
  }

  @Override
  public int hashCode() {
    return Objects.hash(languageId, project);
  }
}
