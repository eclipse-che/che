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
package org.eclipse.che.ide.ext.java.client.refactoring.rename.wizard;

import static com.google.gwt.dom.client.Style.Cursor.DEFAULT;
import static com.google.gwt.dom.client.Style.Cursor.POINTER;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.theme.Style;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.refactoring.rename.wizard.similarnames.SimilarNamesConfigurationPresenter;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.jdt.ls.extension.api.dto.RefactoringStatus;
import org.eclipse.che.jdt.ls.extension.api.dto.RefactoringStatusEntry;

/** @author Valeriy Svydenko */
@Singleton
final class RenameViewImpl extends Window implements RenameView {
  interface RenameViewImplUiBinder extends UiBinder<Widget, RenameViewImpl> {}

  private static RenameViewImplUiBinder UI_BINDER = GWT.create(RenameViewImplUiBinder.class);

  private static final int VALIDATE_NAME_DELAY = 300;

  private JavaResources javaResources;
  private Resources ideResources;
  private final Timer validateNameTimer =
      new Timer() {
        @Override
        public void run() {
          delegate.validateName();
        }
      };

  @UiField(provided = true)
  final JavaLocalizationConstant locale;

  @UiField TextBox newName;
  @UiField CheckBox updateSubpackages;
  @UiField FlowPanel renameSubpackagesPanel;
  @UiField FlowPanel renameKeepOriginalMethodPanel;
  @UiField CheckBox updateOccurrences;
  @UiField Label configureLabel;
  @UiField FlowPanel renameSimilarlyVariablesAndMethodsPanel;
  @UiField CheckBox updateSimilarlyVariables;
  @UiField CheckBox updateDelegateUpdating;
  @UiField CheckBox updateMarkDeprecated;
  @UiField FlowPanel fullNamePanel;
  @UiField TextBox patternField;
  @UiField CheckBox updateFullNames;
  @UiField CheckBox updateReferences;
  @UiField FlowPanel patternsPanelToHide;
  @UiField Label errorLabel;

  private ActionDelegate delegate;
  private Button preview;
  private Button accept;

  @Inject
  public RenameViewImpl(
      JavaLocalizationConstant locale,
      JavaResources javaResources,
      Resources ideResources,
      final SimilarNamesConfigurationPresenter similarNamesConfigurationPresenter) {
    this.locale = locale;
    this.javaResources = javaResources;
    this.ideResources = ideResources;

    setWidget(UI_BINDER.createAndBindUi(this));

    createButtons(locale);

    updateFullNames.addValueChangeHandler(event -> patternField.setEnabled(event.getValue()));

    updateDelegateUpdating.addValueChangeHandler(
        event -> updateMarkDeprecated.setEnabled(event.getValue()));

    configureLabel.addClickHandler(
        event -> {
          if (isUpdateSimilarlyVariables()) {
            similarNamesConfigurationPresenter.show();
          }
        });

    newName.addKeyUpHandler(
        event -> {
          // here need some delay to be sure input box initiated with given value
          // in manually testing hard to reproduce this problem but it reproduced with selenium
          // tests
          validateNameTimer.cancel();
          validateNameTimer.schedule(VALIDATE_NAME_DELAY);
        });

    updateSimilarlyVariables.addValueChangeHandler(
        event -> {
          if (event.getValue()) {
            configureLabel.getElement().getStyle().setCursor(POINTER);
            configureLabel.getElement().getStyle().setColor(Style.getPrimaryHighlightsColor());
          } else {
            configureLabel.getElement().getStyle().setCursor(DEFAULT);
            configureLabel.getElement().getStyle().setColor(Style.getButtonDisabledFontColor());
          }
        });
  }

  private void createButtons(JavaLocalizationConstant locale) {
    preview =
        addFooterButton(
            locale.moveDialogButtonPreview(),
            "move-preview-button",
            event -> delegate.onPreviewButtonClicked());

    addFooterButton(
        locale.moveDialogButtonCancel(),
        "move-cancel-button",
        event -> {
          hide();
          delegate.onCancelButtonClicked();
        });

    accept =
        addFooterButton(
            locale.moveDialogButtonOk(),
            "move-accept-button",
            event -> delegate.onAcceptButtonClicked());
    accept.addStyleName(ideResources.buttonLoaderCss().buttonLoader());
  }

  /** {@inheritDoc} */
  @Override
  public void showDialog() {
    newName.getElement().setAttribute("spellcheck", "false");
    newName.addStyleName(javaResources.css().errorBorder());
    updateDelegateUpdating.setValue(false);
    updateMarkDeprecated.setValue(false);
    updateMarkDeprecated.setEnabled(false);

    super.show(newName);
  }

