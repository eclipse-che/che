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
package org.eclipse.che.ide.api.factory.model;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.core.model.factory.Action;
import org.eclipse.che.api.core.model.factory.OnProjectsLoaded;

/** Data object for {@link OnProjectsLoaded}. */
public class OnProjectsLoadedImpl implements OnProjectsLoaded {

  private List<ActionImpl> actions;

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
      return new ArrayList<>();
    }
    return actions;
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
    return getActions().equals(that.getActions());
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + getActions().hashCode();
    return hash;
  }

  @Override
  public String toString() {
    return "OnProjectsLoadedImpl{" + "actions=" + actions + '}';
  }
}
