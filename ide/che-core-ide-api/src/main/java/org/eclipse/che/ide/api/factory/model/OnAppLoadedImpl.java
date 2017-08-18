/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
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
import org.eclipse.che.api.core.model.factory.OnAppLoaded;

/** Data object for {@link OnAppLoaded}. */
public class OnAppLoadedImpl implements OnAppLoaded {

  private List<ActionImpl> actions;

  public OnAppLoadedImpl(List<? extends Action> actions) {
    if (actions != null) {
      this.actions = actions.stream().map(ActionImpl::new).collect(toList());
    }
  }

  public OnAppLoadedImpl(OnAppLoaded onAppLoaded) {
    this(onAppLoaded.getActions());
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
    if (!(obj instanceof OnAppLoadedImpl)) {
      return false;
    }
    final OnAppLoadedImpl that = (OnAppLoadedImpl) obj;
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
    return "OnAppLoadedImpl{" + "actions=" + actions + '}';
  }
}
