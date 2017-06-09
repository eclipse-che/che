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
 * JavaScript overlay over Orion ContentAssist object.
 *
 * @author Artem Zatsarynnyi
 */
public class OrionContentAssistOverlay extends JavaScriptObject {

    protected OrionContentAssistOverlay() {
    }

    /** Checks whether the content assist is active or not. */
    public final native boolean isActive() /*-{
        return this.isActive();
    }-*/;

    /** Activates the content assist. */
    public final native void activate() /*-{
        this.activate();
    }-*/;

    /** Deactivates the content assist. */
    public final native void deactivate() /*-{
        this.deactivate();
    }-*/;
}
