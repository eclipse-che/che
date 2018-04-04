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

/** Event triggered when the annotation model is cleared. */
public class ClearAnnotationModelEvent extends GwtEvent<ClearAnnotationModelHandler> {

  /** The type instance of the event. */
  public static final Type<ClearAnnotationModelHandler> TYPE = new Type<>();

  /** The model being cleared. */
  private final AnnotationModel annotationModel;

  /**
   * Creates a new annotation model event for the given model.
   *
   * @param model the model
   */
  public ClearAnnotationModelEvent(AnnotationModel model) {
    this.annotationModel = model;
  }

  @Override
  public Type<ClearAnnotationModelHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(final ClearAnnotationModelHandler handler) {
    handler.onClearModel(this);
  }

  /**
   * Return the model being cleared.
   *
   * @return the model
   */
  public AnnotationModel getAnnotationModel() {
    return annotationModel;
  }
}
