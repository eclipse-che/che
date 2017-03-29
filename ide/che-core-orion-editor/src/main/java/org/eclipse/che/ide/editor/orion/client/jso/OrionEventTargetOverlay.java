/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.editor.orion.client.jso;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * It is designed to extends other objects by adding events functionality.
 */
public class OrionEventTargetOverlay extends JavaScriptObject {

    /**
     * Instantiates a new Event Target overlay.
     */
    protected OrionEventTargetOverlay() {
    }

    /**
     * Adds the event target interface into the specified object.
     *
     * @param orionEventTargetModule
     *         the orion event target module
     * @param object
     *         the object to add the event target interface
     */
    public static final native void addMixin(JavaScriptObject orionEventTargetModule, JavaScriptObject object) /*-{
        orionEventTargetModule.EventTarget.addMixin(object);
    }-*/;
}
