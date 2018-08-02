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

import java.util.Objects;
import org.eclipse.che.api.core.model.factory.Ide;
import org.eclipse.che.api.core.model.factory.OnAppClosed;
import org.eclipse.che.api.core.model.factory.OnAppLoaded;
import org.eclipse.che.api.core.model.factory.OnProjectsLoaded;

/** Data object for {@link Ide}. */
public class IdeImpl implements Ide {

  private OnAppLoadedImpl onAppLoaded;
  private OnProjectsLoadedImpl onProjectsLoaded;
  private OnAppClosedImpl onAppClosed;

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

  @Override
  public OnProjectsLoadedImpl getOnProjectsLoaded() {
    return onProjectsLoaded;
  }

  @Override
  public OnAppClosedImpl getOnAppClosed() {
    return onAppClosed;
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
    return Objects.equals(onAppLoaded, that.onAppLoaded)
        && Objects.equals(onProjectsLoaded, that.onProjectsLoaded)
        && Objects.equals(onAppClosed, that.onAppClosed);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(onAppLoaded);
    hash = 31 * hash + Objects.hashCode(onProjectsLoaded);
    hash = 31 * hash + Objects.hashCode(onAppClosed);
    return hash;
  }

  @Override
  public String toString() {
    return "IdeImpl{"
        + "onAppLoaded="
        + onAppLoaded
        + ", onProjectsLoaded="
        + onProjectsLoaded
        + ", onAppClosed="
        + onAppClosed
        + '}';
  }
}
