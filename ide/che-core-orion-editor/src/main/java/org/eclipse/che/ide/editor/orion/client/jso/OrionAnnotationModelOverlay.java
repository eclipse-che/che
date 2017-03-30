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

/** Overlay on the orion JS Annotation model objects. */
public class OrionAnnotationModelOverlay extends JavaScriptObject {

    /** JSO mandated protected constructor. */
    protected OrionAnnotationModelOverlay() {
    }

    /**
     * Adds an annotation to the annotation model.
     * 
     * @param annotation the annotation to add
     */
    public final native void addAnnotation(OrionAnnotationOverlay annotation) /*-{
        this.addAnnotation(annotation);
    }-*/;

    /**
     * Removes an annotation from the annotation model.
     * 
     * @param annotation the annotation to remove
     */
    public final native void removeAnnotation(OrionAnnotationOverlay annotation) /*-{
        this.removeAnnotation(annotation);
    }-*/;

    /**
     * Removes all annotations of the given type.
     * 
     * @param annotationType the type of annotations to remove
     */
    public final native void removeAnnotations(String annotationType) /*-{
        this.removeAnnotations(annotationType);
    }-*/;

    /**
     * Removes all annotations.
     */
    public final native void removeAnnotations() /*-{
        this.removeAnnotations();
    }-*/;

    /**
     * Notifies the annotation model that the given annotation has been modified.
     *
     * @param annotation the modified annotation
     */
    public final native void modifyAnnotation(OrionAnnotationOverlay annotation) /*-{
        this.modifyAnnotation(annotation);
    }-*/;

    /**
     * Removes and adds the specifed annotations to the annotation model.
     *
     * @param removedAnnotation the annotation that is removed
     * @param addedAnnotation the annotation that is added
     */
    public final native void replaceAnnotation(OrionAnnotationOverlay removedAnnotation, OrionAnnotationOverlay addedAnnotation) /*-{
        this.replaceAnnotation(removedAnnotation, addedAnnotation);
    }-*/;

    /**
     * Returns an iterator of annotations for the given range of text.
     * 
     * @return annotations iterator
     */
    public final native OrionAnnotationIteratorOverlay getAnnotations(int startOffset, int endOffset) /*-{
        return this.getAnnotations(startOffset, endOffset);
    }-*/;

    /**
     * Returns an iterator of all annotations.
     * 
     * @return annotations iterator
     */
    public final native OrionAnnotationIteratorOverlay getAnnotations() /*-{
        return this.getAnnotations();
    }-*/;
}
