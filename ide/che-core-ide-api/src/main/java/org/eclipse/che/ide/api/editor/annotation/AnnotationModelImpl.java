/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.editor.annotation;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.che.ide.api.editor.document.DocumentHandle;
import org.eclipse.che.ide.api.editor.events.DocumentChangedEvent;
import org.eclipse.che.ide.api.editor.partition.DocumentPositionMap;
import org.eclipse.che.ide.api.editor.text.BadLocationException;
import org.eclipse.che.ide.api.editor.text.BadPositionCategoryException;
import org.eclipse.che.ide.api.editor.text.LinearRange;
import org.eclipse.che.ide.api.editor.text.Position;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.text.annotation.Annotation;
import org.eclipse.che.ide.util.loging.Log;

/** Default implementation of {@link AnnotationModel} */
public class AnnotationModelImpl implements AnnotationModel {

  /** The list of managed annotations */
  protected Map<Annotation, Position> annotations;

  /** The map which maps {@link Position} to {@link Annotation}. */
  private final IdentityHashMap<Position, Annotation> positions;

  /** The current annotation model event. */
  private AnnotationModelEvent modelEvent;

  private DocumentHandle docHandle;

  private boolean documentChanged;

  private final DocumentPositionMap documentPositionMap;

  private PositionHolder positionHolder;

  public AnnotationModelImpl(final DocumentPositionMap docPositionMap) {
    this.annotations = new HashMap<>(10);
    this.positions = new IdentityHashMap<>(10);
    this.documentPositionMap = docPositionMap;
  }

  @Override
  public void addAnnotation(final Annotation annotation, final Position position) {
    addAnnotation(annotation, position, true);
  }

  protected void addAnnotation(
      final Annotation annotation, final Position position, final boolean fireEvent) {
    annotations.put(annotation, position);
    positions.put(position, annotation);
    try {
      addPosition(position);
    } catch (BadLocationException ignore) {
      Log.error(getClass(), "BadLocation for " + annotation);
      // ignore invalid location
    }
    getAnnotationModelEvent().annotationAdded(annotation);
    if (fireEvent) {
      fireModelChanged();
    }
  }

  private void addPosition(Position position) throws BadLocationException {
    if (positionHolder != null) {
      positionHolder.addPosition(position);
    }
  }

  /**
   * Returns the current annotation model event. This is the event that will be sent out when
   * calling <code>fireModelChanged</code>.
   */
  protected final AnnotationModelEvent getAnnotationModelEvent() {
    if (this.modelEvent == null) {
      this.modelEvent = createAnnotationModelEvent();
    }
    return this.modelEvent;
  }

  /**
   * Creates and returns a new annotation model event. Subclasses may override.
   *
   * @return a new and empty annotation model event
   */
  protected AnnotationModelEvent createAnnotationModelEvent() {
    return new AnnotationModelEvent(this);
  }

  /**
   * Informs all annotation model listeners that this model has been changed as described in the
   * annotation model event.
   */
  protected void fireModelChanged() {
    final boolean empty = getAnnotationModelEvent().isEmpty();
    if (empty) {
      return;
    }
    if (getDocumentHandle() == null || getDocumentHandle().getDocEventBus() == null) {
      return;
    }
    getDocumentHandle().getDocEventBus().fireEvent(this.modelEvent);
  }

  @Override
  public void removeAnnotation(final Annotation annotation) {
    if (this.annotations.containsKey(annotation)) {

      Position pos = this.annotations.get(annotation);

      this.annotations.remove(annotation);
      positions.remove(pos);

      getAnnotationModelEvent().annotationRemoved(annotation, pos);

      fireModelChanged();
    }
  }

  @Override
  public Iterator<Annotation> getAnnotationIterator() {
    return this.annotations.keySet().iterator();
  }

  @Override
  public Iterator<Annotation> getAnnotationIterator(
      final int offset, final int length, final boolean canStartBefore, final boolean canEndAfter) {
    return getRegionAnnotationIterator(offset, length, canStartBefore, canEndAfter);
  }

  private Iterator<Annotation> getRegionAnnotationIterator(
      int offset, int length, boolean canStartBefore, boolean canEndAfter) {

    cleanup(true);

    try {
      List<Position> positions =
          positionHolder.getPositions(offset, length, canStartBefore, canEndAfter);
      return new AnnotationsIterator(positions, this.positions);

    } catch (BadPositionCategoryException e) {
      // can happen if e.g. the document doesn't contain such a category, or when removed in a
      // different thread
      return Collections.<Annotation>emptyList().iterator();
    }
  }

  /**
   * Removes all annotations from the model whose associated positions have been deleted. If
   * requested inform all model listeners about the change. If requested a new thread is created for
   * the notification of the model listeners.
   *
   * @param fireModelChanged indicates whether to notify all model listeners
   */
  private void cleanup(final boolean fireModelChanged) {
    if (documentChanged) {
      documentChanged = false;

      final List<Annotation> deleted = new ArrayList<Annotation>();
      final Iterator<Annotation> e = getAnnotationIterator();
      while (e.hasNext()) {
        final Annotation annotation = e.next();
        final Position pos = annotations.get(annotation);
        if (pos == null || pos.isDeleted()) {
          deleted.add(annotation);
        }
      }

      if (fireModelChanged) {
        removeAnnotations(deleted, false);
        if (modelEvent != null) {
          Scheduler.get()
              .scheduleDeferred(
                  new ScheduledCommand() {

                    @Override
                    public void execute() {
                      fireModelChanged();
                    }
                  });
        }
      } else {
        removeAnnotations(deleted, fireModelChanged);
      }
    }
  }

