/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ui.dialogs.input;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.dialogs.CancelCallback;
import org.eclipse.che.ide.api.dialogs.InputCallback;
import org.eclipse.che.ide.api.dialogs.InputDialog;
import org.eclipse.che.ide.api.dialogs.InputValidator;
import org.eclipse.che.ide.ui.UILocalizationConstant;

import javax.validation.constraints.NotNull;

/**
 * {@link InputDialog} implementation.
 *
 * @author Mickaël Leduque
 * @author Artem Zatsarynnyi
 */
public class InputDialogPresenter implements InputDialog, InputDialogView.ActionDelegate {

    /** This component view. */
    private final InputDialogView view;

    /** The callback used on OK. */
    private final InputCallback inputCallback;

    /** The callback used on cancel. */
    private final CancelCallback cancelCallback;

    private final UILocalizationConstant localizationConstant;
    /** Validator for validating the user's input. */
    private       InputValidator         inputValidator;

    @AssistedInject
    public InputDialogPresenter(final @NotNull InputDialogView view,
                                final @NotNull @Assisted("title") String title,
                                final @NotNull @Assisted("label") String label,
                                final @Nullable @Assisted InputCallback inputCallback,
                                final @Nullable @Assisted CancelCallback cancelCallback,
                                final UILocalizationConstant localizationConstant) {
        this(view, title, label, "", 0, 0, inputCallback, cancelCallback, localizationConstant);
    }

    @AssistedInject
    public InputDialogPresenter(final @NotNull InputDialogView view,
                                final @NotNull @Assisted("title") String title,
                                final @NotNull @Assisted("label") String label,
                                final @NotNull @Assisted("initialValue") String initialValue,
                                final @NotNull @Assisted("selectionStartIndex") Integer selectionStartIndex,
                                final @NotNull @Assisted("selectionLength") Integer selectionLength,
                                final @Nullable @Assisted InputCallback inputCallback,
                                final @Nullable @Assisted CancelCallback cancelCallback,
                                final UILocalizationConstant localizationConstant) {
        this.view = view;
        this.view.setContent(label);
        this.view.setTitle(title);
        this.view.setValue(initialValue);
        this.view.setSelectionStartIndex(selectionStartIndex);
        this.view.setSelectionLength(selectionLength);
        this.inputCallback = inputCallback;
        this.cancelCallback = cancelCallback;
        this.view.setDelegate(this);
        this.localizationConstant = localizationConstant;
    }

    @AssistedInject
    public InputDialogPresenter(final @NotNull InputDialogView view,
                                final @NotNull @Assisted("title") String title,
                                final @NotNull @Assisted("label") String label,
                                final @NotNull @Assisted("initialValue") String initialValue,
                                final @NotNull @Assisted("selectionStartIndex") Integer selectionStartIndex,
                                final @NotNull @Assisted("selectionLength") Integer selectionLength,
                                final @NotNull @Assisted("okButtonLabel") String okButtonLabel,
                                final @Nullable @Assisted InputCallback inputCallback,
                                final @Nullable @Assisted CancelCallback cancelCallback,
                                final UILocalizationConstant localizationConstant) {
        this.view = view;
        this.view.setContent(label);
        this.view.setTitle(title);
        this.view.setValue(initialValue);
        this.view.setSelectionStartIndex(selectionStartIndex);
        this.view.setSelectionLength(selectionLength);
        this.view.setOkButtonLabel(okButtonLabel);
        this.inputCallback = inputCallback;
        this.cancelCallback = cancelCallback;
        this.view.setDelegate(this);
        this.localizationConstant = localizationConstant;
    }

    @Override
    public void cancelled() {
        view.closeDialog();
        if (cancelCallback != null) {
            cancelCallback.cancelled();
        }
    }

    @Override
    public void accepted() {
        view.closeDialog();
        if (inputCallback != null) {
            inputCallback.accepted(view.getValue());
        }
    }

    @Override
    public void inputValueChanged() {
        isInputValid();
    }

    @Override
    public void onEnterClicked() {
        if (view.isOkButtonInFocus()) {
            accepted();
            return;
        }

        if (view.isCancelButtonInFocus()) {
            cancelled();
            return;
        }

        if (isInputValid()) {
            accepted();
        }
    }

    @Override
    public void show() {
        isInputValid();
        view.showDialog();
    }

    @Override
    public InputDialog withValidator(InputValidator inputValidator) {
        this.inputValidator = inputValidator;
        return this;
    }

    private boolean isInputValid() {
        String currentValue = view.getValue();
        if (currentValue.trim().isEmpty()) {
            view.showErrorHint("");
            return false;
        }

        InputValidator.Violation violation = null;

        if (inputValidator != null) {
            violation = inputValidator.validate(currentValue);
        }

        if (violation == null) {
            view.hideErrorHint();
            return true;
        }

        String correctValue = violation.getCorrectedValue();
        if (correctValue != null) {
            view.setValue(correctValue);
            view.hideErrorHint();
            return true;
        }

        String errorMessage = violation.getMessage();
        if (errorMessage == null) {
            view.hideErrorHint();
            return true;
        }

        view.showErrorHint(errorMessage);
        return false;
    }
}
