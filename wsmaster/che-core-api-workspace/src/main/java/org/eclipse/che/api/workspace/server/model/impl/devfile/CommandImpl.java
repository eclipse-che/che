/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
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

import static java.util.stream.Collectors.toCollection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.eclipse.che.api.core.model.workspace.devfile.Action;
import org.eclipse.che.api.core.model.workspace.devfile.Command;
import org.eclipse.che.api.core.model.workspace.devfile.PreviewUrl;

/** @author Sergii Leshchenko */
@Entity(name = "DevfileCommand")
@Table(name = "devfile_command")
public class CommandImpl implements Command {

  @Id
  @GeneratedValue
  @Column(name = "id")
  private Long id;

  @Column(name = "name", nullable = false)
  private String name;

  @Embedded private PreviewUrlImpl previewUrl;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinColumn(name = "devfile_command_id")
  private List<ActionImpl> actions;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
      name = "devfile_command_attributes",
      joinColumns = @JoinColumn(name = "devfile_command_id"))
  @MapKeyColumn(name = "name")
  @Column(name = "value", columnDefinition = "TEXT")
  private Map<String, String> attributes;

  public CommandImpl() {}

  public CommandImpl(
      String name,
      List<? extends Action> actions,
      Map<String, String> attributes,
      PreviewUrl previewUrl) {
    this.name = name;
    if (actions != null) {
      this.actions = actions.stream().map(ActionImpl::new).collect(toCollection(ArrayList::new));
    }
    if (attributes != null) {
      this.attributes = new HashMap<>(attributes);
    }
    if (previewUrl != null) {
      this.previewUrl = new PreviewUrlImpl(previewUrl.getPort(), previewUrl.getPath());
    }
  }

  public CommandImpl(Command command) {
    this(command.getName(), command.getActions(), command.getAttributes(), command.getPreviewUrl());
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public PreviewUrlImpl getPreviewUrl() {
    return previewUrl;
  }

  public void setPreviewUrl(PreviewUrlImpl previewUrl) {
    this.previewUrl = previewUrl;
  }

  @Override
  public List<ActionImpl> getActions() {
    if (actions == null) {
      actions = new ArrayList<>();
    }
    return actions;
  }

  public void setActions(List<ActionImpl> actions) {
    this.actions = actions;
  }

  @Override
  public Map<String, String> getAttributes() {
    if (attributes == null) {
      attributes = new HashMap<>();
    }
    return attributes;
  }

  public void setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", CommandImpl.class.getSimpleName() + "[", "]")
        .add("id=" + id)
        .add("name='" + name + "'")
        .add("previewURL=" + previewUrl)
        .add("actions=" + actions)
        .add("attributes=" + attributes)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CommandImpl command = (CommandImpl) o;
    return Objects.equals(id, command.id)
        && Objects.equals(name, command.name)
        && Objects.equals(previewUrl, command.previewUrl)
        && Objects.equals(actions, command.actions)
        && Objects.equals(attributes, command.attributes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, previewUrl, actions, attributes);
  }
}
