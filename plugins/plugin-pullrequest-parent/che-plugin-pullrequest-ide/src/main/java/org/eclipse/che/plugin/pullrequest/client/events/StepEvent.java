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
import org.eclipse.che.plugin.pullrequest.client.workflow.Step;

/**
 * Event sent when a step is done or in error.
 *
 * @author Kevin Pollet
 */
public class StepEvent extends GwtEvent<StepHandler> {
  public static final Type<StepHandler> TYPE = new Type<>();

  private final Step step;
  private final boolean success;
  private final String message;
  private final Context context;

  public StepEvent(final Context context, final Step step, final boolean success) {
    this(context, step, success, null);
  }

  public StepEvent(
      final Context context, final Step step, final boolean success, final String message) {
    this.step = step;
    this.success = success;
    this.message = message;
    this.context = context;
  }

  @Override
  public Type<StepHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(@NotNull final StepHandler handler) {
    if (success) {
      handler.onStepDone(this);

    } else {
      handler.onStepError(this);
    }
  }

  public Step getStep() {
    return step;
  }

  public String getMessage() {
    return message;
  }

  public Context getContext() {
    return context;
  }
}
