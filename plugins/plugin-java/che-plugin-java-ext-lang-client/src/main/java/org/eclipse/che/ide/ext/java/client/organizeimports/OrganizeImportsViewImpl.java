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
package org.eclipse.che.ide.ext.java.client.organizeimports;

import static com.google.gwt.dom.client.Style.Cursor.POINTER;
import static org.eclipse.che.ide.api.theme.Style.getEditorSelectionColor;
import static org.eclipse.che.ide.api.theme.Style.getMainFontColor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ui.window.Window;

/**
 * Implements of {@link OrganizeImportsView}.
 *
 * @author Valeriy Svydenko
 */
@Singleton
final class OrganizeImportsViewImpl extends Window implements OrganizeImportsView {
  interface OrganizeImportsViewImplUiBinder extends UiBinder<Widget, OrganizeImportsViewImpl> {}

  private static OrganizeImportsViewImplUiBinder UI_BINDER =
      GWT.create(OrganizeImportsViewImplUiBinder.class);

  @UiField VerticalPanel container;

  @UiField(provided = true)
  final JavaLocalizationConstant locale;

  private ActionDelegate delegate;
  private Button next;
  private Button back;
  private Button finish;

  private String selectedImport;
  private Label selectedLabel;

  @Inject
  public OrganizeImportsViewImpl(JavaLocalizationConstant locale) {
    this.locale = locale;
    setTitle(locale.organizeImportsName());
    setWidget(UI_BINDER.createAndBindUi(this));

    createButtons(locale);
  }

  /** {@inheritDoc} */
  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  /** {@inheritDoc} */
  @Override
  public void show(List<String> matches) {
    container.clear();
    for (String fqn : matches) {
      final Label label = new Label(fqn);
      if (fqn.equals(selectedImport)) {
        selectedLabel = label;
        selectedLabel.getElement().getStyle().setBackgroundColor(getEditorSelectionColor());
      }
      label.getElement().getStyle().setColor(getMainFontColor());
      label.getElement().getStyle().setCursor(POINTER);
      label.addClickHandler(
          clickEvent -> {
            selectedLabel.getElement().getStyle().setBackgroundColor("initial");
            selectedLabel = label;
            label.getElement().getStyle().setBackgroundColor(getEditorSelectionColor());
          });

      container.add(label);
    }

    show();
  }

  @Override
  public void close() {
    hide();
  }

  /** {@inheritDoc} */
  public String getSelectedImport() {
    return selectedLabel.getText();
  }

  /** {@inheritDoc} */
  @Override
  public void setSelectedImport(String fqn) {
    this.selectedImport = fqn;
  }

  /** {@inheritDoc} */
  @Override
  public void changePage(List<String> matches) {
    show(matches);
  }

  /** {@inheritDoc} */
  @Override
  public void setEnableFinishButton(boolean isEnable) {
    finish.setEnabled(isEnable);
  }

  /** {@inheritDoc} */
  @Override
  public void setEnableNextButton(boolean isEnable) {
    next.setEnabled(isEnable);
  }

  /** {@inheritDoc} */
  @Override
  public void setEnableBackButton(boolean isEnable) {
    back.setEnabled(isEnable);
  }

  private void createButtons(JavaLocalizationConstant locale) {
    back =
        addFooterButton(
            locale.organizeImportsButtonBack(),
            "imports-back-button",
            event -> delegate.onBackButtonClicked());

    next =
        addFooterButton(
            locale.organizeImportsButtonNext(),
            "imports-next-button",
            event -> delegate.onNextButtonClicked());

    addFooterButton(
        locale.organizeImportsButtonCancel(),
        "imports-cancel-button",
        event -> {
          hide();
          delegate.onCancelButtonClicked();
        });

    finish =
        addFooterButton(
            locale.organizeImportsButtonFinish(),
            "imports-finish-button",
            event -> delegate.onFinishButtonClicked(),
            true);
  }
}
