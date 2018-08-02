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
package org.eclipse.che.ide.ext.java.client.newsourcefile;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ui.window.Window;

/**
 * Implementation of {@link NewJavaSourceFileView}.
 *
 * @author Artem Zatsarynnyi
 */
public class NewJavaSourceFileViewImpl extends Window implements NewJavaSourceFileView {
  final Button btnOk;
  @UiField TextBox nameField;
  @UiField Label errorHintField;
  @UiField ListBox typeField;
  private ActionDelegate delegate;

  private List<JavaSourceFileType> sourceFileTypes = new ArrayList<>();

  @Inject
  public NewJavaSourceFileViewImpl(
      AddToIndexViewImplUiBinder uiBinder, JavaLocalizationConstant constant) {
    setTitle(constant.title());

    addFooterButton(
        constant.buttonCancel(), "newJavaClass-dialog-cancel", event -> delegate.onCancelClicked());

    btnOk =
        addFooterButton(
            constant.buttonOk(), "newJavaClass-dialog-ok", event -> delegate.onOkClicked());

    Widget widget = uiBinder.createAndBindUi(this);
    this.setWidget(widget);
    this.ensureDebugId("newJavaSourceFileView-window");

    nameField.addKeyUpHandler(
        event -> {
          delegate.onNameChanged();
          final boolean isNameEmpty = nameField.getText().trim().isEmpty();
          btnOk.setEnabled(!isNameEmpty);
          if (!isNameEmpty && KeyCodes.KEY_ENTER == event.getNativeKeyCode()) {
            delegate.onOkClicked();
          }
        });
  }

  @Override
  public void setTypes(List<JavaSourceFileType> types) {
    sourceFileTypes.clear();
    typeField.clear();
    sourceFileTypes.addAll(types);
    for (JavaSourceFileType type : sourceFileTypes) {
      typeField.addItem(type.toString());
    }
  }

  @Override
  public String getName() {
    return nameField.getText();
  }

  @Override
  public JavaSourceFileType getSelectedType() {
    return sourceFileTypes.get(typeField.getSelectedIndex());
  }

  @Override
  public void close() {
    hide();
  }

  @Override
  public void showErrorHint(String text) {
    errorHintField.setText(text);
  }

  @Override
  public void hideErrorHint() {
    errorHintField.setText("");
  }

  @Override
  public void showDialog() {
    nameField.setText("");
    hideErrorHint();
    show(nameField);
    btnOk.setEnabled(false);
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  interface AddToIndexViewImplUiBinder extends UiBinder<Widget, NewJavaSourceFileViewImpl> {}
}
