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
package org.eclipse.che.ide.command.toolbar;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.eclipse.che.ide.api.mvp.View;

/** View for command toolbar. */
public interface CommandToolbarView extends View<CommandToolbarView.ActionDelegate> {

  AcceptsOneWidget getCommandsPanelContainer();

  AcceptsOneWidget getProcessesListContainer();

  AcceptsOneWidget getPreviewUrlsListContainer();

  AcceptsOneWidget getPanelSelectorContainer();

  AcceptsOneWidget getToolbarControllerContainer();

  void addButton(ToolbarButton button);

  interface ActionDelegate {}
}
