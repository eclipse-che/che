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
package org.eclipse.che.core.db.cascade;

import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.core.db.cascade.event.CascadeEvent;

/**
 * Receives events and puts exceptions in the context
 * to perform rollback operation if it is necessary.
 *
 * @author Anton Korneta
 * @author Sergii Leschenko
 */
public abstract class CascadeEventSubscriber<T extends CascadeEvent> implements EventSubscriber<T> {
    @Override
    public void onEvent(T event) {
        if (!event.getContext().isFailed()) {
            try {
                onCascadeEvent(event);
            } catch (Exception ex) {
                event.getContext().fail(ex);
            }
        }
    }

    /**
     * Receives notification about cascade event.
     *
     * <p>If the method throws an exception it will be set to context
     * to break event publishing and rethrow exception.
     * Event is responsible for rethrowing or wrapping original exception.
     *
     * @see CascadeEvent#propagateException()
     */
    public abstract void onCascadeEvent(T event) throws Exception;
}
