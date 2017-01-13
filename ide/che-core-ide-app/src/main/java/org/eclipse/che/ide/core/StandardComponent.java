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
package org.eclipse.che.ide.core;

import org.eclipse.che.ide.api.component.Component;
import org.eclipse.che.ide.util.loging.Log;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.Scheduler;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class StandardComponent implements Component {

    private final StandardComponentInitializer initializer;

    @Inject
    public StandardComponent(StandardComponentInitializer initializer) {
        this.initializer = initializer;
    }

    @Override
    public void start(final Callback<Component, Exception> callback) {
        // initialize standard components
        try {
            initializer.initialize();
        } catch (Exception e) {
            Log.error(StandardComponent.class, e);
        }

        // Finalization of starting components
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                callback.onSuccess(StandardComponent.this);
            }
        });
    }
}
