// Copyright 2012 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.eclipse.che.ide.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight manager for listeners that's designed to reduce boilerplate in classes that have
 * listeners.
 *
 * <p>The client stores a final member for this class, and exposes a {@code getListenerRegistrar()}
 * with return type {@link ListenerRegistrar}. To dispatch, the client calls {@link
 * #dispatch(Dispatcher)} with a custom dispatcher. (If the dispatches are frequent, consider
 * keeping a single dispatcher instance whose state you set prior to passing it to the dispatch
 * method.)
 *
 * @param <L> the type of the listener
 */
public class ListenerManager<L> implements ListenerRegistrar<L> {

  /**
   * Dispatches to a listener.
   *
   * @param <L> the type of the listener
   */
  public interface Dispatcher<L> {
    void dispatch(L listener);
  }

  /** Listener that is notified when clients are added or removed from this manager. */
  public interface RegistrationListener<L> {
    void onListenerAdded(L listener);

    void onListenerRemoved(L listener);
  }

  public static <L> ListenerManager<L> create() {
    return new ListenerManager<L>(null);
  }

  public static <L> ListenerManager<L> create(RegistrationListener<L> registrationListener) {
    return new ListenerManager<L>(registrationListener);
  }

  private boolean isDispatching;

  private final List<L> listeners;

  /** Listeners that were added during a dispatch */
  private final List<L> queuedListenerAdditions;

  /** Listeners that were removed during a dispatch */
  private final List<L> queuedListenerRemovals;

  private final RegistrationListener<L> registrationListener;

  private ListenerManager(RegistrationListener<L> registrationListener) {
    this.listeners = new ArrayList<>();
    this.queuedListenerAdditions = new ArrayList<>();
    this.queuedListenerRemovals = new ArrayList<>();
    this.registrationListener = registrationListener;
  }

  /** Adds a new listener to this event. */
  @Override
  public Remover add(final L listener) {
    if (!isDispatching) {
      addListenerImpl(listener);
    } else {
      if (!queuedListenerRemovals.remove(listener)) {
        queuedListenerAdditions.add(listener);
      }
    }

    return new Remover() {
      @Override
      public void remove() {
        ListenerManager.this.remove(listener);
      }
    };
  }

  /** Dispatches this event to all listeners. */
  public void dispatch(final Dispatcher<L> dispatcher) {
    isDispatching = true;
    try {
      for (int i = 0, n = listeners.size(); i < n; i++) {
        dispatcher.dispatch(listeners.get(i));
      }
    } finally {
      isDispatching = false;
      addQueuedListeners();
      removeQueuedListeners();
    }
  }

  /**
   * Removes a listener from this manager.
   *
   * <p>It is strongly preferred that you use the {@link ListenerRegistrar.Remover} returned by
   * {@link #add(Object)} instead of calling this method directly.
   */
  @Override
  public void remove(L listener) {
    if (!isDispatching) {
      removeListenerImpl(listener);
    } else {
      if (!queuedListenerAdditions.remove(listener)) {
        queuedListenerRemovals.add(listener);
      }
    }
  }

  /**
   * Returns the number of listeners registered on this manager. This does not include those
   * listeners that are queued to be added and it does include those listeners that are queued to be
   * removed.
   */
  public int getCount() {
    return listeners.size();
  }

  /** Returns true if the listener manager is currently dispatching to listeners. */
  public boolean isDispatching() {
    return isDispatching;
  }

  private void addQueuedListeners() {
    for (int i = 0, n = queuedListenerAdditions.size(); i < n; i++) {
      addListenerImpl(queuedListenerAdditions.get(i));
    }
    queuedListenerAdditions.clear();
  }

  private void removeQueuedListeners() {
    for (int i = 0, n = queuedListenerRemovals.size(); i < n; i++) {
      removeListenerImpl(queuedListenerRemovals.get(i));
    }
    queuedListenerRemovals.clear();
  }

  private void addListenerImpl(final L listener) {
    if (!listeners.contains(listener)) {
      listeners.add(listener);

      if (registrationListener != null) {
        registrationListener.onListenerAdded(listener);
      }
    }
  }

  private void removeListenerImpl(final L listener) {
    if (listeners.remove(listener) && registrationListener != null) {
      registrationListener.onListenerRemoved(listener);
    }
  }
}
