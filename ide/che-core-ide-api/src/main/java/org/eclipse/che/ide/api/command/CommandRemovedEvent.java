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
package org.eclipse.che.ide.api.command;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when command has been removed.
 *
 * <p><b>Note:</b> this event is not intended to be fired by clients.
 */
public class CommandRemovedEvent extends GwtEvent<CommandRemovedEvent.CommandRemovedHandler> {

  /** Handler type. */
  private static Type<CommandRemovedHandler> TYPE;

  /** Removed command. */
  private final CommandImpl command;

  /**
   * Creates new event.
   *
   * <p><b>Note:</b> this event is not intended to be fired by clients.
   */
  public CommandRemovedEvent(CommandImpl command) {
    this.command = command;
  }

  /**
   * Gets the type associated with this event.
   *
   * @return returns the handler type
   */
  public static Type<CommandRemovedHandler> getType() {
    return TYPE != null ? TYPE : (TYPE = new Type<>());
  }

  @Override
  public Type<CommandRemovedHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(CommandRemovedHandler handler) {
    handler.onCommandRemoved(this);
  }

  /** Returns removed command. */
  public CommandImpl getCommand() {
    return command;
  }

  /** Handler for {@link CommandRemovedEvent}. */
  public interface CommandRemovedHandler extends EventHandler {

    /**
     * Called when command has been removed.
     *
     * @param event the event
     */
    void onCommandRemoved(CommandRemovedEvent event);
  }
}
