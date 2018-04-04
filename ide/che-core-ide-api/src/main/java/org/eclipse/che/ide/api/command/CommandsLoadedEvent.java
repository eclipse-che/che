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
package org.eclipse.che.ide.api.command;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/** Event fired when all commands are loaded. */
public class CommandsLoadedEvent extends GwtEvent<CommandsLoadedEvent.CommandsLoadedHandler> {

  /** Handler type. */
  private static Type<CommandsLoadedHandler> TYPE;

  /**
   * Gets the type associated with this event.
   *
   * @return returns the handler type
   */
  public static Type<CommandsLoadedHandler> getType() {
    return TYPE != null ? TYPE : (TYPE = new Type<>());
  }

  @Override
  public Type<CommandsLoadedHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(CommandsLoadedHandler handler) {
    handler.onCommandsLoaded(this);
  }

  /** Handler for {@link CommandsLoadedEvent}. */
  public interface CommandsLoadedHandler extends EventHandler {

    /**
     * Called when all commands are loaded.
     *
     * @param event the event
     */
    void onCommandsLoaded(CommandsLoadedEvent event);
  }
}
