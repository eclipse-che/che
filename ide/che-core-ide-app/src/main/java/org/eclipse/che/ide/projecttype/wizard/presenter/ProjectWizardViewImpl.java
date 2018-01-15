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
package org.eclipse.che.ide.projecttype.wizard.presenter;

import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.CREATE;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.UPDATE;

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
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode;
import org.eclipse.che.ide.ui.window.Window;

/**
 * @author Evgen Vidolob
 * @author Oleksii Orel
 */
public class ProjectWizardViewImpl extends Window implements ProjectWizardView {
  private static ProjectWizardViewImplUiBinder ourUiBinder =
      GWT.create(ProjectWizardViewImplUiBinder.class);

  private final CoreLocalizationConstant locale;
  @UiField SimplePanel wizardPanel;
  Button nextStepButton;
  Button previousStepButton;
  Button saveButton;

  private boolean isCreatingNewProject;
  private ActionDelegate delegate;

  @Inject
  public ProjectWizardViewImpl(
      org.eclipse.che.ide.Resources resources, CoreLocalizationConstant coreLocalizationConstant) {
    this.locale = coreLocalizationConstant;
    setTitle(coreLocalizationConstant.projectWizardDefaultTitleText());
    setWidget(ourUiBinder.createAndBindUi(this));

    saveButton =
        createPrimaryButton(
            locale.projectWizardDefaultSaveButtonText(),
            "projectWizard-saveButton",
            new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                delegate.onSaveClicked();
              }
            });
    saveButton.addStyleName(resources.Css().buttonLoader());
    addButtonToFooter(saveButton);

    nextStepButton =
        createButton(
            locale.next(),
            "projectWizard-nextStepButton",
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
            "projectWizard-previousStepButton",
            new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                delegate.onBackClicked();
              }
            });
    addButtonToFooter(previousStepButton);

    this.ensureDebugId("projectWizard-window");

    getWidget().getElement().getStyle().setPadding(0, Style.Unit.PX);
  }

  @Override
  public void setLoaderVisibility(boolean visible) {
    if (visible) {
      saveButton.setHTML("<i></i>");
      saveButton.setEnabled(false);
    } else {
      if (isCreatingNewProject) {
        saveButton.setText(locale.projectWizardDefaultSaveButtonText());
      } else {
        saveButton.setText(locale.projectWizardSaveButtonText());
      }
      saveButton.setEnabled(true);
    }
  }

  @Override
  public void showPage(Presenter presenter) {
    presenter.go(wizardPanel);
  }

  @Override
  public void showDialog(ProjectWizardMode wizardMode) {
    this.isCreatingNewProject = wizardMode == CREATE;

    if (wizardMode == CREATE) {
      setTitle(locale.projectWizardDefaultTitleText());
      saveButton.setText(locale.projectWizardDefaultSaveButtonText());
    } else if (wizardMode == UPDATE) {
      setTitle(locale.projectWizardTitleText());
      saveButton.setText(locale.projectWizardSaveButtonText());
    }

    show();
  }

  @Override
  protected void onEnterClicked() {
    if (isWidgetFocused(saveButton)) {
      delegate.onSaveClicked();
      return;
    }

    if (isWidgetFocused(nextStepButton)) {
      delegate.onNextClicked();
      return;
    }

    if (isWidgetFocused(previousStepButton)) {
      delegate.onBackClicked();
      return;
    }

    if (nextStepButton.isEnabled()) {
      delegate.onNextClicked();
      return;
    }

    if (saveButton.isEnabled()) {
      delegate.onSaveClicked();
    }
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
  public void setFinishButtonEnabled(boolean enabled) {
    saveButton.setEnabled(enabled);
  }

  @Override
  public void setPreviousButtonEnabled(boolean enabled) {
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

  interface ProjectWizardViewImplUiBinder extends UiBinder<FlowPanel, ProjectWizardViewImpl> {}
}
