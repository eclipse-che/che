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
package org.eclipse.che.api.devfile.server.model.impl;

import com.google.common.annotations.Beta;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import org.eclipse.che.api.core.model.workspace.devfile.Command;
import org.eclipse.che.api.core.model.workspace.devfile.Component;
import org.eclipse.che.api.core.model.workspace.devfile.Devfile;
import org.eclipse.che.api.core.model.workspace.devfile.Metadata;
import org.eclipse.che.api.core.model.workspace.devfile.Project;
import org.eclipse.che.api.core.model.workspace.devfile.UserDevfile;
import org.eclipse.che.api.workspace.server.model.impl.devfile.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.MetadataImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ProjectImpl;

@Entity(name = "UserDevfile")
@Table(name = "userdevfile")
@NamedQueries({
  @NamedQuery(name = "UserDevfile.getAll", query = "SELECT d FROM UserDevfile d ORDER BY d.id"),
  @NamedQuery(name = "UserDevfile.getAllCount", query = "SELECT COUNT(d) FROM UserDevfile d"),
})
@Beta
public class UserDevfileImpl implements UserDevfile {

  @Id
  @Column(name = "id")
  private String id;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "devfile_id")
  private DevfileImpl devfile;

  @Column(name = "generated_name")
  private String generateName;

  public UserDevfileImpl() {}

  public UserDevfileImpl(String id, Devfile devfile) {
    this.devfile = new DevfileImpl(devfile);
    this.id = id;
  }

  public UserDevfileImpl(UserDevfileImpl userDevfile) {
    this(userDevfile.id, userDevfile.devfile);
  }

  public UserDevfileImpl(UserDevfile userDevfile) {
    this(userDevfile.getId(), new DevfileImpl(userDevfile));
  }

  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String getApiVersion() {
    return devfile.getApiVersion();
  }

  @Override
  public List<? extends Project> getProjects() {
    return devfile.getProjects();
  }

  @Override
  public List<? extends Component> getComponents() {
    return devfile.getComponents();
  }

  @Override
  public List<? extends Command> getCommands() {
    return devfile.getCommands();
  }

  @Override
  public Map<String, String> getAttributes() {
    return devfile.getAttributes();
  }

  @Override
  public Metadata getMetadata() {
    return devfile.getMetadata();
  }

  public void setApiVersion(String apiVersion) {
    devfile.setApiVersion(apiVersion);
  }

  public void setName(String name) {
    devfile.setName(name);
  }

  public void setProjects(List<ProjectImpl> projects) {
    devfile.setProjects(projects);
  }

  public void setComponents(List<ComponentImpl> components) {
    devfile.setComponents(components);
  }

  public void setCommands(List<CommandImpl> commands) {
    devfile.setCommands(commands);
  }

  public void setAttributes(Map<String, String> attributes) {
    devfile.setAttributes(attributes);
  }

  public void setMetadata(MetadataImpl metadata) {
    devfile.setMetadata(metadata);
  }

  @PostLoad
  public void postLoad() {
    devfile.getMetadata().setGenerateName(generateName);
  }

  @PreUpdate
  @PrePersist
  public void beforeDb() {
    generateName = devfile.getMetadata().getGenerateName();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UserDevfileImpl that = (UserDevfileImpl) o;
    return Objects.equals(id, that.id) && Objects.equals(devfile, that.devfile);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, devfile);
  }

  @Override
  public String toString() {
    return "UserDevfileImpl{" + "id='" + id + '\'' + ", devfile=" + devfile + '}';
  }
}
