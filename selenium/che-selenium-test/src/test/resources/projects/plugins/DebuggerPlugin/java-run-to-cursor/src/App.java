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
