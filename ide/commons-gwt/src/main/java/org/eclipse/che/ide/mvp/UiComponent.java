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

/**
 * This class represents the Presenter (P) in the MVP pattern for constructing UI.
 *
 * <p>The View represents some bag of element references and a definition of logical events sources
 * by the View.
 *
 * <p>The Model is not an explicit entity in this class hierarchy, and is simply a name used to
 * refer to the instance state used by the Presenter. Concrete implementations may choose to
 * abstract instance state behind an explicit Model class if they so choose.
 *
 * <p>The View can be injected at any point in time.
 *
 * <p>Presenters contain the logic for handling events sourced by the View, updating the Model, and
 * for taking changes to the Model and propagating them to the View.
 */
public abstract class UiComponent<V extends View<?>> implements HasView<V> {
  private V view;

  protected UiComponent() {
    this(null);
  }

  protected UiComponent(V view) {
    this.view = view;
  }

  @Override
  public V getView() {
    return view;
  }

  public void setView(V view) {
    this.view = view;
  }
}
