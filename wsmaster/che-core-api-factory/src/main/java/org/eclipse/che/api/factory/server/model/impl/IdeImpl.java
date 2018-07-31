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
package org.eclipse.che.api.factory.server.model.impl;

import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.eclipse.che.api.core.model.factory.Ide;
import org.eclipse.che.api.core.model.factory.OnAppClosed;
import org.eclipse.che.api.core.model.factory.OnAppLoaded;
import org.eclipse.che.api.core.model.factory.OnProjectsLoaded;

/**
 * Data object for {@link Ide}.
 *
 * @author Anton Korneta
 */
@Entity(name = "Ide")
@Table(name = "che_factory_ide")
public class IdeImpl implements Ide {

  @Id
  @GeneratedValue
  @Column(name = "id")
  private Long id;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "on_app_loaded_id")
  private OnAppLoadedImpl onAppLoaded;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "on_projects_loaded_id")
  private OnProjectsLoadedImpl onProjectsLoaded;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "on_app_closed_id")
  private OnAppClosedImpl onAppClosed;

  public IdeImpl() {}

  public IdeImpl(
      OnAppLoaded onAppLoaded, OnProjectsLoaded onProjectsLoaded, OnAppClosed onAppClosed) {
    if (onAppLoaded != null) {
      this.onAppLoaded = new OnAppLoadedImpl(onAppLoaded);
    }
    if (onProjectsLoaded != null) {
      this.onProjectsLoaded = new OnProjectsLoadedImpl(onProjectsLoaded);
    }
    if (onAppClosed != null) {
      this.onAppClosed = new OnAppClosedImpl(onAppClosed);
    }
  }

  public IdeImpl(Ide ide) {
    this(ide.getOnAppLoaded(), ide.getOnProjectsLoaded(), ide.getOnAppClosed());
  }

  @Override
  public OnAppLoadedImpl getOnAppLoaded() {
    return onAppLoaded;
  }

  public void setOnAppLoaded(OnAppLoadedImpl onAppLoaded) {
    this.onAppLoaded = onAppLoaded;
  }

  @Override
  public OnProjectsLoadedImpl getOnProjectsLoaded() {
    return onProjectsLoaded;
  }

  public void setOnProjectsLoaded(OnProjectsLoadedImpl onProjectsLoaded) {
    this.onProjectsLoaded = onProjectsLoaded;
  }

  @Override
  public OnAppClosedImpl getOnAppClosed() {
    return onAppClosed;
  }

  public void setOnAppClosed(OnAppClosedImpl onAppClosed) {
    this.onAppClosed = onAppClosed;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof IdeImpl)) {
      return false;
    }
    final IdeImpl that = (IdeImpl) obj;
    return Objects.equals(id, that.id)
        && Objects.equals(onAppLoaded, that.onAppLoaded)
        && Objects.equals(onProjectsLoaded, that.onProjectsLoaded)
        && Objects.equals(onAppClosed, that.onAppClosed);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(id);
    hash = 31 * hash + Objects.hashCode(onAppLoaded);
    hash = 31 * hash + Objects.hashCode(onProjectsLoaded);
    hash = 31 * hash + Objects.hashCode(onAppClosed);
    return hash;
  }

  @Override
  public String toString() {
    return "IdeImpl{"
        + "id="
        + id
        + ", onAppLoaded="
        + onAppLoaded
        + ", onProjectsLoaded="
        + onProjectsLoaded
        + ", onAppClosed="
        + onAppClosed
        + '}';
  }
}
