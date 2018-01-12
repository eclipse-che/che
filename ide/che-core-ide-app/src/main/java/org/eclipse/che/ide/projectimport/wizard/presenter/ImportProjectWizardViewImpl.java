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
package org.eclipse.che.ide.projectimport.wizard.presenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
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

  @UiField(provided = true)
  final org.eclipse.che.ide.ui.window.Window.Resources resources;

  final CoreLocalizationConstant locale;

  private ActionDelegate delegate;

  @Inject
  public ImportProjectWizardViewImpl(
      org.eclipse.che.ide.Resources ideResources,
      org.eclipse.che.ide.ui.window.Window.Resources resources,
      CoreLocalizationConstant locale) {
    this.ensureDebugId("importProjectWizard-window");
    this.resources = resources;
    this.locale = locale;
    setTitle(locale.importProjectViewTitle());
    setWidget(uiBinder.createAndBindUi(this));

    importButton =
        createPrimaryButton(
            locale.importProjectButton(),
            "importProjectWizard-importButton",
            new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                delegate.onImportClicked();
              }
            });
    importButton.addStyleName(ideResources.Css().buttonLoader());

    addButtonToFooter(importButton);

    nextStepButton =
        createButton(
            locale.next(),
            "importProjectWizard-nextStepButton",
            new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                delegate.onNextClicked();
              }
            });

    addButtonToFooter(nextStepButton);

    previousStepButton =
        createButton(
            locale.back(),
            "importProjectWizard-previousStepButton",
            new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                delegate.onBackClicked();
              }
            });

    addButtonToFooter(previousStepButton);

    getWidget().getElement().getStyle().setPadding(0, Style.Unit.PX);
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

  @Override
  public Widget asWidget() {
    return null;
  }

  @Override
  protected void onClose() {
    delegate.onCancelClicked();
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
