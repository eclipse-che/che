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
package org.eclipse.che.ide.command.editor.page.text;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Implementation of {@link PageWithTextEditorView}.
 *
 * @author Artem Zatsarynnyi
 */
public class PageWithTextEditorViewImpl extends Composite implements PageWithTextEditorView {

  private static final PageWithTextEditorViewImplUiBinder UI_BINDER =
      GWT.create(PageWithTextEditorViewImplUiBinder.class);

  @UiField DockLayoutPanel mainPanel;

  @UiField Label title;

  @UiField Hyperlink exploreMacrosLink;

  @UiField SimpleLayoutPanel editorPanel;

  /** The delegate to receive events from this view. */
  private ActionDelegate delegate;

  @Inject
  public PageWithTextEditorViewImpl() {
    initWidget(UI_BINDER.createAndBindUi(this));
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public SimpleLayoutPanel getEditorContainer() {
    return editorPanel;
  }

  @Override
  public void setHeight(int height) {
    mainPanel.setHeight(height + "px");
  }

  @Override
  public void setEditorTitle(String title) {
    this.title.setText(title);
  }

  @UiHandler("exploreMacrosLink")
  public void handleExploreMacrosLinkClick(ClickEvent event) {
    delegate.onExploreMacros();
  }

  interface PageWithTextEditorViewImplUiBinder
      extends UiBinder<Widget, PageWithTextEditorViewImpl> {}
}
