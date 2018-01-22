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
package org.eclipse.che.ide.ui.dialogs.choice;

import static org.eclipse.che.ide.util.dom.DomUtils.isWidgetOrChildFocused;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.ui.window.Window;

/**
 * Implementation of the choice dialog view.
 *
 * @author Mickaël Leduque
 * @author Artem Zatsarynnyi
 */
public class ChoiceDialogViewImpl extends Window implements ChoiceDialogView {

  /** The UI binder instance. */
  private static ChoiceWindowUiBinder uiBinder = GWT.create(ChoiceWindowUiBinder.class);
  /** The window footer. */
  private final ChoiceDialogFooter footer;
  /** The container for the window content. */
  @UiField SimplePanel content;

  private ActionDelegate delegate;

  @Inject
  public ChoiceDialogViewImpl(final @NotNull ChoiceDialogFooter footer) {
    Widget widget = uiBinder.createAndBindUi(this);
    setWidget(widget);

    this.footer = footer;
    addFooterWidget(footer);
  }

  @Override
  public void setDelegate(final ActionDelegate delegate) {
    this.delegate = delegate;
    this.footer.setDelegate(this.delegate);
  }

  @Override
  public void setTitleCaption(String title) {
    setTitle(title);
  }

  @Override
  public void onEnterPress(NativeEvent evt) {
    delegate.onEnterClicked();
  }

  @Override
  public void showDialog() {
    this.show(footer.firstChoiceButton);
  }

  @Override
  public void closeDialog() {
    this.hide();
  }

  @Override
  public void setContent(final IsWidget content) {
    this.content.clear();
    this.content.setWidget(content);
  }

  @Override
  public void setFirstChoiceLabel(final String firstChoiceLabel) {
    footer.firstChoiceButton.setText(firstChoiceLabel);
    footer.firstChoiceButton.setVisible(!firstChoiceLabel.isEmpty());
  }

  @Override
  public void setSecondChoiceLabel(final String secondChoiceLabel) {
    footer.secondChoiceButton.setText(secondChoiceLabel);
    footer.secondChoiceButton.setVisible(!secondChoiceLabel.isEmpty());
  }

  @Override
  public void setThirdChoiceLabel(final String thirdChoiceLabel) {
    footer.thirdChoiceButton.setText(thirdChoiceLabel);
    footer.thirdChoiceButton.setVisible(!thirdChoiceLabel.isEmpty());
  }

  @Override
  public boolean isFirstButtonInFocus() {
    return isWidgetOrChildFocused(footer.firstChoiceButton);
  }

  @Override
  public boolean isSecondButtonInFocus() {
    return isWidgetOrChildFocused(footer.secondChoiceButton);
  }

  @Override
  public boolean isThirdButtonInFocus() {
    return isWidgetOrChildFocused(footer.thirdChoiceButton);
  }

  /** The UI binder interface for this components. */
  interface ChoiceWindowUiBinder extends UiBinder<Widget, ChoiceDialogViewImpl> {}
}
