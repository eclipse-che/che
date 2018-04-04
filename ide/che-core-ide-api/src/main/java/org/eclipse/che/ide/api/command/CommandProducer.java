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

/**
 * Defines the requirements for a component which can produce the commands from the current context.
 *
 * <p>For every registered {@link CommandProducer} an appropriate action will be added in context
 * menus (e.g., explorer, editor tab).
 *
 * <p>Implementations of this interface have to be registered with a GIN multibinder in order to be
 * picked-up on application's start-up.
 *
 * @author Artem Zatsarynnyi
 */
public interface CommandProducer {

  /** Returns the text that should be used as related action's title. */
  String getName();

  /**
   * Whether the command produced by concrete producer is applicable to the current context?
   * Returned value is used for regulating visibility of an appropriate action.
   */
  boolean isApplicable();

  /** Creates a command from the current context. Called when user performs corresponded action. */
  CommandImpl createCommand();
}
