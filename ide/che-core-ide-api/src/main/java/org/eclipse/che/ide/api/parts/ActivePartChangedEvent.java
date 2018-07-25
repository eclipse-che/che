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
package org.eclipse.che.ide.api.parts;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event that notifies of changing active PartPresenter
 *
 * @author Nikolay Zamosenchuk
 */
public class ActivePartChangedEvent extends GwtEvent<ActivePartChangedHandler> {
  public static Type<ActivePartChangedHandler> TYPE = new Type<>();

  private final PartPresenter activePart;

  public ActivePartChangedEvent(PartPresenter activePart) {
    this.activePart = activePart;
  }

  @Override
  public Type<ActivePartChangedHandler> getAssociatedType() {
    return TYPE;
  }

  /** @return instance of Active Part */
  public PartPresenter getActivePart() {
    return activePart;
  }

  @Override
  protected void dispatch(ActivePartChangedHandler handler) {
    handler.onActivePartChanged(this);
  }
}
