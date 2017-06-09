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

import java.util.Iterator;

import com.google.gwt.core.client.JavaScriptObject;

/** Overlay on the orion JS Annotation iterator objects. */
public class OrionAnnotationIteratorOverlay extends JavaScriptObject implements Iterator<OrionAnnotationOverlay> {

    /** JSO mandated protected constructor. */
    protected OrionAnnotationIteratorOverlay() {
    }

    @Override
    public final native boolean hasNext() /*-{
        return this.hasNext();
    }-*/;

    @Override
    public final native OrionAnnotationOverlay next() /*-{
        return this.next();
    }-*/;

    @Override
    public final void remove() {
        throw new UnsupportedOperationException();
    }
}
