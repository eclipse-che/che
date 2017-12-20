/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.pullrequest.client.events;

import com.google.gwt.event.shared.GwtEvent;
import org.eclipse.che.plugin.pullrequest.client.workflow.Context;
import org.eclipse.che.plugin.pullrequest.client.workflow.WorkflowExecutor;

/**
 * This event is fired when context is invalidated.
 *
 * @author Yevhenii Voevodin
 * @see WorkflowExecutor#invalidateContext(ProjectConfig)
 */
public class ContextInvalidatedEvent extends GwtEvent<ContextInvalidatedHandler> {

  public static final Type<ContextInvalidatedHandler> TYPE = new Type<>();

  private final Context context;

  public ContextInvalidatedEvent(final Context context) {
    this.context = context;
  }

  @Override
  public Type<ContextInvalidatedHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(ContextInvalidatedHandler handler) {
    handler.onContextInvalidated(context);
  }
}
