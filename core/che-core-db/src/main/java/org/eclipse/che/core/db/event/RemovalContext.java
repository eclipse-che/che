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
package org.eclipse.che.core.db.event;

/**
 * Context that is used only for sharing the state of
 * the process cascading deleting among subscribers.
 *
 * @author Anton Korneta
 */
public class RemovalContext {
    private Exception cause;

    /**
     * Returns the cause which has changed the state of the context.
     */
    public Exception getCause() {
        return cause;
    }

    /**
     * Returns the state of the context.
     */
    public boolean isFailed() {
        return cause != null;
    }

    /**
     * Sets the context into failed state.
     */
    public RemovalContext fail(Exception cause) {
        this.cause = cause;
        return this;
    }
}
