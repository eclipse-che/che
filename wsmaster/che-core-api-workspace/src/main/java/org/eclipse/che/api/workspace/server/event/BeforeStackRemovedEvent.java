/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.workspace.server.event;

import org.eclipse.che.core.db.event.CascadeRemovalEvent;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;

/**
 * Pre-removal event of {@link StackImpl}.
 *
 * @author Max Shaposhnik
 */
public class BeforeStackRemovedEvent extends CascadeRemovalEvent {

    private final StackImpl stack;

    public BeforeStackRemovedEvent(StackImpl stack) {
        this.stack = stack;
    }

    public StackImpl getStack() {
        return stack;
    }
}
