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
package org.eclipse.che.plugin.java.plain.client.wizard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Implementation of {@link PlainJavaPageView}
 *
 * @author Valeriy Svydenko
 */
public class PlainJavaPageViewImpl implements PlainJavaPageView {
  private static PlainJavaPageViewImplUiBinder ourUiBinder =
      GWT.create(PlainJavaPageViewImplUiBinder.class);

  private final FlowPanel rootElement;

  @UiField TextBox sourceFolderField;
  @UiField Button browseSourceBtn;
  @UiField TextBox libFolderField;
  @UiField Button browseLibBtn;
  @UiField FlowPanel libraryFolderContainer;

  private ActionDelegate delegate;

  @Inject
  public PlainJavaPageViewImpl() {
    rootElement = ourUiBinder.createAndBindUi(this);
    browseSourceBtn.setVisible(false);
    sourceFolderField.setReadOnly(true);

    browseLibBtn.addClickHandler(
        new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            delegate.onBrowseLibraryButtonClicked();
          }
        });
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public Widget asWidget() {
    return rootElement;
  }

  @Override
  public String getSourceFolder() {
    return sourceFolderField.getText();
  }

  @Override
  public void setSourceFolder(String value) {
    sourceFolderField.setText(value);
  }

  @Override
  public String getLibraryFolder() {
    return libFolderField.getText();
  }

  @Override
  public void setLibraryFolder(String value) {
    libFolderField.setText(value);
  }

  @UiHandler({"sourceFolderField"})
  void onKeyUp(KeyUpEvent event) {
    delegate.onCoordinatesChanged();
  }

  @Override
  public void changeBrowseBtnVisibleState(boolean isVisible) {
    browseSourceBtn.setVisible(false);
  }

  @Override
  public void changeLibraryPanelVisibleState(boolean isVisible) {
    libraryFolderContainer.setVisible(isVisible);
  }

  @Override
  public void changeSourceFolderFieldState(boolean isEnable) {
    sourceFolderField.setReadOnly(true);
  }

  interface PlainJavaPageViewImplUiBinder extends UiBinder<FlowPanel, PlainJavaPageViewImpl> {}
}
