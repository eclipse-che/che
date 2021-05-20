/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
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
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import org.eclipse.che.api.core.model.workspace.devfile.Project;
import org.eclipse.che.api.core.model.workspace.devfile.Source;

/** @author Sergii Leshchenko */
@Entity(name = "DevfileProject")
@Table(name = "devfile_project")
public class ProjectImpl implements Project {

  @Id
  @GeneratedValue
  @Column(name = "id")
  private Long id;

  @Column(name = "name", nullable = false)
  private String name;

  @Embedded private SourceImpl source;

  @Column(name = "clone_path", nullable = false)
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
    return Objects.equals(id, project.id)
        && Objects.equals(name, project.name)
        && Objects.equals(source, project.source)
        && Objects.equals(clonePath, project.clonePath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, source, clonePath);
  }

  @Override
  public String toString() {
    return "ProjectImpl{"
        + "id='"
        + id
        + '\''
        + ", name='"
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
