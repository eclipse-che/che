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
package org.eclipse.che.ide.extension.machine.client.processes.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Dmitry Shnurenko
 */
public class ProcessFinishedEvent extends GwtEvent<ProcessFinishedHandler> {

    public static final Type<ProcessFinishedHandler> TYPE = new Type<>();

    private final String processID;

    public ProcessFinishedEvent(String processID) {
        this.processID = processID;
    }

    public String getProcessID() {
        return processID;
    }

    @Override
    public Type<ProcessFinishedHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ProcessFinishedHandler handler) {
        handler.onProcessFinished(this);
    }

}
