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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The debugger provider.
 *
 * @author Andrey Plotnikov
 * @author Anatoliy Bazko
 */
@Singleton
public class DebuggerManager implements DebuggerManagerObservable {
    private Debugger                      activeDebugger;
    private Map<String, Debugger>         debuggers;
    private List<DebuggerManagerObserver> observers;

    @Inject
    protected DebuggerManager() {
        this.debuggers = new HashMap<>();
        this.observers = new ArrayList<>();
    }

    /**
     * Register new debugger for the given language.
     */
    public void registeredDebugger(String language, Debugger debugger) {
        debuggers.put(language, debugger);
    }

    /**
     * Gets {@link Debugger} for the given language.
     */
    @Nullable
    public Debugger getDebugger(String language) {
        return debuggers.get(language);
    }

    /**
     * Sets new active debugger.
     * Resubscriber all {@link DebuggerObserver} to listen to events from new {@link Debugger}
     *
     * @param debugger
     *         debugger is being used
     */
    public void setActiveDebugger(Debugger debugger) {
        if (activeDebugger != null) {
            for (DebuggerManagerObserver observer : observers) {
                activeDebugger.removeObserver(observer);
            }
        }

        activeDebugger = debugger;

        if (activeDebugger != null) {
            for (DebuggerManagerObserver observer : observers) {
                activeDebugger.addObserver(observer);
                observer.onActiveDebuggerChanged(activeDebugger);
            }
        }
    }

    /**
     * @return {@link Debugger} is currently being used
     */
    @Nullable
    public Debugger getActiveDebugger() {
        return activeDebugger;
    }

    // Active debugger events

    @Override
    public void addObserver(DebuggerManagerObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(DebuggerManagerObserver observer) {
        observers.remove(observer);
    }
}
