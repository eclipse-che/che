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

import elemental.dom.Element;

/**
 * Implementors are Objects that represent some DOM structure.
 *
 * @param <D> Generic type representing any class that wishes to become this view's delegate and
 *     handle events sourced by the view.
 */
public interface View<D> {
  /** @return the delegate which receives events from this view. */
  D getDelegate();

  /** Sets the delegate to receive events from this view. */
  void setDelegate(D delegate);

  /** @return the base element for this view. */
  Element getElement();
}
