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
package org.eclipse.che.ide.projecttype.wizard.presenter;

import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.CREATE;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.UPDATE;
import static org.eclipse.che.ide.util.dom.DomUtils.isWidgetOrChildFocused;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
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
    FlowPanel widget = ourUiBinder.createAndBindUi(this);
    widget.getElement().getStyle().setPadding(0, Style.Unit.PX);
    setWidget(widget);

    saveButton =
        addFooterButton(
            locale.projectWizardDefaultSaveButtonText(),
            "projectWizard-saveButton",
            event -> delegate.onSaveClicked(),
            true);
    saveButton.addStyleName(resources.buttonLoaderCss().buttonLoader());

    nextStepButton =
        addFooterButton(
            locale.next(), "projectWizard-nextStepButton", event -> delegate.onNextClicked());

    previousStepButton =
        addFooterButton(
            locale.back(), "projectWizard-previousStepButton", event -> delegate.onBackClicked());

    ensureDebugId("projectWizard-window");
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
  public void onEnterPress(NativeEvent evt) {
    if (isWidgetOrChildFocused(saveButton)) {
      delegate.onSaveClicked();
    } else if (isWidgetOrChildFocused(nextStepButton)) {
      delegate.onNextClicked();
    } else if (isWidgetOrChildFocused(previousStepButton)) {
      delegate.onBackClicked();
    }

    if (nextStepButton.isEnabled()) {
      delegate.onNextClicked();
    } else if (saveButton.isEnabled()) {
      delegate.onSaveClicked();
    }
  }

  @Override
  public void close() {
    hide();
  }

  @Override
  protected void onHide() {
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

  interface ProjectWizardViewImplUiBinder extends UiBinder<FlowPanel, ProjectWizardViewImpl> {}
}
