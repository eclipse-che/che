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
package org.eclipse.che.ide.projectimport.wizard.presenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.inject.Inject;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.mvp.Presenter;
import org.eclipse.che.ide.ui.window.Window;

/**
 * UI implementation for {@link ImportProjectWizardView}.
 *
 * @author Ann Shumilova
 */
public class ImportProjectWizardViewImpl extends Window implements ImportProjectWizardView {
  private static ImportProjectWizardViewImplUiBinder uiBinder =
      GWT.create(ImportProjectWizardViewImplUiBinder.class);

  @UiField SimplePanel wizardPanel;
  Button nextStepButton;
  Button previousStepButton;
  Button importButton;

  final CoreLocalizationConstant locale;

  private ActionDelegate delegate;

  @Inject
  public ImportProjectWizardViewImpl(
      org.eclipse.che.ide.Resources ideResources, CoreLocalizationConstant locale) {
    this.ensureDebugId("importProjectWizard-window");
    this.locale = locale;
    setTitle(locale.importProjectViewTitle());
    FlowPanel widget = uiBinder.createAndBindUi(this);
    widget.getElement().getStyle().setPadding(0, Style.Unit.PX);
    setWidget(widget);

    importButton =
        addFooterButton(
            locale.importProjectButton(),
            "importProjectWizard-importButton",
            event -> delegate.onImportClicked(),
            true);
    importButton.addStyleName(ideResources.buttonLoaderCss().buttonLoader());

    nextStepButton =
        addFooterButton(
            locale.next(), "importProjectWizard-nextStepButton", event -> delegate.onNextClicked());

    previousStepButton =
        addFooterButton(
            locale.back(),
            "importProjectWizard-previousStepButton",
            event -> delegate.onBackClicked());
  }

  @Override
  public void showPage(Presenter presenter) {
    wizardPanel.clear();
    presenter.go(wizardPanel);
  }

  @Override
  public void showDialog() {
    show();
  }

  @Override
  public void close() {
    hide();
    setLoaderVisibility(false);
  }

  @Override
  public void setNextButtonEnabled(boolean enabled) {
    nextStepButton.setEnabled(enabled);
  }

  @Override
  public void setImportButtonEnabled(boolean enabled) {
    importButton.setEnabled(enabled);
  }

  @Override
  public void setBackButtonEnabled(boolean enabled) {
    previousStepButton.setEnabled(enabled);
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  interface ImportProjectWizardViewImplUiBinder
      extends UiBinder<FlowPanel, ImportProjectWizardViewImpl> {}

  /** {@inheritDoc} */
  @Override
  public void setLoaderVisibility(boolean isVisible) {
    if (isVisible) {
      importButton.setHTML("<i></i>");
      importButton.setEnabled(false);
    } else {
      importButton.setText(locale.importProjectButton());
      importButton.setEnabled(true);
    }
  }
}
