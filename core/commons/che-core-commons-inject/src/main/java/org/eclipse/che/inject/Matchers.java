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
package org.eclipse.che.inject;

import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;

import org.aopalliance.intercept.MethodInterceptor;

import java.lang.reflect.Method;

import static java.util.Objects.requireNonNull;

/**
 * Matcher implementations. Supports matching methods.
 * It can be used for binding of {@link MethodInterceptor}.
 * <p/>
 * Example of usage:
 * <code> bindInterceptor(com.google.inject.matcher.Matchers.subclassesOf(SomeClass.class), Matcher.names("getInstance"), new MethodInterceptor() {...});} </code>
 *
 * @author Sergii Leschenko
 */
public class Matchers {
    private Matchers() {}

    /**
     * Returns a matcher which matches methods with matching name.
     */
    public static Matcher<Method> names(String methodName) {
        return new Names(methodName);
    }

    private static class Names extends AbstractMatcher<Method> {
        private String methodName;

        private Names(String methodName) {
            requireNonNull(methodName, "methodName");
            this.methodName = methodName;
        }

        @Override
        public boolean matches(Method m) {
            return m.getName().equals(methodName);
        }

        @Override
        public boolean equals(Object other) {
            return other == this ||
                   other instanceof Names && ((Names)other).methodName.equals(methodName);
        }

        @Override
        public int hashCode() {
            return 37 * methodName.hashCode();
        }

        @Override
        public String toString() {
            return "names(" + methodName + ")";
        }
    }
}
