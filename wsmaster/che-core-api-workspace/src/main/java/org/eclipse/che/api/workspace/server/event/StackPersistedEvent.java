/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
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
