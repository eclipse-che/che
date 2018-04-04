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
package org.eclipse.che.plugin.pullrequest.client.dialogs.paste;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.TextBox;

/** {@link TextBox} that handles onpaste events. */
public class PasteAwareTextBox extends TextBox {

  public PasteAwareTextBox() {
    sinkEvents(Event.ONPASTE);
  }

  public PasteAwareTextBox(final Element element) {
    super(element);
    sinkEvents(Event.ONPASTE);
  }

  @Override
  public void onBrowserEvent(final Event event) {
    super.onBrowserEvent(event);
    switch (event.getTypeInt()) {
      case Event.ONPASTE:
        event.stopPropagation();
        delayedFireEvent();
        break;
      default:
        break;
    }
  }

  /** Fires an event, after waiting the state of the textbox the be updated. */
  private void delayedFireEvent() {
    Scheduler.get()
        .scheduleDeferred(
            new ScheduledCommand() {
              @Override
              public void execute() {
                fireEvent(new PasteEvent());
              }
            });
  }

  /**
   * Adds a {@link PasteHandler} to the component.
   *
   * @param handler the handler to add
   * @return a registration object for removal
   */
  public HandlerRegistration addPasteHandler(final PasteHandler handler) {
    return addHandler(handler, PasteEvent.TYPE);
  }
}
