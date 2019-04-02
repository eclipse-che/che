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
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
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

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "source_id")
  private SourceImpl source;

  public ProjectImpl() {}

  public ProjectImpl(String name, Source source) {
    this.name = name;
    if (source != null) {
      this.source = new SourceImpl(source);
    }
  }

  public ProjectImpl(Project project) {
    this(project.getName(), project.getSource());
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
        && Objects.equals(source, project.source);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName(), getSource());
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
        + '}';
  }
}