  @Override
  public void onEnterPress(NativeEvent evt) {
    if (accept.isEnabled()) {
      accept.click();
    }
  }

  @Override
  protected void onShow() {
    newName.selectAll();
  }

  @Override
  public void setTitleCaption(String title) {
    setTitle(title);
  }

  @Override
  protected void onHide() {
    delegate.onCancelButtonClicked();
  }

  @Override
  public void close() {
    hide();
  }

  /** {@inheritDoc} */
  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  /** {@inheritDoc} */
  @Override
  public String getNewName() {
    return newName.getText();
  }

  /** {@inheritDoc} */
  @Override
  public void setOldName(String name) {
    newName.setText(name);
  }

  /** {@inheritDoc} */
  @Override
  public void clearErrorLabel() {
    newName.removeStyleName(javaResources.css().errorBorder());
    errorLabel.setText("");
  }

  /** {@inheritDoc} */
  @Override
  public void setVisibleFullQualifiedNamePanel(boolean isVisible) {
    fullNamePanel.setVisible(isVisible);
  }

  /** {@inheritDoc} */
  @Override
  public void setVisiblePatternsPanel(boolean isVisible) {
    patternsPanelToHide.setVisible(isVisible);
  }

  /** {@inheritDoc} */
  @Override
  public void setVisibleKeepOriginalPanel(boolean isVisible) {
    renameKeepOriginalMethodPanel.setVisible(isVisible);
  }

  /** {@inheritDoc} */
  @Override
  public void setVisibleRenameSubpackagesPanel(boolean isVisible) {
    renameSubpackagesPanel.setVisible(isVisible);
  }

  /** {@inheritDoc} */
  @Override
  public void setVisibleSimilarlyVariablesPanel(boolean isVisible) {
    renameSimilarlyVariablesAndMethodsPanel.setVisible(isVisible);
  }

  /** {@inheritDoc} */
  @Override
  public void showStatusMessage(RefactoringStatus status) {
    errorLabel.getElement().getStyle().setColor(Style.getMainFontColor());
    showMessage(status);
  }

  /** {@inheritDoc} */
  @Override
  public void showErrorMessage(RefactoringStatus status) {
    newName.addStyleName(javaResources.css().errorBorder());
    errorLabel.getElement().getStyle().setColor(Style.getErrorColor());
    showMessage(status);
  }

  @Override
  public void setFocus() {}

  /** {@inheritDoc} */
  @Override
  public void setEnablePreviewButton(boolean isEnable) {
    preview.setEnabled(isEnable);
  }

  /** {@inheritDoc} */
  @Override
  public void setEnableAcceptButton(boolean isEnable) {
    accept.setEnabled(isEnable);
  }

  @Override
  public void setLoaderVisibility(boolean isVisible) {
    if (isVisible) {
      accept.setHTML("<i></i>");
      accept.setEnabled(false);
    } else {
      accept.setText(locale.moveDialogButtonOk());
      accept.setEnabled(true);
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean isUpdateReferences() {
    return updateReferences.getValue();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isUpdateDelegateUpdating() {
    return updateDelegateUpdating.getValue();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isUpdateMarkDeprecated() {
    return updateMarkDeprecated.getValue();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isUpdateSubpackages() {
    return updateSubpackages.getValue();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isUpdateTextualOccurrences() {
    return updateOccurrences.getValue();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isUpdateQualifiedNames() {
    return updateFullNames.getValue();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isUpdateSimilarlyVariables() {
    return updateSimilarlyVariables.getValue();
  }

  /** {@inheritDoc} */
  @Override
  public String getFilePatterns() {
    return patternField.getValue();
  }

  private void showMessage(RefactoringStatus status) {
    RefactoringStatusEntry statusEntry =
        getEntryMatchingSeverity(
            status.getRefactoringSeverity().getValue(), status.getRefactoringStatusEntries());
    if (statusEntry != null) {
      errorLabel.setText(statusEntry.getMessage());
    } else {
      errorLabel.setText("");
    }
  }

  /**
   * Returns the first entry which severity is equal or greater than the given severity. If more
   * than one entry exists that matches the criteria the first one is returned. Returns <code>null
   * </code> if no entry matches.
   *
   * @param severity the severity to search for. Must be one of <code>FATAL
   *         </code>, <code>ERROR</code>, <code>WARNING</code> or <code>INFO</code>
   * @param entries list of refactoring status
   * @return the entry that matches the search criteria
   */
  private RefactoringStatusEntry getEntryMatchingSeverity(
      int severity, List<RefactoringStatusEntry> entries) {
    for (RefactoringStatusEntry entry : entries) {
      if (entry.getRefactoringSeverity().getValue() >= severity) return entry;
    }
    return null;
  }
}
