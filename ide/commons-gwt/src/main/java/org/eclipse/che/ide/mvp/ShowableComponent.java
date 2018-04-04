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

package org.eclipse.che.ide.mvp;

import org.eclipse.che.ide.util.ListenerRegistrar;

/**
 * A component which can be explicitly shown and hidden. Implementors of this interface must
 * callback listeners for at least {@link ShowState#SHOWN} and {@link ShowState#HIDDEN}.
 */
public interface ShowableComponent {

  /** An indicating indicating the show state of a component. */
  public enum ShowState {
    SHOWING,
    SHOWN,
    HIDDEN,
    HIDING
  }

  /** A listener which is notified of changes in the components {@link ShowState}. */
  public interface ShowStateChangedListener {
    void onShowStateChanged(ShowState showState);
  }

  /** Displays a component. */
  public void show();

  /** Hides a component. */
  public void hide();

  /**
   * @return true if the state of the component is logically {@link ShowState#SHOWING} or {@link
   *     ShowState#SHOWN}.
   */
  public boolean isShowing();

  public ListenerRegistrar<ShowStateChangedListener> getShowStateChangedListenerRegistrar();
}
