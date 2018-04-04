/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package test;

import java.util.Arrays;
import java.util.function.Predicate;

/**
 * Hello world!
 *
 */
public class App {

    public static void main(String[] args) {
        testAppInnerClass();
        testAppLambda();
    }

    private static void testAppInnerClass() {
        App anonym = new App() {
            String anonym = "App anonym";

            @Override
            public String getAppValue() {
                return getAnonymValue() + " " + super.getAppValue();
            }

            String getAnonymValue() {
                return anonym;
            }
        };

        System.out.println(anonym.getAppValue());
        System.out.println(new App().getAppValue());
        System.out.println(anonym.new Inner().getInnerValue());

        System.out.println(new StaticInner().getStaticInnerValue());
    }

    public String getAppValue() {
        final String methodValue = "App method local inner test";

        class MethodLocalInner {
            String getValue() {
                return methodValue;
            }
        }

        return new MethodLocalInner().getValue();
    }

    class Inner {
        private String innerValue = "App inner value";

        String getInnerValue() {
            return innerValue;
        }
    }

    static class StaticInner {
        private String staticInnerValue = "App static inner value";

        String getStaticInnerValue() {
            return staticInnerValue;
        }
    }

    private static void testAppLambda() {
        Predicate<Integer> filterEvenNumber = i -> {
            int j = i;
            return j % 2 == 0;
        };

        Arrays.asList(1, 2)
              .stream()
              .filter(filterEvenNumber)
              .forEach(i -> {
                  int j = i;
                  System.out.print(j);  // prints "2"
              });
    }

}
