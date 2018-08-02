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
 * Event fired when command has been updated.
 *
 * <p><b>Note:</b> this event is not intended to be fired by clients.
 */
public class CommandUpdatedEvent extends GwtEvent<CommandUpdatedEvent.CommandUpdatedHandler> {

  /** Handler type. */
  private static Type<CommandUpdatedHandler> TYPE;

  /** Initial command. */
  private final CommandImpl initialCommand;
  /** Updated command. */
  private final CommandImpl updatedCommand;

  /**
   * Creates new event.
   *
   * <p><b>Note:</b> this event is not intended to be fired by clients.
   */
  public CommandUpdatedEvent(CommandImpl initialCommand, CommandImpl updatedCommand) {
    this.initialCommand = initialCommand;
    this.updatedCommand = updatedCommand;
  }

  /**
   * Gets the type associated with this event.
   *
   * @return returns the handler type
   */
  public static Type<CommandUpdatedHandler> getType() {
    return TYPE != null ? TYPE : (TYPE = new Type<>());
  }

  @Override
  public Type<CommandUpdatedHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(CommandUpdatedHandler handler) {
    handler.onCommandUpdated(this);
  }

  /** Returns initial command. */
  public CommandImpl getInitialCommand() {
    return initialCommand;
  }

  /** Returns updated command. */
  public CommandImpl getUpdatedCommand() {
    return updatedCommand;
  }

  /** Handler for {@link CommandUpdatedEvent}. */
  public interface CommandUpdatedHandler extends EventHandler {

    /**
     * Called when some command has been updated.
     *
     * @param event the event
     */
    void onCommandUpdated(CommandUpdatedEvent event);
  }
}
