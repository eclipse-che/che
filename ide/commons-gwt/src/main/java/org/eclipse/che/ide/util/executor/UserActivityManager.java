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

package org.eclipse.che.ide.util.executor;

import com.google.gwt.user.client.Timer;
import org.eclipse.che.ide.util.ListenerManager;
import org.eclipse.che.ide.util.ListenerManager.Dispatcher;
import org.eclipse.che.ide.util.ListenerRegistrar;

/**
 * A class that manages the active status of the user so that other objects can be intelligent about
 * performing computationally intensive work.
 */
public class UserActivityManager {

  private static final int IDLE_DELAY_MS = 400;

  /** A listener that is called when the user either becomes idle or becomes active. */
  public interface UserActivityListener {
    /** Called when the user is considered idle. */
    void onUserIdle();

    /**
     * Called when the user is considered active. This may be called synchronously from critical
     * paths (scrolling), so avoid intensive work.
     */
    void onUserActive();
  }

  private final Dispatcher<UserActivityListener> activeListenerDispatcher =
      new Dispatcher<UserActivityManager.UserActivityListener>() {
        @Override
        public void dispatch(UserActivityListener listener) {
          listener.onUserActive();
        }
      };

  private final Dispatcher<UserActivityListener> idleListenerDispatcher =
      new Dispatcher<UserActivityManager.UserActivityListener>() {
        @Override
        public void dispatch(UserActivityListener listener) {
          listener.onUserIdle();
        }
      };

  private boolean isUserActive = false;

  private final ListenerManager<UserActivityListener> userActivityListenerManager =
      ListenerManager.create();

  private final Timer switchToIdleTimer =
      new Timer() {
        @Override
        public void run() {
          handleUserIdle();
        }
      };

  public ListenerRegistrar<UserActivityListener> getUserActivityListenerRegistrar() {
    return userActivityListenerManager;
  }

  public boolean isUserActive() {
    return isUserActive;
  }

  public void markUserActive() {
    switchToIdleTimer.schedule(IDLE_DELAY_MS);

    if (isUserActive) {
      return;
    }

    isUserActive = true;
    userActivityListenerManager.dispatch(activeListenerDispatcher);
  }

  private void handleUserIdle() {
    if (!isUserActive) {
      return;
    }

    isUserActive = false;
    userActivityListenerManager.dispatch(idleListenerDispatcher);
  }
}
