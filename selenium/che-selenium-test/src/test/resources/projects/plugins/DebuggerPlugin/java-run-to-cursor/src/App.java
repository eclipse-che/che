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
public class App {

  public static void main(String[] args) {
    String string = "line";
    doMethod();
    new InnerClass().method();
  }

  private static void doMethod() {
    String string = "method";
    System.out.println("method");
  }

  private static class InnerClass {

    String string = "inner class";
    String s = "";

    public static void method() {
      String string = "inner class method";
      System.out.println("inner class method");
    }
  }
}
