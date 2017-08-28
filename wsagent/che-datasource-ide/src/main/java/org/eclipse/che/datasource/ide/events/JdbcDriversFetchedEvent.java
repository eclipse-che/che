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
package org.eclipse.che.datasource.ide.events;

import com.google.gwt.event.shared.GwtEvent;

import java.util.List;

public class JdbcDriversFetchedEvent extends GwtEvent<JdbcDriversFetchedEventHandler> {

    private static Type<JdbcDriversFetchedEventHandler> TYPE;

    protected List<String>                              drivers;

    public JdbcDriversFetchedEvent(List<String> drivers) {
        this.drivers = drivers;
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<JdbcDriversFetchedEventHandler> getAssociatedType() {
        if (TYPE == null) {
            TYPE = new GwtEvent.Type<JdbcDriversFetchedEventHandler>();
        }
        return TYPE;
    }

    @Override
    protected void dispatch(JdbcDriversFetchedEventHandler handler) {
        handler.onJdbcDriversFetched(drivers);
    }

    public static com.google.gwt.event.shared.GwtEvent.Type<JdbcDriversFetchedEventHandler> getType() {
        if (TYPE == null) {
            TYPE = new GwtEvent.Type<JdbcDriversFetchedEventHandler>();
        }
        return TYPE;
    }
}
