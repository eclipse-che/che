/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.editor.annotation;

import com.google.gwt.event.shared.GwtEvent;
import java.util.Map;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.editor.text.LinearRange;
import org.eclipse.che.ide.api.editor.text.Position;
import org.eclipse.che.ide.api.editor.text.annotation.Annotation;

/** Event triggered when the annotation model is cleared. */
public class QueryAnnotationsEvent extends GwtEvent<QueryAnnotationsHandler> {

  /** The type instance of the event. */
  public static final Type<QueryAnnotationsHandler> TYPE = new Type<>();

  /** Default filter, that accepts any annotation. */
  public static final AnnotationFilter YES_FILTER =
      new AnnotationFilter() {
        @Override
        public boolean accept(final Annotation annotation) {
          return true;
        }
      };

  /** The callback to call with the result. */
  private final QueryCallback callback;

  /** An optional additional filter. */
  private final AnnotationFilter additionalFilter;

  /** The text range for the query. */
  private final LinearRange range;

  /**
   * Creates a new annotation model event for the given model.
   *
   * @param model the model
   * @param callback the callback to give the results
   * @param an additional filter to choose the annotations
   */
  private QueryAnnotationsEvent(
      final LinearRange range,
      final QueryCallback callback,
      final AnnotationFilter additionalFilter) {
    this.callback = callback;
    this.range = range;
    this.additionalFilter = additionalFilter;
  }

  @Override
  public Type<QueryAnnotationsHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(final QueryAnnotationsHandler handler) {
    handler.onQueryAnnotations(this);
  }

  /**
   * Returns the callback used to anwser the query.
   *
   * @return the callback
   */
  public QueryCallback getCallback() {
    return callback;
  }

  /**
   * Return the range of the query.
   *
   * @return the range
   */
  public LinearRange getRange() {
    return range;
  }

  /**
   * Returns the annotation filter.
   *
   * @return the filter. <em>May be null</em>
   */
  @Nullable
  public AnnotationFilter getAdditionalFilter() {
    return additionalFilter;
  }

  /** A callback interface used to answer the query. */
  public interface QueryCallback {

    /** The callback method. */
    void respond(Map<Annotation, Position> annotations);
  }

  /** A filter for annotations in the query result. */
  public interface AnnotationFilter {

    /**
     * Whether the annotation will be handed.
     *
     * @param annotation the annotation to examine
     * @return true accepts the annotation, false rejects it
     */
    boolean accept(Annotation annotation);
  }

  /** Builder for {@link QueryAnnotationsEvent} instances. */
  public static final class Builder {
    /** The range for the query. */
    private LinearRange range;
    /** The callback for the query. */
    private QueryCallback callback;
    /** The filter for the query. */
    private AnnotationFilter filter = YES_FILTER;

    /**
     * Sets the range.
     *
     * @param range the range
     * @return this object
     */
    public Builder withRange(final LinearRange range) {
      this.range = range;
      return this;
    }

    /**
     * Sets the cllback.
     *
     * @param callback the callback
     * @return this object
     */
    public Builder withCallback(final QueryCallback callback) {
      this.callback = callback;
      return this;
    }

    /**
     * Sets the filter.
     *
     * @param filter the filter
     * @return this object
     */
    public Builder withFilter(AnnotationFilter filter) {
      this.filter = filter;
      return this;
    }

    /**
     * Builds the {@link QueryAnnotationsEvent}.
     *
     * @return the created instance
     */
    public QueryAnnotationsEvent build() {
      return new QueryAnnotationsEvent(this.range, this.callback, this.filter);
    }
  }
}
