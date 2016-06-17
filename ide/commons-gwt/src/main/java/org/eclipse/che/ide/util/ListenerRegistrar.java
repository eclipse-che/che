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


/** A manager to register or unregister listeners. */
public interface ListenerRegistrar<T> {

    /** A handle to allow removing the added listener. */
    public interface Remover {
        void remove();
    }

    /**
     * An object which helps to simplify management of multiple handlers that need
     * to be removed. This is the recommended approach to managing removers as it
     * guards against null checks and prevents forgetting to remove listeners.
     */
    public static class RemoverManager implements Remover {
        private List<Remover> handlers;

        /** Tracks a new handler so that it can be removed in bulk. */
        public RemoverManager track(Remover remover) {
            if (handlers == null) {
                handlers = new ArrayList<>();
            }

            handlers.add(remover);
            return this;
        }

        /** Removes all tracked handlers and clears the stored list of handlers. */
        @Override
        public void remove() {
            if (handlers == null) {
                return;
            }

            for (int i = 0; i < handlers.size(); i++) {
                handlers.get(i).remove();
            }

            handlers.clear();
        }
    }

    /** Registers a new listener. */
    Remover add(T listener);

    /**
     * Removes a listener. It is strongly preferred you use the {@link Remover}
     * returned by {@link #add(Object)} instead of calling this method directly.
     */
    void remove(T listener);
}
