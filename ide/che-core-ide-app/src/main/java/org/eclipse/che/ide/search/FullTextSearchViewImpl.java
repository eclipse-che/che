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
package org.eclipse.che.ide.search;

import static org.eclipse.che.ide.util.dom.DomUtils.isWidgetOrChildFocused;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.search.selectpath.SelectPathPresenter;
import org.eclipse.che.ide.ui.window.Window;

/**
 * The implementation of {@link FullTextSearchView} view.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class FullTextSearchViewImpl extends Window implements FullTextSearchView {

  interface FullTextSearchViewImplUiBinder extends UiBinder<Widget, FullTextSearchViewImpl> {}

  @UiField Label errLabel;

  @UiField(provided = true)
  CoreLocalizationConstant locale;

  @UiField TextBox text;
  @UiField TextBox filesMask;
  @UiField CheckBox isUseFileMask;
  @UiField CheckBox isUseDirectory;
  @UiField CheckBox wholeWordsOnly;
  @UiField TextBox directory;
  @UiField Button selectPathButton;

  Button cancelButton;
  Button acceptButton;

  private ActionDelegate delegate;

  private final SelectPathPresenter selectPathPresenter;

  @Inject
  public FullTextSearchViewImpl(
      CoreLocalizationConstant locale,
      final SelectPathPresenter selectPathPresenter,
      FullTextSearchViewImplUiBinder uiBinder) {
    this.locale = locale;
    this.selectPathPresenter = selectPathPresenter;

    setTitle(locale.textSearchTitle());

    Widget widget = uiBinder.createAndBindUi(this);
    setWidget(widget);

    createButtons();
    addHandlers();

    directory.setReadOnly(true);
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public void close() {
    hide();
  }

  @Override
  public void showDialog() {
    acceptButton.setEnabled(false);
    isUseFileMask.setValue(false);
    filesMask.setEnabled(false);
    isUseDirectory.setValue(false);
    wholeWordsOnly.setValue(false);
    directory.setEnabled(false);
    selectPathButton.setEnabled(false);
    directory.setText("");
    filesMask.setText("*.*");
    directory.setText("/");
    errLabel.setText("");

    new Timer() {
      @Override
      public void run() {
        text.setFocus(true);
      }
    }.schedule(100);

    super.show();
  }

  @Override
  public void setPathDirectory(String path) {
    directory.setText(path);
  }

  @Override
  public String getSearchText() {
    return text.getText();
  }

  @Override
  public String getFileMask() {
    return isUseFileMask.getValue() ? filesMask.getText() : "";
  }

  @Override
  public String getPathToSearch() {
    return isUseDirectory.getValue() ? directory.getText() : "";
  }

  @Override
  public void showErrorMessage(String message) {
    errLabel.setText(message);
  }

  @Override
  public void onEnterPress(NativeEvent evt) {
    super.onEnterPress(evt);
    delegate.onEnterClicked();
  }

  @Override
  public void clearInput() {
    text.setText("");
  }

  @Override
  public void setFocus() {
    acceptButton.setFocus(true);
  }

  @Override
  public boolean isAcceptButtonInFocus() {
    return isWidgetOrChildFocused(acceptButton);
  }

  @Override
  public boolean isCancelButtonInFocus() {
    return isWidgetOrChildFocused(cancelButton);
  }

  @Override
  public boolean isSelectPathButtonInFocus() {
    return isWidgetOrChildFocused(selectPathButton);
  }

  @Override
  public boolean isWholeWordsOnly() {
    return wholeWordsOnly.getValue();
  }

  @Override
  public void showSelectPathDialog() {
    selectPathPresenter.show(delegate);
  }

  private void createButtons() {
    cancelButton = addFooterButton(locale.cancel(), "search-cancel-button", event -> close());

    acceptButton =
        addFooterButton(
            locale.search(), "search-button", event -> delegate.search(text.getText()), true);
  }

  private void addHandlers() {
    isUseFileMask.addValueChangeHandler(event -> filesMask.setEnabled(event.getValue()));

    isUseDirectory.addValueChangeHandler(
        event -> {
          directory.setEnabled(event.getValue());
          selectPathButton.setEnabled(event.getValue());
        });

    text.addKeyUpHandler(event -> acceptButton.setEnabled(!text.getValue().isEmpty()));

    selectPathButton.addClickHandler(event -> showSelectPathDialog());
  }
}
