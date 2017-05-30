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
package org.eclipse.che.ide.api.editor.annotation;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.che.ide.api.editor.text.Position;
import org.eclipse.che.ide.api.editor.text.annotation.Annotation;
import org.eclipse.che.ide.api.editor.events.DocumentChangeHandler;
import org.eclipse.che.ide.api.editor.document.UseDocumentHandle;

/**
 * This interface defines the model for managing annotations attached to a document. The model maintains a set of annotations for a given
 * document and notifies registered annotation model listeners about annotation model changes. It also provides methods for querying the
 * current position of an annotation managed by this model.<br>
 * The following events are produced:
 * <ul>
 * <li> {@link AnnotationModelEvent} when the {@link AnnotationModel} changes (the changes are batched)</li>
 * <li> {@link ClearAnnotationModelEvent} when he {@link AnnotationModel} is cleared</li>
 * </ul>
 * The model handles the following events:
 * <ul>
 * <li> {@link QueryAnnotationsEvent} where a component requests a list of annotations in a given range and the model answers
 * using the callback.</li>
 * </ul>
 */
public interface AnnotationModel extends UseDocumentHandle, DocumentChangeHandler, QueryAnnotationsHandler {


    /**
     * Adds a annotation to this annotation model. The annotation is associated with with the given position which describes the range
     * covered by the annotation. All registered annotation model listeners are informed about the change. If the model is connected to a
     * document, the position is automatically updated on document changes. If the annotation is already managed by this annotation model or
     * is not a valid position in the connected document nothing happens.
     *
     * @param annotation the annotation to add, may not be <code>null</code>
     * @param position the position describing the range covered by this annotation, may not be <code>null</code>
     */
    void addAnnotation(Annotation annotation, Position position);

    /**
     * Removes the given annotation from the model. I.e. the annotation is no longer managed by this model. The position associated with the
     * annotation is no longer updated on document changes. If the annotation is not managed by this model, nothing happens.
     *
     * @param annotation the annotation to be removed from this model, may not be <code>null</code>
     */
    void removeAnnotation(Annotation annotation);

    /**
     * Returns all annotations managed by this model.
     *
     * @return all annotations managed by this model (element type: {@link Annotation})
     */
    Iterator<Annotation> getAnnotationIterator();

    /**
     * Returns an iterator over all annotations managed by this model that are inside the given region.
     *
     * @param offset the start position of the region, must be >= 0
     * @param length the length of the region, must be >= 0
     * @param canStartBefore if <code>true</code> then annotations are included which start before the region if they end at or after the
     *            region's start
     * @param canEndAfter if <code>true</code> then annotations are included which end after the region if they start at or before the
     *            region's end
     * @return all annotations inside the region managed by this model (element type: {@link Annotation})
     */
    Iterator<Annotation> getAnnotationIterator(int offset, int length, boolean canStartBefore, boolean canEndAfter);

    /**
     * Returns the position associated with the given annotation.
     *
     * @param annotation the annotation whose position should be returned
     * @return the position of the given annotation or <code>null</code> if no associated annotation exists
     */
    Position getPosition(Annotation annotation);

    /**
     * Returns decorations (CSS styles) mapped to Annotation type
     *
     * @return all decorations
     */
    Map<String, String> getAnnotationDecorations();

    /**
     * Returns styles (CSS styles) mapped to Annotation type
     *
     * @return all decorations
     */
    Map<String, String> getAnnotationStyle();

    /**
     * Clear the annotation model.
     */
    void clear();
}
