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
package org.eclipse.che.plugin.testing.ide.model;

import org.eclipse.che.ide.util.loging.Log;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Data stack of the test's suites.
 */
public class TestSuiteStack {

    private Deque<TestState> stack = new ArrayDeque<>();

    public void push(TestState state) {
        stack.push(state);
    }

    public TestState getCurrent() {
        return stack.peek();
    }


    public TestState pop(String suiteName) {
        if (stack.isEmpty()) {
            Log.error(getClass(), "Test suite stack is empty, unexpected suite name: " + suiteName);
            return null;
        }

        TestState state = stack.peek();

        if (!suiteName.equals(state.getName())) {

            TestState expectedState = null;
            for (TestState testState : stack) {
                if (suiteName.equals(testState.getName())) {
                    expectedState = testState;
                    break;
                }
            }

            if (expectedState != null) {

                TestState testState = state;
                while (testState != expectedState) {
                    testState = stack.pop();
                }

                return expectedState;
            } else {
                return null;
            }

        } else {
            stack.pop();
        }
        return state;
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public void clear() {
        stack.clear();
    }
}
