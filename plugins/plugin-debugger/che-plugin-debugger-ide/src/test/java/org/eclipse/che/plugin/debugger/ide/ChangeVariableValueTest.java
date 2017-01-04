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
package org.eclipse.che.plugin.debugger.ide;

import org.eclipse.che.api.debug.shared.dto.VariableDto;
import org.eclipse.che.api.debug.shared.dto.VariablePathDto;
import org.eclipse.che.api.debug.shared.model.MutableVariable;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerPresenter;
import org.eclipse.che.plugin.debugger.ide.debug.changevalue.ChangeValuePresenter;
import org.eclipse.che.plugin.debugger.ide.debug.changevalue.ChangeValueView;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.ArrayList;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testing {@link ChangeValuePresenter} functionality.
 *
 * @author Artem Zatsarynnyi
 */
public class ChangeVariableValueTest extends BaseTest {
    private static final String VAR_VALUE   = "var_value";
    private static final String VAR_NAME    = "var_name";
    private static final String EMPTY_VALUE = "";
    @Mock
    private ChangeValueView      view;
    @InjectMocks
    private ChangeValuePresenter presenter;
    @Mock
    private VariableDto          var;
    @Mock
    private VariablePathDto      varPath;
    @Mock
    private DebuggerManager      debuggerManager;
    @Mock
    private Debugger             debugger;
    @Mock
    private DebuggerPresenter    debuggerPresenter;
    @Mock
    private MutableVariable      variable;
    @Mock
    private VariablePathDto      variablePathDto;

    @Before
    public void setUp() {
        super.setUp();
        when(var.getName()).thenReturn(VAR_NAME);
        when(var.getValue()).thenReturn(VAR_VALUE);
        when(var.getVariablePath()).thenReturn(varPath);
        when(dtoFactory.createDto(VariableDto.class)).thenReturn(mock(VariableDto.class));
    }

    @Test
    public void shouldShowDialog() throws Exception {
        when(debuggerPresenter.getSelectedVariable()).thenReturn(variable);
        when(variable.getValue()).thenReturn(VAR_VALUE);

        presenter.showDialog();

        verify(debuggerPresenter).getSelectedVariable();
        verify(view).setValueTitle(constants.changeValueViewExpressionFieldTitle(VAR_NAME));
        verify(view).setValue(VAR_VALUE);
        verify(view).focusInValueField();
        verify(view).selectAllText();
        verify(view).setEnableChangeButton(eq(DISABLE_BUTTON));
        verify(view).showDialog();
    }

    @Test
    public void shouldCloseDialogOnCancelClicked() throws Exception {
        presenter.onCancelClicked();

        verify(view).close();
    }

    @Test
    public void shouldDisableChangeButtonIfNoValue() throws Exception {
        when(view.getValue()).thenReturn(EMPTY_VALUE);

        presenter.onVariableValueChanged();

        verify(view).setEnableChangeButton(eq(DISABLE_BUTTON));
    }

    @Test
    public void shouldEnableChangeButtonIfValueNotEmpty() throws Exception {
        when(view.getValue()).thenReturn(VAR_VALUE);

        presenter.onVariableValueChanged();

        verify(view).setEnableChangeButton(eq(!DISABLE_BUTTON));
    }

    @Test
    public void testChangeValueRequest() throws Exception {
        when(debuggerPresenter.getSelectedVariable()).thenReturn(variable);
        when(debuggerManager.getActiveDebugger()).thenReturn(debugger);
        when(view.getValue()).thenReturn(VAR_VALUE);
        when(variable.getVariablePath()).thenReturn(variablePathDto);
        when(variablePathDto.getPath()).thenReturn(new ArrayList<>());

        presenter.showDialog();
        presenter.onChangeClicked();

        verify(debugger).setValue(anyObject());
        verify(view).close();
    }
}
