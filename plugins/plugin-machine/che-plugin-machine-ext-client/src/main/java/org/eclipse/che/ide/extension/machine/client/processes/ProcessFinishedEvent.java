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
package org.eclipse.che.ide.extension.machine.client.processes;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Dmitry Shnurenko
 */
public class ProcessFinishedEvent extends GwtEvent<ProcessFinishedEvent.Handler> {

    public interface Handler extends EventHandler {

        /** Implement this method to handle ProcessFinishedEvent. */
        void onProcessFinished(ProcessFinishedEvent event);

    }

    public static final Type<ProcessFinishedEvent.Handler> TYPE = new Type<>();

    private final int processID;

    public ProcessFinishedEvent(int processID) {
        this.processID = processID;
    }

    public int getProcessID() {
        return processID;
    }

    @Override
    public Type<ProcessFinishedEvent.Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onProcessFinished(this);
    }

}
