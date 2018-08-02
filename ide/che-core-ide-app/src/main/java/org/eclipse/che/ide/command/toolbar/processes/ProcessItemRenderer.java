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
package org.eclipse.che.ide.command.toolbar.processes;

import com.google.gwt.user.client.ui.Widget;
import org.eclipse.che.ide.ui.dropdown.BaseListItem;
import org.eclipse.che.ide.ui.dropdown.DropdownListItemRenderer;

/** Renders widgets for representing a {@link Process}. */
class ProcessItemRenderer implements DropdownListItemRenderer {

  private final BaseListItem<Process> item;
  private final StopProcessHandler stopHandler;
  private final RerunProcessHandler reRunHandler;

  private ProcessWidget headerWidget;
  private ProcessWidget listWidget;

  ProcessItemRenderer(
      BaseListItem<Process> listItem,
      StopProcessHandler stopProcessHandler,
      RerunProcessHandler reRunProcessHandler) {
    item = listItem;
    stopHandler = stopProcessHandler;
    reRunHandler = reRunProcessHandler;
  }

  @Override
  public Widget renderHeaderWidget() {
    if (headerWidget == null) {
      headerWidget = new ProcessWidget(item, stopHandler, reRunHandler);
    }

    return headerWidget;
  }

  @Override
  public Widget renderListWidget() {
    if (listWidget == null) {
      listWidget = new ProcessWidget(item, stopHandler, reRunHandler);
    }

    return listWidget;
  }

  /** Informs rendered widgets that related process has been stopped. */
  void notifyProcessStopped() {
    headerWidget.toggleStopped();
    listWidget.toggleStopped();
  }

  interface StopProcessHandler {
    /** Called when stopping {@code process} is requested. */
    void onStopProcess(Process process);
  }

  interface RerunProcessHandler {
    /** Called when rerunning {@code process} is requested. */
    void onRerunProcess(Process process);
  }
}
