/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.editor.annotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.che.ide.api.editor.text.Position;
import org.eclipse.che.ide.api.editor.text.annotation.Annotation;
import org.eclipse.che.ide.util.loging.Log;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Specification of changes applied to annotation models. The event carries the changed annotation model as well as added, removed, and
 * modified annotations.
 * <p/>
 * An event can be sealed. Afterwards it can not be modified. Thus, the normal process is that an empty event is created, filled with the
 * changed information, and before it is sent to the listeners, the event is sealed.
 */
public class AnnotationModelEvent extends GwtEvent<AnnotationModelHandler> {

    public static final Type<AnnotationModelHandler> TYPE = new Type<>();

    /** The model this event refers to. */
    private final AnnotationModel annotationModel;

    /** The added annotations. */
    private final Set<Annotation> addedAnnotations = new HashSet<Annotation>();

    /** The removed annotations. */
    private final Map<Annotation, Position> removedAnnotations = new HashMap<Annotation, Position>();

    /** The changed annotations. */
    private final Set<Annotation> changedAnnotations = new HashSet<Annotation>();

    /**
     * Creates a new annotation model event for the given model.
     * 
     * @param model the model
     */
    public AnnotationModelEvent(AnnotationModel model) {
        this.annotationModel = model;
    }

    /**
     * Returns the model this event refers to.
     * 
     * @return the model this events belongs to
     */
    public AnnotationModel getAnnotationModel() {
        return annotationModel;
    }

    /**
     * Adds the given annotation to the set of annotations that are reported as being added from the model. If this event is considered a
     * world change, it is no longer so after this method has successfully finished.
     * 
     * @param annotation the added annotation
     */
    public void annotationAdded(Annotation annotation) {
        addedAnnotations.add(annotation);
    }

    /**
     * Returns the added annotations.
     * 
     * @return the added annotations
     */
    public List<Annotation> getAddedAnnotations() {
        return new ArrayList<>(addedAnnotations);
    }

    /**
     * Adds the given annotation to the set of annotations that are reported as being removed from the model. If this event is considered a
     * world change, it is no longer so after this method has successfully finished.
     * 
     * @param annotation the removed annotation
     */
    public void annotationRemoved(Annotation annotation) {
        annotationRemoved(annotation, null);
    }

    /**
     * Adds the given annotation to the set of annotations that are reported as being removed from the model. If this event is considered a
     * world change, it is no longer so after this method has successfully finished.
     * 
     * @param annotation the removed annotation
     * @param position the position of the removed annotation
     */
    public void annotationRemoved(Annotation annotation, Position position) {
        removedAnnotations.put(annotation, position);
    }

    /**
     * Returns the removed annotations.
     * 
     * @return the removed annotations
     */
    public List<Annotation> getRemovedAnnotations() {
        return new ArrayList<>(removedAnnotations.keySet());
    }

    /**
     * Returns the position of the removed annotation at that point in time when the annotation has been removed.
     * 
     * @param annotation the removed annotation
     * @return the position of the removed annotation or <code>null</code>
     */
    public Position getPositionOfRemovedAnnotation(Annotation annotation) {
        return removedAnnotations.get(annotation);
    }

    /**
     * Adds the given annotation to the set of annotations that are reported as being changed from the model. If this event is considered a
     * world change, it is no longer so after this method has successfully finished.
     * 
     * @param annotation the changed annotation
     */
    public void annotationChanged(Annotation annotation) {
        changedAnnotations.add(annotation);
    }

    /**
     * Returns the changed annotations.
     * 
     * @return the changed annotations
     */
    public List<Annotation> getChangedAnnotations() {
        return new ArrayList<>(this.changedAnnotations);
    }

    /**
     * Returns whether this annotation model event is empty or not. If this event represents a world change, this method returns
     * <code>false</code> although the event does not carry any added, removed, or changed annotations.
     * 
     * @return <code>true</code> if this event is empty
     */
    public boolean isEmpty() {

        final boolean result = addedAnnotations.isEmpty() && removedAnnotations.isEmpty() && changedAnnotations.isEmpty();
        Log.debug(getClass(), "result", result);

        return result;
    }

    @Override
    public Type<AnnotationModelHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(final AnnotationModelHandler handler) {
        handler.onAnnotationModel(this);
    }
}
