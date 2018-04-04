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
