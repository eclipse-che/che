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
package org.eclipse.che.api.factory.server.model.impl;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.eclipse.che.api.core.model.factory.Action;
import org.eclipse.che.api.core.model.factory.OnProjectsLoaded;

/**
 * Data object for {@link OnProjectsLoaded}.
 *
 * @author Anton Korneta
 */
@Entity(name = "OnProjectsLoaded")
@Table(name = "che_factory_on_projects_loaded_action")
public class OnProjectsLoadedImpl implements OnProjectsLoaded {

  @Id
  @GeneratedValue
  @Column(name = "id")
  private Long id;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinTable(
      name = "che_factory_on_projects_loaded_action_value",
      joinColumns = @JoinColumn(name = "on_projects_loaded_id"),
      inverseJoinColumns = @JoinColumn(name = "action_entity_id"))
  private List<ActionImpl> actions;

  public OnProjectsLoadedImpl() {}

  public OnProjectsLoadedImpl(List<? extends Action> actions) {
    if (actions != null) {
      this.actions = actions.stream().map(ActionImpl::new).collect(toList());
    }
  }

  public OnProjectsLoadedImpl(OnProjectsLoaded onProjectsLoaded) {
    this(onProjectsLoaded.getActions());
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
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof OnProjectsLoadedImpl)) {
      return false;
    }
    final OnProjectsLoadedImpl that = (OnProjectsLoadedImpl) obj;
    return Objects.equals(id, that.id) && getActions().equals(that.getActions());
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(id);
    hash = 31 * hash + getActions().hashCode();
    return hash;
  }

  @Override
  public String toString() {
    return "OnProjectsLoadedImpl{" + "id=" + id + ", actions=" + actions + '}';
  }
}
