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

import org.eclipse.che.api.core.notification.EventSubscriber;

/**
 * Receives notification about cascade removal events and
 * puts exceptions in the removal context.
 *
 * @author Anton Korneta
 */
public abstract class CascadeRemovalEventSubscriber<T extends CascadeRemovalEvent> implements EventSubscriber<T> {

    @Override
    public void onEvent(T event) {
        if (!event.getContext().isFailed()) {
            try {
                onRemovalEvent(event);
            } catch (Exception ex) {
                event.getContext().fail(ex);
            }
        }
    }

    /**
     * Receives notification about cascade removal event.
     */
    public abstract void onRemovalEvent(T event) throws Exception;
}