  /**
   * Removes the given annotations from this model. If requested all annotation model listeners will
   * be informed about this change. <code>modelInitiated</code> indicates whether the deletion has
   * been initiated by this model or by one of its clients.
   *
   * @param annotations the annotations to be removed
   * @param fireModelChanged indicates whether to notify all model listeners
   */
  protected void removeAnnotations(
      final List<? extends Annotation> annotations, final boolean fireModelChanged) {
    if (!annotations.isEmpty()) {
      final Iterator<? extends Annotation> e = annotations.iterator();
      while (e.hasNext()) {
        removeAnnotation(e.next(), false);
      }

      if (fireModelChanged) {
        fireModelChanged();
      }
    }
  }

  /**
   * Removes the given annotation from the annotation model. If requested inform all model change
   * listeners about this change.
   *
   * @param annotation the annotation to be removed
   * @param fireModelChanged indicates whether to notify all model listeners
   */
  protected void removeAnnotation(final Annotation annotation, final boolean fireModelChanged) {
    if (annotations.containsKey(annotation)) {

      Position pos = null;
      pos = annotations.get(annotation);

      annotations.remove(annotation);
      positions.remove(pos);
      getAnnotationModelEvent().annotationRemoved(annotation, pos);

      if (fireModelChanged) {
        fireModelChanged();
      }
    }
  }

  @Override
  public Position getPosition(final Annotation annotation) {
    final Position position = annotations.get(annotation);
    return position;
  }

  @Override
  public Map<String, String> getAnnotationDecorations() {
    return new HashMap<>();
  }

  @Override
  public Map<String, String> getAnnotationStyle() {
    return new HashMap<>();
  }

  @Override
  public void onDocumentChanged(final DocumentChangedEvent event) {
    this.documentChanged = true;
  }

  @Override
  public void setDocumentHandle(final DocumentHandle handle) {
    this.docHandle = handle;
    this.positionHolder = new PositionHolder(handle.getDocument());
  }

  @Override
  public DocumentHandle getDocumentHandle() {
    return this.docHandle;
  }

  // TODO evaluate: keep?
  public void forgetLines(final int fromLine, final int count) {
    forgetLines(fromLine, count, true);
  }

  // TODO evaluate: keep?
  public void forgetLines(int fromLine) {
    forgetLines(fromLine, 0, false);
  }

  // TODO evaluate: keep?
  private void forgetLines(final int fromLine, final int count, final boolean checkCount) {
    // use an iterator to have remove()
    final Iterator<Entry<Annotation, Position>> iterator = this.annotations.entrySet().iterator();
    while (iterator.hasNext()) {
      final Entry<Annotation, Position> entry = iterator.next();
      final Position position = entry.getValue();
      final TextPosition textPos =
          docHandle.getDocument().getPositionFromIndex(position.getOffset());
      final int line = textPos.getLine();
      if (line >= fromLine && (!checkCount || line < fromLine + count)) {
        iterator.remove();
      }
    }
  }

  // TODO evaluate: keep?
  public void shiftLines(final int fromLine, final int lineDelta, final int charDelta) {
    final Map<Annotation, Position> modified = new IdentityHashMap<>();
    for (final Entry<Annotation, Position> entry : this.annotations.entrySet()) {
      final Position position = entry.getValue();
      final TextPosition textPos =
          docHandle.getDocument().getPositionFromIndex(position.getOffset());

      int horizontal;
      if (textPos.getLine() == fromLine) {
        horizontal = charDelta;
      } else if (textPos.getLine() >= fromLine) {
        horizontal = 0;
      } else {
        continue;
      }
      final TextPosition newTextPos =
          new TextPosition(fromLine + lineDelta, textPos.getCharacter() + horizontal);
      final int newOffset = docHandle.getDocument().getIndexFromPosition(newTextPos);
      final Position newPos = new Position(newOffset, position.getLength());
      modified.put(entry.getKey(), newPos);
    }
    // merge changes in the annotartion map
    this.annotations.putAll(modified);
  }

  @Override
  public void clear() {
    this.annotations.clear();
    this.positions.clear();
    this.modelEvent = new AnnotationModelEvent(this);
    this.docHandle.getDocEventBus().fireEvent(new ClearAnnotationModelEvent(this));
  }

  @Override
  public void onQueryAnnotations(final QueryAnnotationsEvent event) {
    final QueryAnnotationsEvent.QueryCallback callback = event.getCallback();
    if (callback == null) {
      return;
    }
    final LinearRange range = event.getRange();
    Iterator<Annotation> iterator;
    if (range == null) {
      iterator = getAnnotationIterator();
    } else {
      iterator = getAnnotationIterator(range.getStartOffset(), range.getLength(), true, true);
    }
    final QueryAnnotationsEvent.AnnotationFilter filter = event.getAdditionalFilter();

    final Map<Annotation, Position> result = new HashMap<>();
    while (iterator.hasNext()) {
      final Annotation annotation = iterator.next();
      if (filter.accept(annotation)) {
        result.put(annotation, this.annotations.get(annotation));
      }
    }
    callback.respond(result);
  }
}
