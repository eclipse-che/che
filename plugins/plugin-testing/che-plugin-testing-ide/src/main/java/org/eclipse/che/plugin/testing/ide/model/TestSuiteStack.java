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
package org.eclipse.che.plugin.testing.ide.model;

import java.util.ArrayDeque;
import java.util.Deque;

/** Data stack of the test's suites. */
public class TestSuiteStack {

  private Deque<TestState> stack = new ArrayDeque<>();

  /**
   * Pushes an element onto the stack at the head of this deque.
   *
   * @param state new test state
   */
  public void push(TestState state) {
    stack.push(state);
  }

  /**
   * Inserts the specified element at the tail of this deque.
   *
   * @param state new test state
   */
  public void add(TestState state) {
    stack.add(state);
  }

  /**
   * Retrieves, but does not remove, the last element of this deque, or returns {@code null} if this
   * deque is empty.
   */
  public TestState peekLast() {
    return stack.peekLast();
  }

  /**
   * Retrieves, but does not remove, the head of the queue represented by this deque (in other
   * words, the first element of this deque), or returns {@code null} if this deque is empty.
   */
  public TestState getCurrent() {
    return stack.peek();
  }

  /**
   * Pops an element from the stack represented by this deque. In other words, removes and returns
   * the first element of this deque.
   *
   * @param suiteName name of suite
   * @return element from the stack which has same name
   */
  public TestState pop(String suiteName) {
    if (stack.isEmpty()) {
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

  /**
   * Returns <tt>true</tt> if this collection contains no elements.
   *
   * @return <tt>true</tt> if this collection contains no elements
   */
  public boolean isEmpty() {
    return stack.isEmpty();
  }

  /**
   * Removes all of the elements from this collection (optional operation). The collection will be
   * empty after this method returns.
   */
  public void clear() {
    stack.clear();
  }
}
