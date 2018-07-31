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
package org.eclipse.che.ide.selection;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.api.parts.ActivePartChangedEvent;
import org.eclipse.che.ide.api.parts.ActivePartChangedHandler;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PropertyListener;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.ide.api.selection.SelectionChangedEvent;

/**
 * Implements {@link SelectionAgent}
 *
 * @author Nikolay Zamosenchuk
 */
public class SelectionAgentImpl
    implements ActivePartChangedHandler, PropertyListener, SelectionAgent {

  private PartPresenter activePart;
  private final EventBus eventBus;

  @Inject
  public SelectionAgentImpl(EventBus eventBus) {
    this.eventBus = eventBus;
    // bind event listener
    eventBus.addHandler(ActivePartChangedEvent.TYPE, this);
  }

  /** {@inheritDoc} */
  @Override
  public Selection<?> getSelection() {
    return activePart != null ? activePart.getSelection() : null;
  }

  protected void notifySelectionChanged() {
    eventBus.fireEvent(new SelectionChangedEvent(getSelection()));
  }

  /** {@inheritDoc} */
  @Override
  public void onActivePartChanged(ActivePartChangedEvent event) {
    // remove listener from previous active part
    if (activePart != null) {
      activePart.removePropertyListener(this);
    }
    // set new active part
    activePart = event.getActivePart();
    if (activePart != null) {
      activePart.addPropertyListener(this);
    }
    notifySelectionChanged();
  }

  /** {@inheritDoc} */
  @Override
  public void propertyChanged(PartPresenter source, int propId) {
    // Check property and ensure came from active part
    if (propId == PartPresenter.SELECTION_PROPERTY && source == activePart) {
      notifySelectionChanged();
    }
  }
}
