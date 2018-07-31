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
package org.eclipse.che.ide.ext.java.client.editor;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import org.eclipse.che.ide.ext.java.shared.dto.ReconcileResult;

/**
 * The event is used to notify interested client consumers about results of reconcile operation.
 *
 * @author Roman Nikitenko
 */
public class ReconcileOperationEvent
    extends GwtEvent<ReconcileOperationEvent.ReconcileOperationHandler> {
  public static Type<ReconcileOperationHandler> TYPE = new Type<>();

  private final ReconcileResult reconcileResult;

  /**
   * Creates an event which contains result of reconcile operation
   *
   * @param reconcileResult info about result of reconcile operation
   */
  public ReconcileOperationEvent(ReconcileResult reconcileResult) {
    this.reconcileResult = reconcileResult;
  }

  @Override
  public Type<ReconcileOperationHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(ReconcileOperationHandler handler) {
    handler.onReconcileOperation(reconcileResult);
  }

  /** Apples result of reconcile operation */
  public interface ReconcileOperationHandler extends EventHandler {
    void onReconcileOperation(ReconcileResult result);
  }
}
