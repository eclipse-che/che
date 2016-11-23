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
 * Special event type which is needed only for
 * notification in the process of cascade removing.
 *
 * @author Anton Korneta
 */
public abstract class CascadeRemovalEvent {
    private final RemovalContext context = new RemovalContext();

    public RemovalContext getContext() {
        return context;
    }
}
