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
package org.eclipse.che.api.workspace.server.model.impl.devfile;

import java.util.Objects;
import org.eclipse.che.api.core.model.workspace.devfile.Project;
import org.eclipse.che.api.core.model.workspace.devfile.Source;

/** @author Sergii Leshchenko */
public class ProjectImpl implements Project {

  private String name;
  private SourceImpl source;
  private String clonePath;

  public ProjectImpl() {}

  public ProjectImpl(String name, Source source, String clonePath) {
    this.name = name;
    if (source != null) {
      this.source = new SourceImpl(source);
    }
    this.clonePath = clonePath;
  }

  public ProjectImpl(Project project) {
    this(project.getName(), project.getSource(), project.getClonePath());
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public SourceImpl getSource() {
    return source;
  }

  public void setSource(SourceImpl source) {
    this.source = source;
  }

  @Override
  public String getClonePath() {
    return clonePath;
  }

  public void setClonePath(String clonePath) {
    this.clonePath = clonePath;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ProjectImpl)) {
      return false;
    }
    ProjectImpl project = (ProjectImpl) o;
    return Objects.equals(getName(), project.getName())
        && Objects.equals(getSource(), project.getSource())
        && Objects.equals(getClonePath(), project.getClonePath());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName(), getSource(), getClonePath());
  }

  @Override
  public String toString() {
    return "ProjectImpl{"
        + "name='"
        + name
        + '\''
        + ", source="
        + source
        + ", clonePath='"
        + clonePath
        + '\''
        + '}';
  }
}
