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
package org.eclipse.che.ide.api.command;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when new command has been added.
 *
 * <p><b>Note:</b> this event is not intended to be fired by clients.
 */
public class CommandAddedEvent extends GwtEvent<CommandAddedEvent.CommandAddedHandler> {

  /** Handler type. */
  private static Type<CommandAddedHandler> TYPE;

  /** Added command. */
  private final CommandImpl command;

  /**
   * Creates new event.
   *
   * <p><b>Note:</b> this event is not intended to be fired by clients.
   */
  public CommandAddedEvent(CommandImpl command) {
    this.command = command;
  }

  /**
   * Gets the type associated with this event.
   *
   * @return returns the handler type
   */
  public static Type<CommandAddedHandler> getType() {
    return TYPE != null ? TYPE : (TYPE = new Type<>());
  }

  @Override
  public Type<CommandAddedHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(CommandAddedHandler handler) {
    handler.onCommandAdded(this);
  }

  /** Returns added command. */
  public CommandImpl getCommand() {
    return command;
  }

  /** Handler for {@link CommandAddedEvent}. */
  public interface CommandAddedHandler extends EventHandler {

    /**
     * Called when new command has been added.
     *
     * @param event the event
     */
    void onCommandAdded(CommandAddedEvent event);
  }
}
