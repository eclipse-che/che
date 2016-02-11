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
import org.eclipse.che.ide.api.app.CurrentProject;

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

    @Inject
    protected DebuggerManager(AppContext appContext) {
        this.appContext = appContext;
        this.debuggers = new HashMap<>();
    }

    /**
     * Register new debugger for the specified project type ID.
     *
     * @param projectTypeId
     * @param debugger
     */
    public void registeredDebugger(String projectTypeId, Debugger debugger) {
        debuggers.put(projectTypeId, debugger);
    }

    /**
     * @return debugger for current project type or null otherwise
     */
    @Nullable
    public Debugger getDebugger() {
        CurrentProject currentProject = appContext.getCurrentProject();
        if (currentProject != null) {
            String projectTypeId = currentProject.getProjectConfig().getType();
            return debuggers.get(projectTypeId);
        }

        return null;
    }
}