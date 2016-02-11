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
package org.eclipse.che.ide.ui.menu;

import elemental.dom.Element;

import org.eclipse.che.ide.mvp.CompositeView;
import org.eclipse.che.ide.util.AnimationController;


/**
 * The View for AutoHideComponent.
 *
 * @param <D>
 *         event delegate class
 */
public class AutoHideView<D> extends CompositeView<D> {

    private AnimationController animationController = AnimationController.NO_ANIMATION_CONTROLLER;

    public AutoHideView(final Element elem) {
        super(elem);
    }

    /** Constructor to allow subclasses to use UiBinder. */
    protected AutoHideView() {
    }

    /** Hides the view, using the animation controller. */
    public void hide() {
        animationController.hide(getElement());
    }

    /** Shows the view, using the animation controller. */
    public void show() {
        animationController.show(getElement());
    }

    public void setAnimationController(AnimationController controller) {
        this.animationController = controller;
    }

    @Override
    protected void setElement(Element element) {
      /*
       * Start in the hidden state. animationController may not be initialized if
       * this method is called from the constructor, so use the default animation
       * controller.
       */
        AnimationController.NO_ANIMATION_CONTROLLER.hideWithoutAnimating(element);
        super.setElement(element);
    }
}
