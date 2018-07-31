/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.debug;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * The debugger provider.
 *
 * @author Andrey Plotnikov
 * @author Anatoliy Bazko
 */
@Singleton
public class DebuggerManager implements DebuggerManagerObservable {
  private Debugger activeDebugger;
  private Map<String, Debugger> debuggers;
  private List<DebuggerManagerObserver> observers;

  @Inject
  protected DebuggerManager() {
    this.debuggers = new HashMap<>();
    this.observers = new ArrayList<>();
  }

  /** Register new debugger for the given id. */
  public void registeredDebugger(String id, Debugger debugger) {
    debuggers.put(id, debugger);
  }

  /** Gets {@link Debugger} for the given id. */
  @Nullable
  public Debugger getDebugger(String id) {
    return debuggers.get(id);
  }

  /**
   * Sets new active debugger. Resubscribe all {@link DebuggerObserver} to listen to events from new
   * {@link Debugger}
   *
   * @param debugger debugger is being used
   */
  public void setActiveDebugger(@Nullable Debugger debugger) {
    if (activeDebugger != null) {
      for (DebuggerManagerObserver observer : observers) {
        activeDebugger.removeObserver(observer);
      }
    }

    activeDebugger = debugger;

    for (DebuggerManagerObserver observer : observers) {
      if (activeDebugger != null) {
        activeDebugger.addObserver(observer);
      }
      observer.onActiveDebuggerChanged(activeDebugger);
    }
  }

  /** @return {@link Debugger} is currently being used */
  @Nullable
  public Debugger getActiveDebugger() {
    return activeDebugger;
  }

  @Override
  public void addObserver(DebuggerManagerObserver observer) {
    observers.add(observer);
  }

  @Override
  public void removeObserver(DebuggerManagerObserver observer) {
    observers.remove(observer);
  }
}
