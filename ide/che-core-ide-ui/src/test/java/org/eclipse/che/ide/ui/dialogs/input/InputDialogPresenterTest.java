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

import org.eclipse.che.ide.api.dialogs.InputValidator;
import org.eclipse.che.ide.ui.UILocalizationConstant;
import org.eclipse.che.ide.ui.dialogs.BaseTest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testing {@link InputDialogPresenter} functionality.
 *
 * @author Artem Zatsarynnyi
 * @author Roman Nikitenko
 */
public class InputDialogPresenterTest extends BaseTest {
    private static final String CORRECT_INPUT_VALUE = "testInputValue";
    private static final String INCORRECT_INPUT_VALUE = "test input value";
    private static final String ERROR_MESSAGE = "ERROR";

    @Mock
    UILocalizationConstant localizationConstant;
    @Mock
    private InputDialogView      view;
    private InputDialogPresenter presenter;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        presenter = new InputDialogPresenter(view, TITLE, MESSAGE, inputCallback, cancelCallback, localizationConstant);
    }

    @Test
    public void shouldCallCallbackOnCanceled() throws Exception {
        presenter.cancelled();

        verify(view).closeDialog();
        verify(cancelCallback).cancelled();
    }

    @Test
    public void shouldNotCallCallbackOnCanceled() throws Exception {
        presenter = new InputDialogPresenter(view, TITLE, MESSAGE, inputCallback, null, localizationConstant);

        presenter.cancelled();

        verify(view).closeDialog();
        verify(cancelCallback, never()).cancelled();
    }

    @Test
    public void shouldCallCallbackOnAccepted() throws Exception {
        presenter.accepted();

        verify(view).closeDialog();
        verify(view).getValue();
        verify(inputCallback).accepted(anyString());
    }

    @Test
    public void shouldNotCallCallbackOnAccepted() throws Exception {
        presenter = new InputDialogPresenter(view, TITLE, MESSAGE, null, cancelCallback, localizationConstant);

        presenter.accepted();

        verify(view).closeDialog();
        verify(inputCallback, never()).accepted(anyString());
    }

    @Test
    public void shouldShowView() throws Exception {
        when(view.getValue()).thenReturn(CORRECT_INPUT_VALUE);

        presenter.show();

        verify(view).showDialog();
    }

    @Test
    public void shouldShowErrorHintWhenEmptyValue() throws Exception {
        reset(view);
        when(view.getValue()).thenReturn("");

        presenter.inputValueChanged();

        verify(view).showErrorHint(eq(""));
        verify(view, never()).hideErrorHint();
        verify(view, never()).setValue(anyString());
    }

    @Test
    public void shouldShowErrorHintWhenValueIsIncorrect() throws Exception {
        reset(view);
        when(view.getValue()).thenReturn(INCORRECT_INPUT_VALUE);
        InputValidator inputValidator = mock(InputValidator.class);
        InputValidator.Violation violation = mock(InputValidator.Violation.class);
        when(inputValidator.validate(INCORRECT_INPUT_VALUE)).thenReturn(violation);
        when(violation.getMessage()).thenReturn(ERROR_MESSAGE);

        presenter.withValidator(inputValidator);
        presenter.inputValueChanged();

        verify(view).showErrorHint(anyString());
        verify(view, never()).hideErrorHint();
        verify(view, never()).setValue(anyString());
    }

    @Test
    public void shouldNotShowErrorHintWhenViolationHasCorrectValue() throws Exception {
        reset(view);
        when(view.getValue()).thenReturn(INCORRECT_INPUT_VALUE);
        InputValidator inputValidator = mock(InputValidator.class);
        InputValidator.Violation violation = mock(InputValidator.Violation.class);
        when(inputValidator.validate(INCORRECT_INPUT_VALUE)).thenReturn(violation);
        when(violation.getMessage()).thenReturn(null);
        when(violation.getCorrectedValue()).thenReturn(CORRECT_INPUT_VALUE);

        presenter.withValidator(inputValidator);
        presenter.inputValueChanged();

        verify(view, never()).showErrorHint(anyString());
        verify(view).hideErrorHint();
        verify(view).setValue(eq(CORRECT_INPUT_VALUE));
    }

    @Test
    public void onEnterClickedWhenOkButtonInFocus() throws Exception {
        reset(view);
        when(view.isOkButtonInFocus()).thenReturn(true);

        presenter.onEnterClicked();

        verify(view, never()).showErrorHint(anyString());
        verify(view).closeDialog();
        verify(inputCallback).accepted(anyString());
    }

    @Test
    public void onEnterClickedWhenCancelButtonInFocus() throws Exception {
        reset(view);
        when(view.isCancelButtonInFocus()).thenReturn(true);

        presenter.onEnterClicked();

        verify(view, never()).showErrorHint(anyString());
        verify(inputCallback, never()).accepted(anyString());
        verify(view).closeDialog();
        verify(cancelCallback).cancelled();
    }

    @Test
    public void onEnterClickedWhenInputValueIsCorrect() throws Exception {
        reset(view);
        when(view.getValue()).thenReturn(CORRECT_INPUT_VALUE);
        InputValidator inputValidator = mock(InputValidator.class);
        when(inputValidator.validate(CORRECT_INPUT_VALUE)).thenReturn(null);

        presenter.withValidator(inputValidator);
        presenter.onEnterClicked();

        verify(view, never()).showErrorHint(anyString());
        verify(view).hideErrorHint();
        verify(view).closeDialog();
        verify(inputCallback).accepted(eq(CORRECT_INPUT_VALUE));
    }

    @Test
    public void onEnterClickedWhenInputValueIsIncorrect() throws Exception {
        reset(view);
        when(view.getValue()).thenReturn(INCORRECT_INPUT_VALUE);
        InputValidator inputValidator = mock(InputValidator.class);
        InputValidator.Violation violation = mock(InputValidator.Violation.class);
        when(inputValidator.validate(INCORRECT_INPUT_VALUE)).thenReturn(violation);
        when(violation.getMessage()).thenReturn(ERROR_MESSAGE);

        presenter.withValidator(inputValidator);
        presenter.onEnterClicked();

        verify(view).showErrorHint(anyString());
        verify(view, never()).hideErrorHint();
        verify(view, never()).setValue(anyString());
        verify(inputCallback, never()).accepted(anyString());
    }
}
