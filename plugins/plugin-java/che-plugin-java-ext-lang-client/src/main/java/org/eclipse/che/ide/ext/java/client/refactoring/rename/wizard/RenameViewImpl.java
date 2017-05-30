/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.client.refactoring.rename.wizard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.theme.Style;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.refactoring.rename.wizard.similarnames.SimilarNamesConfigurationPresenter;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatusEntry;
import org.eclipse.che.ide.ui.window.Window;

import java.util.List;

import static com.google.gwt.dom.client.Style.Cursor.DEFAULT;
import static com.google.gwt.dom.client.Style.Cursor.POINTER;

/**
 * @author Valeriy Svydenko
 */
@Singleton
final class RenameViewImpl extends Window implements RenameView {
    interface RenameViewImplUiBinder extends UiBinder<Widget, RenameViewImpl> {
    }

    private static RenameViewImplUiBinder UI_BINDER = GWT.create(RenameViewImplUiBinder.class);

    private final JavaResources javaResources;

    @UiField(provided = true)
    final JavaLocalizationConstant locale;

    @UiField
    FlowPanel   headerPanelToHide;
    @UiField
    TextBox     newName;
    @UiField
    FlowPanel   subSettings;
    @UiField
    CheckBox    updateSubpackages;
    @UiField
    FlowPanel   renameSubpackagesPanel;
    @UiField
    FlowPanel   renameKeepOriginalMethodPanel;
    @UiField
    CheckBox    updateOccurrences;
    @UiField
    Label       configureLabel;
    @UiField
    FlowPanel   renameSimilarlyVariablesAndMethodsPanel;
    @UiField
    CheckBox    updateSimilarlyVariables;
    @UiField
    CheckBox    updateDelegateUpdating;
    @UiField
    CheckBox    updateMarkDeprecated;
    @UiField
    FlowPanel   occurrencesPanel;
    @UiField
    FlowPanel   fullNamePanel;
    @UiField
    SimplePanel icon;
    @UiField
    TextBox     patternField;
    @UiField
    CheckBox    updateFullNames;
    @UiField
    CheckBox    updateReferences;
    @UiField
    FlowPanel   patternsPanelToHide;
    @UiField
    Label       errorLabel;

    private ActionDelegate delegate;
    private Button         preview;
    private Button         accept;

    @Inject
    public RenameViewImpl(JavaLocalizationConstant locale,
                          JavaResources javaResources,
                          final SimilarNamesConfigurationPresenter similarNamesConfigurationPresenter) {
        this.locale = locale;
        this.javaResources = javaResources;

        setWidget(UI_BINDER.createAndBindUi(this));

        createButtons(locale);

        updateFullNames.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                patternField.setEnabled(event.getValue());
            }
        });

        updateDelegateUpdating.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                updateMarkDeprecated.setEnabled(event.getValue());
            }
        });

        configureLabel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (isUpdateSimilarlyVariables()) {
                    similarNamesConfigurationPresenter.show();
                }
            }
        });

        newName.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                delegate.validateName();
            }
        });

        updateSimilarlyVariables.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (event.getValue()) {
                    configureLabel.getElement().getStyle().setCursor(POINTER);
                    configureLabel.getElement().getStyle().setColor(Style.getPrimaryHighlightsColor());
                } else {
                    configureLabel.getElement().getStyle().setCursor(DEFAULT);
                    configureLabel.getElement().getStyle().setColor(Style.getButtonDisabledFontColor());
                }
            }
        });
    }

    private void createButtons(JavaLocalizationConstant locale) {
        preview = createButton(locale.moveDialogButtonPreview(), "move-preview-button", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onPreviewButtonClicked();
            }
        });

        Button cancel = createButton(locale.moveDialogButtonCancel(), "move-cancel-button", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
                delegate.onCancelButtonClicked();
            }
        });

        accept = createButton(locale.moveDialogButtonOk(), "move-accept-button", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onAcceptButtonClicked();
            }
        });

        addButtonToFooter(accept);
        addButtonToFooter(cancel);
        addButtonToFooter(preview);
    }

    /** {@inheritDoc} */
    @Override
    public void show() {
        newName.getElement().setAttribute("spellcheck", "false");
        newName.addStyleName(javaResources.css().errorBorder());
        updateDelegateUpdating.setValue(false);
        updateMarkDeprecated.setValue(false);
        updateMarkDeprecated.setEnabled(false);

        super.show();

        new Timer() {
            @Override
            public void run() {
                setFocus();
            }
        }.schedule(100);
    }

    /** {@inheritDoc} */
    @Override
    protected void onClose() {
        delegate.onCancelButtonClicked();

        super.onClose();
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
    public void setFocus() {
        newName.selectAll();
        newName.setFocus(true);
    }

    /** {@inheritDoc} */
    @Override
    public void setEnablePreviewButton(boolean isEnable) {
        accept.setEnabled(isEnable);
    }

    /** {@inheritDoc} */
    @Override
    public void setEnableAcceptButton(boolean isEnable) {
        preview.setEnabled(isEnable);
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
        RefactoringStatusEntry statusEntry = getEntryMatchingSeverity(status.getSeverity(), status.getEntries());
        if (statusEntry != null) {
            errorLabel.setText(statusEntry.getMessage());
        } else {
            errorLabel.setText("");
        }
    }

    /**
     * Returns the first entry which severity is equal or greater than the
     * given severity. If more than one entry exists that matches the
     * criteria the first one is returned. Returns <code>null</code> if no
     * entry matches.
     *
     * @param severity
     *         the severity to search for. Must be one of <code>FATAL
     *         </code>, <code>ERROR</code>, <code>WARNING</code> or <code>INFO</code>
     * @param entries
     *         list of refactoring status
     * @return the entry that matches the search criteria
     */
    private RefactoringStatusEntry getEntryMatchingSeverity(int severity, List<RefactoringStatusEntry> entries) {
        for (RefactoringStatusEntry entry : entries) {
            if (entry.getSeverity() >= severity)
                return entry;
        }
        return null;
    }

}
