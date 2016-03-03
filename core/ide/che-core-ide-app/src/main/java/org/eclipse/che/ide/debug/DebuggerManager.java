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
package org.eclipse.che.ide.debug;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;

import java.util.HashMap;
import java.util.Map;

/**
 * The debugger provider.
 *
 * @author Andrey Plotnikov
 * @author Anatoliy Bazko
 */
@Singleton
public class DebuggerManager {
    private final AppContext appContext;

    private Map<String, Debugger> debuggers;
    private Debugger debugger;

    @Inject
    protected DebuggerManager(AppContext appContext) {
        this.appContext = appContext;
        this.debuggers = new HashMap<>();
    }

    /**
     * Register new debugger for the specified project type ID.
     * TODO: don't link debugger with project.
     *
     * @param projectTypeId
     * @param debugger
     */
    public void registeredDebugger(String projectTypeId, Debugger debugger) {
        debuggers.put(projectTypeId, debugger);
        setDebugger(debugger);
    }

    public void setDebugger(Debugger debugger) {
        this.debugger = debugger;
    }

    @Nullable
    public Debugger getDebugger() {
        return debugger;
    }
}