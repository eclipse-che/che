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
package org.eclipse.che.api.workspace.server.event;

import org.eclipse.che.api.core.notification.EventOrigin;
import org.eclipse.che.api.workspace.shared.stack.Stack;
import org.eclipse.che.core.db.cascade.event.PersistEvent;

/**
 * Published after stack instance is persisted.
 *
 * @author Yevhenii Voevodin
 */
@EventOrigin("stack")
public class StackPersistedEvent extends PersistEvent {

  private final Stack stack;

  public StackPersistedEvent(Stack stack) {
    this.stack = stack;
  }

  public Stack getStack() {
    return stack;
  }
}
