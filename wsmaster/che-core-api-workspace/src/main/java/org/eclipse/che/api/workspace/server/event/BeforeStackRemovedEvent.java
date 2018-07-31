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
package org.eclipse.che.api.workspace.server.event;

import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.core.db.cascade.event.RemoveEvent;

/**
 * Pre-removal event of {@link StackImpl}.
 *
 * @author Max Shaposhnik
 */
public class BeforeStackRemovedEvent extends RemoveEvent {

  private final StackImpl stack;

  public BeforeStackRemovedEvent(StackImpl stack) {
    this.stack = stack;
  }

  public StackImpl getStack() {
    return stack;
  }
}
