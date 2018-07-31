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
package org.eclipse.che.ide.processes;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * The event is fired when a tree node is selected in the processes tree.
 *
 * @author Vitaliy Guliy
 */
public class ProcessTreeNodeSelectedEvent extends GwtEvent<ProcessTreeNodeSelectedEvent.Handler> {

  public interface Handler extends EventHandler {

    /**
     * Implement this method to handle selecting the process tree node.
     *
     * @param event the event
     */
    void onProcessTreeNodeSelected(ProcessTreeNodeSelectedEvent event);
  }

  public static final Type<ProcessTreeNodeSelectedEvent.Handler> TYPE = new Type<>();

  private ProcessTreeNode processTreeNode;

  public ProcessTreeNodeSelectedEvent(ProcessTreeNode processTreeNode) {
    this.processTreeNode = processTreeNode;
  }

  public ProcessTreeNode getProcessTreeNode() {
    return processTreeNode;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onProcessTreeNodeSelected(this);
  }
}
