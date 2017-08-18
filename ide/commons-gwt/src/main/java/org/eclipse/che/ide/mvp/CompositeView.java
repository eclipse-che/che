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
 * Wraps an Element that corresponds to some DOM subtree.
 *
 * <p>This is a View (V) in our use of MVP.
 *
 * <p>Use this when you want to give some brains to one or more DOM elements by making this the View
 * for some {@link UiComponent} that will contain business logic.
 *
 * <p>Implementors may choose to attach event listeners that it needs to DOM elements that are
 * contained with this View, and expose logical events where appropriate to the containing
 * UiComponent.
 */
public abstract class CompositeView<D> implements View<D> {
  private Element element;
  private D delegate;

  /**
   * This constructor only exists to support UiBinder which requires us to inject the element after
   * the call to the constructor.
   */
  protected CompositeView() {}

  protected CompositeView(Element element) {
    this.setElement(element);
  }

  /** @return the delegate */
  @Override
  public D getDelegate() {
    return delegate;
  }

  /** @return the element */
  @Override
  public Element getElement() {
    return element;
  }

  /** @param delegate the delegate to set */
  @Override
  public void setDelegate(D delegate) {
    this.delegate = delegate;
  }

  /** @param element the element to set */
  protected void setElement(Element element) {
    this.element = element;
  }
}
