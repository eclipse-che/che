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

/*
 * FIXME: this 'controller' is an 'AutoHideComponent', which is
 * weird, but less code than creating a bunch of delegates to an encapsulated
 * AutoHideComponent. We can fix this if it starts getting ugly.
 */

/** A controller that wraps the given element in a {@link AutoHideComponent}. */
public class AutoHideController extends AutoHideComponent<AutoHideView<Void>, AutoHideComponent.AutoHideModel> {

    public static AutoHideController create(Element element) {
        AutoHideView<Void> view = new AutoHideView<Void>(element);
        AutoHideModel model = new AutoHideModel();
        return new AutoHideController(view, model);
    }

    private AutoHideController(AutoHideView<Void> view, AutoHideComponent.AutoHideModel model) {
        super(view, model);
    }
}
