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
package org.eclipse.che.ide.ext.java.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event that describes the fact that dependencies were updated.
 *
 * @author Alexander Andrienko
 */
public class DependencyUpdatedEvent extends GwtEvent<DependencyUpdatedEventHandler> {

    public static final Type<DependencyUpdatedEventHandler> TYPE = new Type<>();

    @Override
    public Type<DependencyUpdatedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(DependencyUpdatedEventHandler handler) {
        handler.onDependencyUpdated();
    }
}
