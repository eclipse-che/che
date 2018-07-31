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

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import javax.inject.Singleton;

/** Implementation of {@link CommandToolbarView}. */
@Singleton
public class CommandToolbarViewImpl implements CommandToolbarView {

  private static final CommandToolbarViewImplUiBinder UI_BINDER =
      GWT.create(CommandToolbarViewImplUiBinder.class);

  @UiField FlowPanel rootPanel;
  @UiField SimplePanel commandsPanel;
  @UiField SimplePanel processesListPanel;
  @UiField SimplePanel panelSelectorPanel;
  @UiField SimplePanel toolbarControllerPanel;
  @UiField SimplePanel buttonsPanel;
  @UiField SimplePanel previewUrlListPanel;

  private ActionDelegate delegate;

  @Inject
  public CommandToolbarViewImpl() {
    UI_BINDER.createAndBindUi(this);
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public Widget asWidget() {
    return rootPanel;
  }

  @Override
  public AcceptsOneWidget getCommandsPanelContainer() {
    return commandsPanel;
  }

  @Override
  public AcceptsOneWidget getProcessesListContainer() {
    return processesListPanel;
  }

  @Override
  public AcceptsOneWidget getPreviewUrlsListContainer() {
    return previewUrlListPanel;
  }

  @Override
  public AcceptsOneWidget getPanelSelectorContainer() {
    return panelSelectorPanel;
  }

  @Override
  public AcceptsOneWidget getToolbarControllerContainer() {
    return toolbarControllerPanel;
  }

  @Override
  public void addButton(ToolbarButton button) {
    buttonsPanel.add(button);
  }

  interface CommandToolbarViewImplUiBinder extends UiBinder<Widget, CommandToolbarViewImpl> {}
}
