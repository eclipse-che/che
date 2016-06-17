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

package org.eclipse.che.ide.ui;

import elemental.dom.Element;
import elemental.js.dom.JsElement;

import org.eclipse.che.ide.mvp.UiComponent;
import org.eclipse.che.ide.mvp.View;


/**
 * A single DOM Element.
 * <p/>
 * This is a View (V) in our use of MVP.
 * <p/>
 * Use this when you want to give some brains to a single DOM element by making this the View for
 * some {@link UiComponent} that will contain business logic.
 */
// TODO: move this to mvp package when ray fixes the
// JsoRestrictionChecker bug in the gwt compiler.
public class ElementView<D> extends JsElement implements View<D> {
    protected ElementView() {
    }

    @Override
    public final native D getDelegate() /*-{
        return this["delegate"];
    }-*/;

    @Override
    public final native void setDelegate(D delegate) /*-{
        this["delegate"] = delegate;
    }-*/;

    @Override
    public final native Element getElement() /*-{
        return this;
    }-*/;
}
