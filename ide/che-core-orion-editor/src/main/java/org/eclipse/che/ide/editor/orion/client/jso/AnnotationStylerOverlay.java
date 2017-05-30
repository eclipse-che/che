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
 * This object represents a styler for annotation attached to a text view.
 * @author Evgen Vidolob
 */
public class AnnotationStylerOverlay extends JavaScriptObject {
    protected AnnotationStylerOverlay() {
    }


    //TODO add all this methods if needed
//    getAnnotationsByType(annotationModel, start, end)
//    Returns an array of annotations in the specified annotation model for the given range of text sorted by type.
//
//    getAnnotationTypePriority(type)
//    Gets the annotation type priority.
//
//            isAnnotationTypeVisible(type)
//    Returns whether the receiver shows annotations of the specified type.
//
//    removeAnnotationType(type)
//    Removes an annotation type from the receiver.

    /**
     * Adds an annotation type to the receiver.
     * @param type
     * @param priority
     */
    public final native void addAnnotationType(String type, int priority) /*-{
        this.addAnnotationType(type, priority);
    }-*/;
}
