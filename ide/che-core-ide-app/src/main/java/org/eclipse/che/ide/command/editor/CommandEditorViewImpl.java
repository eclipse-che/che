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
package org.eclipse.che.ide.command.editor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.eclipse.che.ide.command.CommandResources;

/**
 * Implementation of {@link CommandEditorView}.
 *
 * @author Artem Zatsarynnyi
 */
public class CommandEditorViewImpl extends Composite implements CommandEditorView {

  private static final CommandEditorViewImplUiBinder UI_BINDER =
      GWT.create(CommandEditorViewImplUiBinder.class);

  private final CommandResources resources;

  @UiField Button cancelButton;

  @UiField Button saveButton;

  @UiField ScrollPanel scrollPanel;

  @UiField FlowPanel pagesPanel;

  /** The delegate to receive events from this view. */
  private ActionDelegate delegate;

  @Inject
  public CommandEditorViewImpl(CommandResources resources) {
    this.resources = resources;

    initWidget(UI_BINDER.createAndBindUi(this));
    setSaveEnabled(false);
  }

  @Override
  public void addPage(IsWidget page, String title) {
    page.asWidget().addStyleName(resources.editorCss().section());
    pagesPanel.insert(page, 0);

    if (!title.isEmpty()) {
      Label label = new Label(title);
      label.addStyleName(resources.editorCss().sectionLabel());
      pagesPanel.insert(label, 0);
    }

    // editor must be scrolled to the top immediately after opening
    new Timer() {
      @Override
      public void run() {
        scrollPanel.scrollToTop();
      }
    }.schedule(1000);
  }

  @Override
  public void setSaveEnabled(boolean enable) {
    saveButton.setEnabled(enable);
  }

  @UiHandler("cancelButton")
  public void handleCancelButton(ClickEvent clickEvent) {
    delegate.onCommandCancel();
  }

  @UiHandler("saveButton")
  public void handleSaveButton(ClickEvent clickEvent) {
    delegate.onCommandSave();
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  interface CommandEditorViewImplUiBinder extends UiBinder<Widget, CommandEditorViewImpl> {}
}
