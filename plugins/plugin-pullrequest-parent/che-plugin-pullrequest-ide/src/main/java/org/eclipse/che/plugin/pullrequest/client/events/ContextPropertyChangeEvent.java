/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.pullrequest.client.events;

import com.google.gwt.event.shared.GwtEvent;
import javax.validation.constraints.NotNull;
import org.eclipse.che.plugin.pullrequest.client.workflow.Context;

/** @author Kevin Pollet */
public class ContextPropertyChangeEvent extends GwtEvent<ContextPropertyChangeHandler> {
  public static Type<ContextPropertyChangeHandler> TYPE = new Type<>();

  private final Context context;
  private final ContextProperty contextProperty;

  public ContextPropertyChangeEvent(
      @NotNull final Context context, @NotNull final ContextProperty contextProperty) {
    this.context = context;
    this.contextProperty = contextProperty;
  }

  /**
   * Returns the context object.
   *
   * @return the context object.
   */
  public Context getContext() {
    return context;
  }

  /**
   * Returns the property changed.
   *
   * @return the property changed.
   */
  public ContextProperty getContextProperty() {
    return contextProperty;
  }

  @Override
  public Type<ContextPropertyChangeHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(final ContextPropertyChangeHandler handler) {
    handler.onContextPropertyChange(this);
  }

  public enum ContextProperty {
    PROJECT,
    ORIGIN_REPOSITORY_OWNER,
    ORIGIN_REPOSITORY_NAME,
    CONTRIBUTE_TO_BRANCH_NAME,
    WORK_BRANCH_NAME
  }
}
