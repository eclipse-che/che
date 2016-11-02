/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.core;

import com.google.gwt.core.client.Callback;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.StyleElement;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.component.Component;
import org.eclipse.che.ide.api.theme.Style;

/**
 * Selection background style injector for input fields.
 *
 * @author Vitaliy Guliy
 */
@Singleton
public class SelectionColorInjector implements Component {

    @Override
    public void start(Callback<Component, Exception> callback) {
        StyleElement style = Document.get().createStyleElement();
        style.setInnerHTML("input::selection { background-color: " + Style.theme.inputSelectionBackground() + "!important; }");
        Document.get().getHead().appendChild(style);

        callback.onSuccess(this);
    }

}
