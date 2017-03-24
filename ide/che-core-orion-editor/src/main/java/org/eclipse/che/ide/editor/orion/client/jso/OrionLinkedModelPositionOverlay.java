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
 * @author Evgen Vidolob
 */
public class OrionLinkedModelPositionOverlay extends JavaScriptObject {
    protected OrionLinkedModelPositionOverlay() {
    }

    public final native void setOffset(int offset) /*-{
        this.offset = offset;
    }-*/;

    public final native void setLength(int length) /*-{
        this.length = length;
    }-*/;
}
