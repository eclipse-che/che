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
      actions = new ArrayList<>();
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
