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
package org.eclipse.che.ide.ext.debugger.client.debug;

import org.eclipse.che.ide.ext.debugger.shared.Variable;
import org.eclipse.che.ide.ext.debugger.shared.VariablePath;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class DebuggerVariableTest {

    private final static String SOME_TEXT = "someText";

    //constructor mock
    @Mock
    private Variable variable;

    //additional mocks
    @Mock
    private Variable         testVariable1;
    @Mock
    private Variable         testVariable2;
    @Mock
    private DebuggerVariable debuggerVariable1;
    @Mock
    private DebuggerVariable debuggerVariable2;

    @Captor
    private ArgumentCaptor<List<Variable>> variableListCaptor;

    private DebuggerVariable debuggerVariable;

    @Before
    public void setUp() {
        debuggerVariable = new DebuggerVariable(variable);
    }

    @Test
    public void variableShouldBeReturned() {
        Variable testVariable = debuggerVariable.getVariable();

        assertThat(testVariable, sameInstance(variable));
    }

    @Test
    public void variableNameShouldBeReturned() {
        debuggerVariable.getName();

        verify(variable).getName();
    }

    @Test
    public void nameShouldBeSet() {
        debuggerVariable.setName(SOME_TEXT);

        verify(variable).setName(SOME_TEXT);
    }

    @Test
    public void valueShouldBeReturned() {
        debuggerVariable.getValue();

        verify(variable).getValue();
    }

    @Test
    public void valueShouldBeSet() {
        debuggerVariable.setValue(SOME_TEXT);

        verify(variable).setValue(SOME_TEXT);
    }

    @Test
    public void typeShouldBeReturned() {
        debuggerVariable.getType();

        verify(variable).getType();
    }

    @Test
    public void typeShouldBeSet() {
        debuggerVariable.setType(SOME_TEXT);

        verify(variable).setType(SOME_TEXT);
    }

    @Test
    public void pathShouldBeReturned() {
        debuggerVariable.getVariablePath();

        verify(variable).getVariablePath();
    }

    @Test
    public void isPrimitiveShouldBeReturned() {
        debuggerVariable.isPrimitive();

        verify(variable).isPrimitive();
    }

    @Test
    public void listDebuggerVariablesShouldBeReturned() {
        when(variable.getVariables()).thenReturn(Arrays.asList(testVariable1, testVariable2));

        List<DebuggerVariable> debuggerVariables = debuggerVariable.getVariables();

        assertThat(debuggerVariables.size(), equalTo(2));
        assertThat(debuggerVariables.get(0).getVariable(), sameInstance(testVariable1));
        assertThat(debuggerVariables.get(1).getVariable(), sameInstance(testVariable2));
    }

    @Test
    public void debuggerVariablesListShouldBeSet() {
        when(debuggerVariable1.getVariable()).thenReturn(testVariable1);
        when(debuggerVariable2.getVariable()).thenReturn(testVariable2);


        debuggerVariable.setVariables(Arrays.asList(debuggerVariable1, debuggerVariable2));

        verify(debuggerVariable1).getVariable();
        verify(debuggerVariable2).getVariable();

        verify(variable).setVariables(variableListCaptor.capture());

        List<Variable> variables = variableListCaptor.getValue();

        assertThat(variables.size(), equalTo(2));
        assertThat(variables.contains(testVariable1), is(true));
        assertThat(variables.contains(testVariable2), is(true));
    }

    @Test
    public void hashCodeShouldBeReturned() {
        //noinspection ResultOfMethodCallIgnored
        debuggerVariable.hashCode();

        verify(variable).getName();
        verify(variable).getValue();
    }

    @Test
    public void objectShouldNotBeEqualsWhenObjectIsNull() {
        boolean isEquals = debuggerVariable.equals(nullValue());

        assertThat(isEquals, is(false));
    }

    @Test
    public void objectShouldNotBeEqualsWhenObjectIsOtherClass() {
        DebuggerView testObject = mock(DebuggerView.class);

        boolean isEquals = debuggerVariable.equals(testObject);

        assertThat(isEquals, is(false));
    }

    @Test
    public void objectShouldBeEqualsWhenObjectsHaveSameReference() {
        boolean isEquals = debuggerVariable.equals(debuggerVariable);

        assertThat(isEquals, is(true));
    }

    @Test
    public void objectShouldBeEquals() {
        VariablePath path = mock(VariablePath.class);
        DebuggerVariable testVariable = new DebuggerVariable(variable);

        when(variable.getName()).thenReturn(SOME_TEXT);
        when(variable.getValue()).thenReturn(SOME_TEXT);
        when(variable.getVariablePath()).thenReturn(path);

        boolean isEquals = debuggerVariable.equals(testVariable);

        assertThat(isEquals, is(true));
    }

}